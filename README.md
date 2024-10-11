# Toilet Blog Engine

This code powers [theandrewbailey.com](https://theandrewbailey.com/). This is built with Netbeans. It runs on [Java](https://openjdk.org/), [Payara](https://www.payara.fish/), [Postgres](https://www.postgresql.org/), and [Linux](https://www.debian.org/). (JPPL stack?) It should run on other Jakarta EE containers without much difficulty (untested), and other databases with a bit of effort (particularly around full text search). Pull requests for such compatibility welcome!

## Features

Write blog posts in markdown. When an article is posted, the first paragraph and image are pulled into a link and summary shown on the homepage. [This uses commonmark-java (with all first-party modules enabled) for Markdown functionality.](https://github.com/commonmark/commonmark-java)

This code serves RSS feeds automatically! Feeds are served for all articles, articles by category, all comments, and per-article comments. RSS feeds are also used to backup and restore articles and comments.

[This uses Postgres' full text search functionality.](https://www.postgresql.org/docs/current/textsearch.html) The search box features custom spellcheck and autocomplete [(thanks to trigrams)](https://www.postgresql.org/docs/current/pgtrgm.html). When an article is shown, its title is searched (can be overridden), and those results are shown at the end of the article as a 'you might also like' feature. The links are presented similarly to the homepage.

Links in the homepage, sidebar, and the 'you might also like' area won't list the same article between them.

This code is optimized for page load speed:

* File uploads are compressed with gzip/[zopfli](https://github.com/google/zopfli) and [brotli](https://github.com/google/brotli). (Maybe [zstd](https://facebook.github.io/zstd/) too, someday)

* Images will lazy load [(via `<img loading="lazy">`)](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#loading), except for all images of an article, and the first 2 images of the homepage.

* When posting an article with an image, other image uploads are searched by file type (see `site_imagePriority` configuration), and size (named *image*×*n*, [see avifify.sh for more](https://gist.github.com/theandrewbailey/4e05e20a229ef2f2c1f9a6d0e326ec2a)). All images (even if no others are found) will be placed in [a `<picture>` element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/picture) with the original.

* HTTP cache tags are set on every page, and are set to 200,000 seconds (a bit more than 2 days). Etag headers use a hash of the meaningful data served.
	* Images, CSS, and JS have unique URLs based on upload time and are served with Cache-Control: immutable

* Up to 100 pages are stored in an internal cache, and are automatically dropped when not requested for a while (must get more than 1 hit per hour to stay).

Security is important!

* All headers checked by [SecurityHeaders.com](https://securityheaders.com/) are supported, including [content security policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP), [feature policy, and permissions policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Permissions_Policy).

* [Subresource integrity](https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity) is automatically calculated for all CSS and JS uploaded and served.

* All form fields are obfuscated on a per-visitor/session basis.

* [This uses argon2 to store passwords.](https://github.com/Password4j/password4j)

## Setup guide

(Having trouble? Open an issue on this repository.)

1. Download `toilet.war` release and [`setupUsTheBlog.sh`](https://github.com/theandrewbailey/toilet/blob/master/setupUsTheBlog.sh) from the repository. Run `setupUsTheBlog.sh`.
	1. This script will create will create a directory, `toilet`, in your home directory and dump most stuff there. (Feel free to create it yourself and put the war and script there.) Payara will be extracted to your home directory under `payara6`.
	1. This script will build zopfli and brotli, setup a Postgres database, download Payara, setup a domain on Payara (and slightly optimize it), and deploy `toilet.war`.
	1. The last 7 or so lines are important. Save them somewhere.
1. Setup Toilet.
	1. Go to the toilet homepage. The script will let you know the URL (both http and https), like http://localhost:22980
		* You should see a configuration page with lots of text boxes.
	1. If you have a backup, upload it at the bottom of the page at "Not your first flush?", and skip the rest of this.
	1. Set passwords for administrative tasks (YOU MUST CHANGE THESE):
		* admin_editPosts
			* Enter this at /adminLogin to add and edit posts, list all articles and comments posted, and delete comments.
		* admin_files
			* Enter this at /adminLogin to list files uploaded to the blog, along with options to upload more or delete. Images, CSS, and JS can live here.
		* admin_health
			* Enter this at /adminLogin to show a health page, displaying some vital stats about the server and site. You can also view errors (as an RSS feed), and reload the site (drop all caches and redirect to homepage).
		* admin_imead
			* Enter this at /adminLogin to show this configuration page.
		* admin_importExport
			* Enter this at /adminLogin to show a form to upload site exports. This password should be the strongest since it can affect every piece of data and configuration on this site.
	1. Pay attention to other options:
		* site_security_baseURL
			* This is the URL where the blog expects to be. There is no room for substitutions or exceptions when it comes to the URL. Accessing the blog by any other URL (even if substituting IPs for domains or changing HTTP to/from HTTPS) will cause problems and is unsupported.
		* site_backup
			* [The site will observe International Backup Awareness Day at 1am local time every day.](https://blog.codinghorror.com/international-backup-awareness-day/) All articles, comments, configurations, and uploaded files will be dumped into this directory, along with timestamped zip of the same that can be uploaded to quickly restore the site (see "Not your first flush?" at bottom of the page, and `admin_importExport`). The script will set this as the toilet directory it created.
		* site_css, site_javascript
			* These are the CSS and JS files that are put on every page (one file per line). These should match up with an uploaded file (see `admin_files)`. The default style isn't great, but it's on purpose.
		* site_security_* options are mostly regex filters or HTTP headers.
		* page_* options are little bits of visible text scattered around pages. HTML entities will be escaped automatically. You'll probably want to customize a few of these later. Seriously, if you see a piece of text somewhere, you can change it from here. You can make the comment form a complaint form, haha!
			* page_title
			* page_error_*
		* site_* options are configurations that aren't seen, and aren't escaped.
	1. This blog will call external programs during use:
		* site_healthCommands
			* These programs are called on the health check page (see `admin_health`), with their outputs shown.
		* site_gzipCommand
			* Optional. This is called to compress file uploads with gzip. The script should have verified or built zopfli.
		* site_brCommand
			* Optional. This is called to compress file uploads with brotli. The script should have verified or built this.
	1. Click Save and start blogging!
1. To start Payara again after a reboot, run:
	* `~/payara6/glassfish/bin/asadmin start-domain toiletPayara-xxxxx` where toiletPayara-xxxxx is the Payara username that the script gave you. (You kept that info, right? I told you it's important!)

I'm using [HAProxy](https://www.haproxy.org/) in front of Payara to handle port conversions and HTTPS functionality. However, I've run Payara directly on the internet with nothing between:

* Port numbers can be changed in the admin console. (Configurations > server-config > HTTP Service > HTTP Listeners > http-listener-1, http-listener-2)

* TLS certificates (like from Let's Encrypt) must be added in payara6/glassfish/domains/toiletPayara-(number)/config/keystore.p12 which uses the Payara master password (if using `setupUsTheBlog.sh`, it's the same as admin console login). You can setup a HTTPS-only redirect in Configurations > server-config > Network Config > Network Listeners > http-listener-1 > HTTP tab > Redirect Port.


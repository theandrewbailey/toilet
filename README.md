# Toilet Blog Engine

This code powers [theandrewbailey.com](https://theandrewbailey.com/). This is built with Netbeans. It runs on [Java](https://openjdk.org/), [Payara](https://www.payara.fish/), [Postgres](https://www.postgresql.org/), and [Linux](https://www.debian.org/). (JPPL stack?) I'm using it with [HAProxy](https://www.haproxy.org/) for HTTPS functionality.

## Features

This code serves RSS feeds automatically! Feeds are served for all articles, articles by category, all comments, and per-article comments. RSS feeds are also used to backup and restore articles and comments.

When an article is posted, the first paragraph and image are pulled into a link and summary shown on the homepage.

[This code uses Postgres' full text search functionality.](https://www.postgresql.org/docs/current/textsearch.html) The search box features custom spellcheck and autocomplete [(thanks to trigrams)](https://www.postgresql.org/docs/current/pgtrgm.html). When an article is shown, its title is searched (can be overridden), and those results are shown at the end of the article as a 'you might also like' feature. The links are presented similarly to the homepage.

Links in the homepage, sidebar, and the 'you might also like' area won't list the same article between them.

This code is optimized for page load speed:

* File uploads are compressed with gzip/[zopfli](https://github.com/google/zopfli) and [brotli](https://github.com/google/brotli). (Maybe zstd too, someday)

* Most images will lazy load [(via `<img loading="lazy">`)](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#loading), except for all images of an article, and the first 2 images of the homepage.

* When posting an article with an image, other image uploads are searched by file type (see `site_imagePriority` configuration), and size (named *image*×*n*, [see avifify.sh for more](https://gist.github.com/theandrewbailey/4e05e20a229ef2f2c1f9a6d0e326ec2a)). All images (even if no others are found) will be placed in [a `<picture>` element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/picture) with the original.

* HTTP cache tags are set on every page, and are set to 200,000 seconds (a bit more than 2 days). Etag headers use a hash of the meaningful data served.
	* Images, CSS, and JS have unique URLs based on upload time and are served with Cache-Control: immutable

* Up to 100 pages are stored in an internal cache, and are automatically dropped when not requested for a while (must get more than 1 hit per hour to stay).

Security is important! All headers checked by SecurityHeaders.com are supported, including [content security policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP), [feature policy, and permissions policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/Permissions_Policy). All form fields are obfuscated on a per-visitor basis. [Subresource integrity](https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity) is automatically calculated for all CSS and JS uploaded and served.

[This uses argon2 to store passwords.](https://github.com/andreas1327250/argon2-java)

[This uses commonmark-java for Markdown functionality.](https://github.com/commonmark/commonmark-java)

## Setup guide

(Having trouble? Open an issue on this repository.)

1. Setup Postgres.
	1. Create a Postgres role (user/login) and database.
	1. Install trigram extension on database: `CREATE EXTENSION pg_trgm;`
1. [Setup Payara with a separate domain.](https://www.payara.fish/learn/getting-started-with-payara/)
	1. Copy to payara/glassfish/lib directory:
		* [Postgres JDBC driver](https://jdbc.postgresql.org/)
		* payara/glassfish/modules/jakarta.enterprise.concurrent-api.jar
	1. Start (or restart) Payara, and login to the admin console. (usually at localhost:4848)
		1. Add JDBC Connection Pool using database credentials created in step 1.
			* Initial pool size 2
			* Maximum pool size 6
			* After setup, verify that Ping works.
		1. Add JDBC Resource with JNDI name `java/toilet/default` and use connection pool from previous step.
		1. Under Concurrent Resources > Managed Executor Service:
			* Core Size (number of CPUs)
			* Maximum Pool Size (number of CPUs)
		1. Under Configurations > server-config > JVM Settings > JVM Options, set `-Xmx1g`
			* This increases the maximum memory the server can use. You'll need to restart the server at some point for this to take effect.
		1. Under Applications, deploy toilet.war
			1. Under Configurations > server-config > Virtual Servers > server, set Default Web Module to toilet
1. Setup Toilet.
	1. Go to default page of web app, like localhost:8080/toilet.
		* You should see a configuration page with lots of text boxes.
	1. If you have a backup, upload it at the bottom of the page, and skip the rest of this.
	1. Create passwords for administrative tasks:
		* admin_addArticle
			* Enter this at /adminLogin to add an article. All extensions of commonmark-java are enabled, [so most of Github Flavored Markdown is supported.](https://github.github.com/gfm/)
		* admin_dingus
			* Enter this at /adminLogin to show a markdown dingus, for those times you want to convert markdown to HTML, but don't want to post something. This uses the same markdown functionality that adding and editing articles does.
		* admin_editPosts
			* Enter this at /adminLogin to list all articles and comments. You can edit articles, delete comments, or download a full site backup here. You can restore a backup, too, with the admin_addArticle password.
		* admin_errorLog
			* Enter this at /adminLogin to show an RSS feed of errors. This includes HTTP 4xx and 5xx errors, as well as content security policy reports.
		* admin_files
			* Enter this at /adminLogin to list files uploaded to the blog, along with options to upload more or delete.
		* admin_health
			* Enter this at /adminLogin to show a health check page.
		* admin_imead
			* Enter this at /adminLogin to show this configuration page.
		* admin_reload
			* Enter this at /adminLogin to flush all caches in Payara and direct to the homepage.
	1. Pay attention to other options:
		* site_security_baseURL
			* This is the URL where the blog expects to be.
		* site_backup
			* The site will attempt to perform a backup at 1am local time. All articles, comments, configurations, and uploaded files will be dumped into this directory, along with timestamped zip of the same that can be uploaded to quickly restore the site (see option at bottom of the page).
	1. This blog will call external programs during use:
		* site_healthCommands
			* These programs are called on the health check page, with their outputs shown.
		* site_gzipCommand
			* Optional. This is called to compress file uploads with gzip. [Compile and/or install zopfli. https://github.com/google/zopfli](https://github.com/google/zopfli)
		* site_brCommand
			* Optional. This is called to compress file uploads. [Compile and/or install brotli. https://github.com/google/brotli](https://github.com/google/brotli)
	1. The rest are defaults that should work for most, I hope.
		* site_security_* options are mostly regex filters or HTTP headers.
		* page_* options are little bits of visible text scattered around pages. You'll probably want to customize a few of these later.
			* Seriously, if you see a piece of text somewhere, you can change it from here. You can make comment form a complaint form, haha!
	1. Click Save and start blogging!

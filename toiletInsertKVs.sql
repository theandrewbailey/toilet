-- TOILET KV DEV
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM tools.localization;
COMMIT;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

-- env specific vars
INSERT INTO tools.localization (key, localecode, value) VALUES 
('page_css', 'en', 'https://localhost:8181/blog/content/toilet.css'),
('page_cssamp', 'en', 'toiletamp.css'),
('page_javascript', 'en', 'https://localhost:8181/blog/content/toilet.js'),
('file_brCommand', '', '/home/alpha/brotli -c -q 6 -w 24'),
('file_gzipCommand', '', 'gzip -c -9 '),
('site_title', 'en',  'the Development Environment'),
('site_tagline', 'en',  'We''re not in production anymore!'),
('site_backup', '', '/home/alpha/toiletback/'),
('libOdyssey_guard_canonicalURL', '', 'https://localhost:8181/blog/'),
('libOdyssey_certificate_name', '', 's1as'),
--('libOdyssey_content_security_policy', '', 'default-src data: https://localhost https://10.1.1.10 https://theandrewbailey.com; script-src https://localhost https://10.1.1.10; object-src ''none''; frame-ancestors ''self''; require-sri-for script style;'),
('libOdyssey_guard_domains', '', '^http://(?:10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}|localhost)(?::8080)?(?:/.*)?$
^https://(?:10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}|localhost|alphavm\.lan)(?::8181)?(?:/.*)?$');

-- non specific vars
INSERT INTO tools.localization (key, localecode, value) VALUES 
('coroner_13', 'en', 'Congratulations, the database is totally GONE.<br/><br/>Please relay this to the site owner.'),
('coroner_30', 'en', 'I''m sorry Dave, but I can''t get that article for you right now.'),
('coroner_41', 'en', 'What?<br/>Could you be more specific?'),
('coroner_42', 'en', 'What is your name?<br/>What is your quest?<br/>What is the airspeed velocity of an unladen swallow?'),
('coroner_45', 'en', 'Great, the server is being stubborn with the DB again.<br/><br/>Please relay this message to the site owner.'),
('coroner_400', 'en', 'Your post failed.<br/><br/>You must put a name and your comment.<br/>And don''t be insanely long.<br/>And wait three seconds.'),
('coroner_401', 'en', 'Who do you think you are?'),
('coroner_403', 'en', 'Go away.'),
('coroner_404', 'en', 'This content is not available in your country.'),
('coroner_405', 'en', 'You can''t do that, Dave. What do you think you are doing, Dave?'),
('coroner_500', 'en', 'Just what do you think you''re doing, Dave?<br/>Dave, I really think I''m entitled to an answer to that question.'),
('coroner_501', 'en', 'Just what do you think you''re doing, Dave?<br/>Dave, where are you going?'),
('coroner_599', 'en', 'ZAP goes the spam!'),
('content_unauthorized', 'en', 'Unauthorized content request.'),
('page_admin_door', 'en', 'What will you do if it never goes out?'),
('page_amp', 'en', 'This is an AMP formatted page. See and comment on the original at theAndrewBailey.com'),
('page_articleFooter', 'en', 'Posted {0} under {1}. '),
('page_commentDisabled', 'en', '<p class="noComment noPrint">No new comments may be posted for this article at this time.</p>'),
('page_commentFooter', 'en', 'Posted {0} by {1}. '),
('page_dateFormat', 'en', 'EEEE, MMMM d, yyyy ''at'' h:mm a z'),
('page_foot', 'en', '<p>Posts and comments may be deleted at any time without your knowledge per the developer''s discretion. Off topic discussion and unsolicited advertisements are strictly forbidden. This site, its visitors, and its content are continually monitored by people who can make your life miserable. By visiting or posting, you implicitly agree to the above terms.</p><p>tl;dr: all this might be gone tomorrow.</p>'),
('page_footFormat', 'en', 'Request received at {0}. Rendered in {1} milliseconds.'),
('page_formReplacement', 'en', '<p><a href="#" class="refreshLink">Refresh the page and wait three seconds to post a comment.</a> If you are still getting this message, make sure you have cookies and javascript enabled.</p>'),
('page_message_legend', 'en', 'Leave a message:'),
('page_message_name', 'en', 'Name: '),
('page_message_text', 'en', 'Your two cents: '),
('page_patternMismatch', 'en', 'I don''t understand this.'),
('page_side_top', 'en', '<ul><li><a href="http://thenexus.tv/category/cs/">Listen to my podcast: Control Structure</a></li><li><a href="https://steamcommunity.com/id/praetor_alpha/">See what I&apos;m playing on Steam</a></li></ul>'),
('page_side_bottom', 'en', ''),
('page_sideRecent', 'en', 'Latest entries:'),
('page_sideRecentCategory', 'en', 'Latest entries in {0}:'),
('page_spruce_entry', 'en', '<p>Spruce is a random sentence generator written in Python. It has found a home on this server, and likes it here.</p><p>It was one of those late-night epiphanies, you see. It was during the three week summer break of 2008, and everyone had left the apartment to go home. It was just me and my machines having a good time.</p><p>A passing thought wandered by: Write a book on programming. Then I wondered, if I did, what kind of examples would I put in it. I began thinking away. Being pretty good at English (and having absolutely adored the English course the previous quarter), I realized that parts of speech could map easily to an object-oriented paradigm. "Make a random sentence generator," I thought.</p><p>Being past 4am, low on sleep, and not having seen a soul in a few days, I thought it was the best thing ever. Thought about how funny it would be. After a day or two, it started forming complete sentences. "Lesiure is a limp spruce" was one of the first sentences generated. Being of a relaxed mind at the time, it caught my attention: it just named itself.</p><p>It was entered at my college''s coding contest the next quarter. It didn''t get me any prizes, but it intrigued many people. I eventually got it working with the Windows speech API... creepy.</p><p>When I started coding this blog, I got a second epiphany: stuff Spruce into Jython, put it on your server, and have a sentence spit out on every request. Then when I made my RSS library, <a href="rss/Spruce.rss">I made an RSS feed for Spruce.</a></p><p>So here you are.</p>'),
('page_spruce_header', 'en', 'Here''s some more wisdom from Spruce:'),
('page_spruce_link', 'en', 'Want more Spruce?'),
('page_topics', 'en', 'Blog topics:'),
('page_valueMissing', 'en', 'You''re not giving me anything useful to go on.'),

('libOdyssey_firstHoneypot', '', '10000000'),
('libOdyssey_guard_enable', '', 'true'),
('libOdyssey_guard_errors', '', 'true'),
('libOdyssey_guard_emptySessions', '', '10x5x1'), -- number of sessions X seconds X requests that define 'empty'
('libOdyssey_guard_sessionsPerSecond', '', '5x5'), -- number of sessions X seconds
('admin_addEntry', '', '6400b25f5509142728ea0b69846d7e19ff9f334786d14cfd21682c15e458c022'),
('admin_anal', '', '6e90ca854d36575dc20a4e3183c46b431fa72632d1903b192e709f30e10277ea'),
('admin_content', '', '05ae21d78b869a25a26950afc547cb14647885b4c7493d0d435ae94a0159dd03'),
('admin_comments', '', '8e75a35dd3ea1e9a23cf8bf7ad9fa38a2c5e698b96e9c701af8e8aa7d74dc2a8'),
('admin_import', '', 'c1a6f196258307a0a31f011d0064ef7c8d777dde2a56b9f75a9daf5aca8b6deb'),
('admin_log', '', '5b6176784270db9e20689dfe36c11121e8f3da879fe79328a02817a05b71d519'),
('admin_magicwords', '', '008a15f71f612c68ac4501cc2dc82e92890eb4b05160a25abf389f684df3f342'),
('admin_posts', '', '9b35c75c8d3ac758d41bba6b20fead83e11139955c9498f22383c57a350fa90a'),
('admin_reset', '', '02be7aeaae80216cbf59c07599cbcc11e78ccc378dd4710889dbc14b0d92a282'),
('admin_sessions', '', '17721d4792ebd23c963c1270e18d0d0d6c08d4ab9bc354b04106cce3f678ad6d'),
('argon2_salt', '', 'SOMEreallyLONGsaltTH1NGforP455WORD$'),
('entry_defaultCategory', '', ' '),
('entry_defaultName', '', 'Andrew Bailey'),
('index_pac', '', '3'),
('index_ppp', '', '7'),
('site_favicon', '', 'data:image/x-icon;base64,AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAHAkBAB0KAgDAwFAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIiIiIiIiIiIiIiIiIhIiIiIjIiIiIjIiIjMyIiIjMyIiIzMiIjMyIiIiMzIjMyIiIiIjMzMyIiIiIiIzMyIiIiIiIjMzIiIiIiIjMzMyIiIiIjMyIzMiIiIjMyIiMzIiIjMyIiIjMyIiIyIiIiIyIiIgIiIiIiIiIiIiIiIiIiIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'),
('spruce_dictionary', '', 'dictionary.xml'),
('spruce_name', '', '-Spruce'),
('rss_articleCount', '', '12'),
('rss_commentCount', '', '12'),
('rss_copyright', '', 'CC BY-SA Andrew Bailey praetoralpha@gmail.com theandrewbailey.com'),
('rss_language', '', 'en-us'),
('rss_master', '', 'praetoralpha@gmail.com (Andrew Bailey)'),
('rss_spruceCount', '', '200');


INSERT INTO tools.localization (key, localecode, value) VALUES ('libOdyssey_guard_denyUserAgents', '', '.*ZmEu.*
\QMozilla/5\.0 (compatible; WBSearchBot/1\.1; +http://www\.warebay\.com/bot\.html)\E
\QMozilla/5\.0 (compatible; SISTRIX Crawler; http://crawler\.sistrix\.net/)\E
\QMorfeus Fucking Scanner\E');

INSERT INTO tools.localization (key, localecode, value) VALUES ('libOdyssey_guard_honeypots', '', '.*\.php.*
.*\.PHP.*
.*data:image/x-icon;base64.*
.*\.ssh/id_rsa.*
.*\.ssh/id_dsa.*');

INSERT INTO tools.localization (key, localecode, value) VALUES ('site_spamwords', '', '.*lolita.*
.*http://.*
.* fuck.*
.* shit.*
.* ass .*');

INSERT INTO tools.localization (key, localecode, value) VALUES ('site_acceptableContentDomains', '', '^https?://(?:www\.|content\.)?theandrewbailey\.com(?::[0-9]{1,5})?(?:$|/.*)
^https?://localhost(?::[0-9]{1,5})?(?:$|/.*)
^https?://(?:10\.[0-9]{1,3}\.|192\.168\.)[0-9]{1,3}\.[0-9]{1,3}(?::[0-9]{1,5})?(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?google(?:\.com)?(?:\.[a-zA-Z]{2}){0,2}(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?googleusercontent(?:\.com)?(?:\.[a-zA-Z]{2}){0,2}(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?feedly\.com(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?slack\.com(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?bing\.com(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?yandex(?:\.com)?(?:\.[a-zA-Z]{2})?(?:$|/.*)
^https?://images\.rambler\.ru(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?yahoo(?:\.com)?(?:\.[a-zA-Z]{2})?(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?duckduckgo\.com(?:$|/.*)
^https?://(?:[a-zA-Z]+\.)+?baidu\.com(?:$|/.*)');

COMMIT WORK;
-- INSERT INTO tools.keyvalue (key, value) VALUES ('', '');

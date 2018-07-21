-- TOILET KV DEV
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM tools.keyvalue;
DELETE FROM tools.localization where key = 'page_css';
DELETE FROM tools.localization where key = 'page_cssamp';
DELETE FROM tools.localization where key = 'page_javascript';
COMMIT;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

-- env specific vars
INSERT INTO tools.localization (key, localecode, value) VALUES 
('page_css', 'en', 'https://localhost/blog/content/toilet.css'),
('page_cssamp', 'en', 'toiletamp.css'),
('page_javascript', 'en', 'https://localhost/blog/content/toilet.js');

INSERT INTO tools.keyvalue (key, value) VALUES 
('file_brCommand', '/home/alpha/brotli -c -q 6 -w 24'),
('file_gzipCommand', 'gzip -c -9 '),
('site_title', 'the Development Environment'),
('site_tagline', 'We''re not in production anymore!'),
('site_backup', '/home/alpha/toiletback/'),
('libOdyssey_guard_canonicalURL', 'https://localhost/blog/'),
('libOdyssey_certificate_name', 's1as'),
('libOdyssey_content_security_policy', 'default-src data: https://localhost https://10.1.1.10; script-src https://localhost https://10.1.1.10; object-src ''none''; frame-ancestors ''self''; require-sri-for script style;'),
('libOdyssey_guard_domains', '^http://(?:10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}|localhost)(?::8080)?.*
^https://(?:10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}|localhost|alphavm\.lan)(?::8181)?.*');

-- non specific vars
INSERT INTO tools.keyvalue (key, value) VALUES 
('libOdyssey_firstHoneypot', '10000000'),
('libOdyssey_guard_enable', 'true'),
('libOdyssey_guard_errors', 'true'),
('libOdyssey_guard_emptySessions', '10x5x1'), -- number of sessions X seconds X requests that define 'empty'
('libOdyssey_guard_sessionsPerSecond', '5x5'), -- number of sessions X seconds
('admin_addEntry', 'b6cec50bd3a96f06f09b8a3e81d6a8bebd26711af266f8a8cfd494dc00c82fa0'),
('admin_anal', 'eece1a903f0c17636d2694d141bbb43b4ae3341245ebd68c48287a15b71e9da7'),
('admin_content', 'a12be70b715698f6ee5798f8254640f9ea482fbc19022cb2ea14f0aedf2ef2d2'),
('admin_comments', 'f48131c09fbed1579600d92f25cdd41937428cd72190131ca1a1a4ecc19c9daa'),
('admin_import', 'fcb4d7c84d539fa7408a4039310fd478bc3ceb873a3173b83c5318f99101c4da'),
('admin_log', '5cc3b7671a04b96a001f24530d16b81a84e61258baaa97717f2828dbf8af494a'),
('admin_magicwords', 'b8aca492d701995d96ae1673e63642bdd3ac4136713085955da74d59976652ba'),
('admin_posts', '6f2c109a4db559a70300a1172cd1ef5dacec9f856fa2fc17463542d05d6f409f'),
('admin_reset', '042d69773145d0e8d8ff293facf6c12b97dfc306ec6c680677a4959ad565f914'),
('admin_sessions', 'd936ab9bc9f242c2598d4f0e30f200d9f790f636fe0d32d66ead100f155ff4d4'),
('argon2_salt', 'SOMEreallyLONGsaltTH1NGforP455WORD$'),
('entry_defaultCategory', ' '),
('entry_defaultName', 'nobody'),
('index_pac', '3'),
('index_ppp', '7'),
('site_favicon', 'data:image/x-icon;base64,AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAHAkBAB0KAgDAwFAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIiIiIiIiIiIiIiIiIhIiIiIjIiIiIjIiIjMyIiIjMyIiIzMiIjMyIiIiMzIjMyIiIiIjMzMyIiIiIiIzMyIiIiIiIjMzIiIiIiIjMzMyIiIiIjMyIzMiIiIjMyIiMzIiIjMyIiIjMyIiIyIiIiIyIiIgIiIiIiIiIiIiIiIiIiIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'),
('spruce_dictionary', 'dictionary.xml'),
('spruce_name', '-Spruce'),
('rss_articleCount', '30'),
('rss_commentCount', '60'),
('rss_copyright', 'CC BY-SA nobody'),
('rss_language', 'en-us'),
('rss_master', 'nobody'),
('rss_spruceCount', '200');


INSERT INTO tools.keyvalue (key, value) VALUES ('libOdyssey_guard_denyUserAgents', '.*ZmEu.*
\QMozilla/5\.0 (compatible; WBSearchBot/1\.1; +http://www\.warebay\.com/bot\.html)\E
\QMozilla/5\.0 (compatible; SISTRIX Crawler; http://crawler\.sistrix\.net/)\E
\QMorfeus Fucking Scanner\E');

INSERT INTO tools.keyvalue (key, value) VALUES ('libOdyssey_guard_honeypots', '.*\.php.*
.*\.PHP.*
.*data:image/x-icon;base64.*
.*\.ssh/id_rsa.*
.*\.ssh/id_dsa.*');

INSERT INTO tools.keyvalue (key, value) VALUES ('site_spamwords', '.*http://.*');

INSERT INTO tools.keyvalue (key, value) VALUES ('site_acceptableContentDomains', '^https?://(?:www\.|content\.)?theandrewbailey\.com(?::[0-9]{1,5})?(?:$|/.*)
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

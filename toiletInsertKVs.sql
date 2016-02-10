-- TOILET KV DEV
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM imead.keyvalue;
DELETE FROM imead.localization where key = 'page_css';
COMMIT;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

-- env specific vars
INSERT INTO imead.localization (key, localecode, value) VALUES 
('page_css', 'en', 'http://localhost:8080/toilet/content/toilet.css');
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_domains', '^http://(?:10\.1\.1\.10|localhost)(?::8080)?.*
^https://(?:10\.1\.1\.10|localhost)(?::8181)?.*');

INSERT INTO imead.keyvalue (key, value) VALUES 
('site_title', 'toilet Blog Engine'),
('site_tagline', 'the Development Environment'),
('site_backup', '~/toiletback/'),
('libOdyssey_guard_canonicalURL', 'http://localhost:8080/toilet/'),
('libOdyssey_certificate_name', 'certificaqte');

-- non specific vars
INSERT INTO imead.keyvalue (key, value) VALUES 
('libOdyssey_firstHoneypot', '10000000'),
('libOdyssey_guard_enable', 'false'),
('libOdyssey_guard_errors', 'true'),
('libOdyssey_guard_emptySessions', '10x5x1'), -- number of sessions X seconds X requests that define 'empty'
('libOdyssey_guard_sessionsPerSecond', '5x5'), -- number of sessions X seconds
('admin_addEntry', '$s0$c0801$i3W246aGMd5MPcElpH9BVQ==$6HFP4JzFFc8jrq99D2mo8nZE+FoShodwrXixzqYgQ9Q='),
('admin_anal', '$s0$c0801$4fc5Zvr+aRiQvudi2XN7lg==$yqhAis1wvnlb3fHraA7R/HYw+ZnxRjXfufSfUAhnMH0='),
('admin_content', '$s0$c0801$XhYmehV3fSz3rj5qn/ToZA==$9p5e3eMF33yphqkTJvrKjEThHDzMYCdBtFbZoLHhc/0='),
('admin_comments', '$s0$c0801$LbbMl6uTD2iN7nYhk/Zg1A==$OvoLMkIk0FxKhkLhNtuHSBuv+j7xgYO5LnE/PaoUOsM='),
('admin_import', '$s0$c0801$az8tuBFsewcPzQbEXOfbSQ==$GC4XC8vfhAKhGVj23quAs6KIjSj4Vjop8oamPTF3NKA='),
('admin_log', '$s0$c0801$+U3PR30FJCgu+/IJQ1TkVw==$L1xRIJ0PPnwdbOhlwaZaZZyc3ifj6tKOg9a2sPzPfXk='),
('admin_magicwords', '$s0$c0801$OSgqAP/BH0+tOiVqkbZWmA==$Mai0JgnxhgE9ln4Wx1k9/16ci53qRNua+0XuOR5vCtk='),
('admin_posts', '$s0$c0801$hqXlv8iy9y/NLjcqhPl40Q==$T2qYNRzDTxonu7+oL3UWDxtFdW4Wo6bopru0E7465zE='),
('admin_reset', '$s0$c0801$uDjRogxhYptlm137ejlctw==$hKtbtdl8EaMonxR0oV+Y4tzGhMZAhmK/geW20v9CGQs='),
('admin_sessions', '$s0$c0801$yGa/9TeBtPrePkVWSjiqWw==$svNfypShN54BbD/gaYNzNZ0DESGuqZLgOF3iXmsqcLg='),
('entry_defaultCategory', ' '),
('entry_defaultName', 'nobody'),
('index_pac', '3'),
('index_ppp', '5'),
('site_favicon', 'data:image/x-icon;base64,AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAHAkBAB0KAgDAwFAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIiIiIiIiIiIiIiIiIhIiIiIjIiIiIjIiIjMyIiIjMyIiIzMiIjMyIiIiMzIjMyIiIiIjMzMyIiIiIiIzMyIiIiIiIjMzIiIiIiIjMzMyIiIiIjMyIzMiIiIjMyIiMzIiIjMyIiIjMyIiIyIiIiIyIiIgIiIiIiIiIiIiIiIiIiIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'),
('spruce_dictionary', 'dictionary.xml'),
('spruce_name', '-Spruce'),
('rss_articleCount', '30'),
('rss_commentCount', '60'),
('rss_copyright', 'nobody'),
('rss_language', 'en-us'),
('rss_master', 'nobody'),
('rss_spruceCount', '200');


INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_denyUserAgents', '.*ZmEu.*
\QMozilla/5\.0 (compatible; WBSearchBot/1\.1; +http://www\.warebay\.com/bot\.html)\E
\QMozilla/5\.0 (compatible; SISTRIX Crawler; http://crawler\.sistrix\.net/)\E
\Qwww\.socialayer\.com Agent 0.1\E
\QTurnitinBot/2\.1 (http://www\.turnitin\.com/robot/crawlerinfo\.html)\E
.*Mail\.RU.*
.*AhrefsBot.*');

INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_honeypots', '.*muieblackcat.*
.*/user/soapCaller.*
.*/oscommerce/.*
.*/catalog/.*
.*/shop.*
.*#.*
.*\.php.*
.*\.PHP.*
.*data:image/x-icon;base64.*');

INSERT INTO imead.keyvalue (key, value) VALUES ('site_spamwords', '.*http://.*');

INSERT INTO imead.keyvalue (key, value) VALUES ('site_acceptableContentDomains', '^https?://localhost(?::[0-9]{1,5})?(?:$|/.*)
^https?://(?:10\.[0-9]{1,3}\.|192\.168\.)[0-9]{1,3}\.[0-9]{1,3}(?::[0-9]{1,5})?(?:$|/.*)
^https?://(?:www|images|encrypted)\.google(?:\.com)?(?:\.[a-zA-Z]{2}){0,2}(?:$|/.*)
^https?://(?:[a-zA-Z]*\.)?feedly\.com(?:$|/.*)
^https?://(?:www|[a-zA-Z]{2})\.bing\.com(?:$|/.*)
^https?://images.yandex(?:\.com)?(?:\.[a-zA-Z]{2})?(?:$|/.*)
^https?://images\.rambler\.ru(?:$|/.*)
^https?://(?:[a-zA-Z]{2}\.)?images\.search\.yahoo\.com(?:$|/.*)
^https?://image\.baidu\.com(?:$|/.*)');

COMMIT WORK;
-- INSERT INTO imead.keyvalue (key, value) VALUES ('', '');

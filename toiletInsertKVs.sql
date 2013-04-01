-- TOILET KV DEV
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM imead.keyvalue;
COMMIT;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

-- env specific vars
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_host', '192.168.0.198:8080');
INSERT INTO imead.keyvalue (key, value) VALUES ('thisURL', 'http://192.168.0.198:8080/toilet/');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_redirect', '');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_title', 'toilet Blog Engine');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_tagline', 'the Development Environment');
INSERT INTO imead.keyvalue (key, value) VALUES ('entry_akismetKey', '');
INSERT INTO imead.keyvalue (key, value) VALUES ('site_backup', '/home/alphavm/toiletback/');

-- non specific vars
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_firstHoneypot', '10000000');
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_enable', 'true');
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_errors', 'true');
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_emptySessions', '10x5x1'); -- number of sessions X seconds X requests that define 'empty'
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_sessionsPerSecond', '5x5'); -- number of sessions X seconds
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_addEntry', '$s0$c0801$i3W246aGMd5MPcElpH9BVQ==$6HFP4JzFFc8jrq99D2mo8nZE+FoShodwrXixzqYgQ9Q=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_anal', '$s0$c0801$4fc5Zvr+aRiQvudi2XN7lg==$yqhAis1wvnlb3fHraA7R/HYw+ZnxRjXfufSfUAhnMH0=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_content', '$s0$c0801$XhYmehV3fSz3rj5qn/ToZA==$9p5e3eMF33yphqkTJvrKjEThHDzMYCdBtFbZoLHhc/0=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_comments', '$s0$c0801$LbbMl6uTD2iN7nYhk/Zg1A==$OvoLMkIk0FxKhkLhNtuHSBuv+j7xgYO5LnE/PaoUOsM=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_door', 'What do you have to say for yourself?');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_import', '$s0$c0801$az8tuBFsewcPzQbEXOfbSQ==$GC4XC8vfhAKhGVj23quAs6KIjSj4Vjop8oamPTF3NKA=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_log', '$s0$c0801$+U3PR30FJCgu+/IJQ1TkVw==$L1xRIJ0PPnwdbOhlwaZaZZyc3ifj6tKOg9a2sPzPfXk=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_magicwords', '$s0$c0801$OSgqAP/BH0+tOiVqkbZWmA==$Mai0JgnxhgE9ln4Wx1k9/16ci53qRNua+0XuOR5vCtk=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_posts', '$s0$c0801$hqXlv8iy9y/NLjcqhPl40Q==$T2qYNRzDTxonu7+oL3UWDxtFdW4Wo6bopru0E7465zE=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_reset', '$s0$c0801$uDjRogxhYptlm137ejlctw==$hKtbtdl8EaMonxR0oV+Y4tzGhMZAhmK/geW20v9CGQs=');
INSERT INTO imead.keyvalue (key, value) VALUES ('admin_sessions', '$s0$c0801$yGa/9TeBtPrePkVWSjiqWw==$svNfypShN54BbD/gaYNzNZ0DESGuqZLgOF3iXmsqcLg=');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_13', 'Congratulations, the database is totally GONE.<br/><br/>Please relay this.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_30', 'I''m sorry Dave, but I can''t get that article for you right now.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_400', 'Your post failed.<br/><br/>You must put a name and your comment.<br/>And don''t be insanely long.<br/>And wait three seconds.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_401', 'Who do you think you are?');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_404', 'You can''t get there from here. There''s just no way around it. That page actually doesn''t exist.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_405', 'You can''t do that, Dave. What do you think you are doing, Dave?');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_45', 'Great, the server is being stubborn with the DB again.<br/><br/>Please relay this message to the developer.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_500', 'Just what do you think you''re doing, Dave?<br/>Dave, I really think I''m entitled to an answer to that question.');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_501', 'Just what do you think you''re doing, Dave?<br/>Dave, where are you going?');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_599', 'ZAP goes the spam!');
INSERT INTO imead.keyvalue (key, value) VALUES ('coroner_ie6', 'http://www.ie6-must-die.com/');
INSERT INTO imead.keyvalue (key, value) VALUES ('entry_defaultCategory', ' ');
INSERT INTO imead.keyvalue (key, value) VALUES ('entry_defaultName', 'Andrew Bailey');
INSERT INTO imead.keyvalue (key, value) VALUES ('entry_spruce', '<p>Spruce is a random sentence generator written in Python. <a href="rss/Spruce.rss">Look, an RSS feed for Spruce!</a></p>');
INSERT INTO imead.keyvalue (key, value) VALUES ('index_pac', '2');
INSERT INTO imead.keyvalue (key, value) VALUES ('index_ppp', '5');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_articleFooter', 'Posted {0} under {1}. ');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_commentFooter', 'Posted {0} by {1}. ');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_commentPostDelay', '3000');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_css', 'content/style/toilet.css');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_dateFormat', 'EEEE, MMMM d, yyyy ''at'' h:mm a z');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_favicon', 'data:image/x-icon;base64,AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARAAAAHAkBAB0KAgDAwFAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIiIiIiIiIiIiIiIiIhIiIiIjIiIiIjIiIjMyIiIjMyIiIzMiIjMyIiIiMzIjMyIiIiIjMzMyIiIiIiIzMyIiIiIiIjMzIiIiIiIjMzMyIiIiIjMyIzMiIiIjMyIiMzIiIjMyIiIjMyIiIyIiIiIyIiIgIiIiIiIiIiIiIiIiIiIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_foot', '<p>Posts and comments may be deleted at any time without your knowledge.</p>');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_footFormat', 'Request received at {0}. Rendered in {1} milliseconds.');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_formReplacement', '<p><a href="#comments" onClick="document.location.reload(true)">Refresh the page and wait three seconds to post a comment.</a> If you are still getting this message, make sure you have cookies and javascript enabled.</p>');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_honeypot', 'index.php');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_message_legend', 'Leave a message:');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_message_name', 'Name: ');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_message_text', 'Your two cents: ');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_side', '');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_sideRecent', 'Latest entries:');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_sideRecentCategory', 'Latest entries in {0}:');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_spruce_header', 'Here''s some more wisdom from Spruce:');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_test', 'LOLSKTJOWEIFJKLSDFJ');
INSERT INTO imead.keyvalue (key, value) VALUES ('page_topics', 'Topics:');
INSERT INTO imead.keyvalue (key, value) VALUES ('spruce_dictionary', 'dictionary.xml');
INSERT INTO imead.keyvalue (key, value) VALUES ('spruce_link', 'Want more Spruce?');
INSERT INTO imead.keyvalue (key, value) VALUES ('spruce_name', '-Spruce');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_articleCount', '30');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_commentCount', '60');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_copyright', 'CC BY-SA');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_language', 'en-us');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_master', 'idiot@this.com');
INSERT INTO imead.keyvalue (key, value) VALUES ('rss_spruceCount', '200');


INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_denyUserAgents', '.*ZmEu.*
\QMozilla/5.0 (compatible; WBSearchBot/1.1; +http://www.warebay.com/bot.html)\E
\QMozilla/5.0 (compatible; SISTRIX Crawler; http://crawler.sistrix.net/)\E
\QMorfeus Fucking Scanner\E
\Qwww.socialayer.com Agent 0.1\E
\QTurnitinBot/2.1 (http://www.turnitin.com/robot/crawlerinfo.html)\E
.*Mail\.RU.*
.*\QMozilla/4.0 (compatible; MSIE 5\E.*
.*AhrefsBot.*');
INSERT INTO imead.keyvalue (key, value) VALUES ('libOdyssey_guard_honeypots', '.*muieblackcat.*
.*/user/soapCaller.*
.*/oscommerce/.*
.*/catalog/.*
.*/shop.*
.*#.*
.*php.*
.*PHP.*
.*data:image/x-icon;base64.*');

COMMIT WORK;
-- INSERT INTO imead.keyvalue (key, value) VALUES ('', '');

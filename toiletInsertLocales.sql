-- NAPKIN LOCALE
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM imead.localization;
COMMIT WORK;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

INSERT INTO imead.localization (key, localecode, value) VALUES 
('coroner_13', 'en', 'Congratulations, the database is totally GONE.<br/><br/>Please relay this to the site owner.'),
('coroner_30', 'en', 'I''m sorry Dave, but I can''t get that article for you right now.'),
('coroner_45', 'en', 'Great, the server is being stubborn with the DB again.<br/><br/>Please relay this message to the site owner.'),
('coroner_400', 'en', 'Your post failed.<br/><br/>You must put a name and your comment.<br/>And don''t be insanely long.<br/>And wait three seconds.'),
('coroner_401', 'en', 'Who do you think you are?'),
('coroner_403', 'en', 'Go away.'),
('coroner_404', 'en', 'You can''t get there from here. There''s just no way around it. That page actually doesn''t exist.'),
('coroner_405', 'en', 'You can''t do that, Dave. What do you think you are doing, Dave?'),
('coroner_500', 'en', 'Just what do you think you''re doing, Dave?<br/>Dave, I really think I''m entitled to an answer to that question.'),
('coroner_501', 'en', 'Just what do you think you''re doing, Dave?<br/>Dave, where are you going?'),
('coroner_599', 'en', 'ZAP goes the spam!'),
('content_unauthorized', 'en', 'Unauthorized content request.'),
('page_admin_door', 'en', 'What will you do if it never goes out?'),
('page_articleFooter', 'en', 'Posted {0} under {1}. '),
('page_commentDisabled', 'en', '<p class="noComment noPrint">No new comments may be posted for this article at this time.</p>'),
('page_commentFooter', 'en', 'Posted {0} by {1}. '),
('page_dateFormat', 'en', 'EEEE, MMMM d, yyyy ''at'' h:mm a z'),
('page_foot', 'en', '<p>Posts and comments may be deleted at any time without your knowledge per the developer''s discretion. Off topic discussion and unsolicited advertisements are strictly forbidden. This site, its visitors, and its content are continually monitored by people who can make your life miserable. By visiting or posting, you implicitly agree to the above terms.</p><p>tl;dr: all this might be gone tomorrow.</p>'),
('page_footFormat', 'en', 'Request received at {0}. Rendered in {1} milliseconds.'),
('page_formReplacement', 'en', '<p><a href="#comments" onClick="document.location.reload(true)">Refresh the page and wait three seconds to post a comment.</a> If you are still getting this message, make sure you have cookies and javascript enabled.</p>'),
('page_message_legend', 'en', 'Leave a message:'),
('page_message_name', 'en', 'Name: '),
('page_message_text', 'en', 'Your two cents: '),
('page_side_top', 'en', ''),
('page_side_bottom', 'en', ''),
('page_sideRecent', 'en', 'Latest entries:'),
('page_sideRecentCategory', 'en', 'Latest entries in {0}:'),
('page_spruce_entry', 'en', '<p class="noComment noPrint">Spruce is a random sentence generator written in Python. It has found a home on this server, and likes it here.</p><p>It was one of those late-night epiphanies, you see. It was during the three week summer break of 2008, and everyone had left the apartment to go home. It was just me and my machines having a good time.</p><p>A passing thought wandered by: Write a book on programming. Then I wondered, if I did, what kind of examples would I put in it. I began thinking away. Being pretty good at English (and having absolutely adored the English course the previous quarter), I realized that parts of speech could map easily to an object-oriented paradigm. "Make a random sentence generator," I thought.</p><p>Being past 4am, low on sleep, and not having seen a soul in a few days, I thought it was the best thing ever. Thought about how funny it would be. After a day or two, it started forming complete sentences. "Lesiure is a limp spruce" was one of the first sentences generated. Being of a relaxed mind at the time, it caught my attention: it just named itself.</p><p>It was entered at my college''s coding contest the next quarter. It didn''t get me any prizes, but it intrigued many people. I eventually got it working with the Windows speech API... creepy.</p><p>When I started coding this blog, I got a second epiphany: stuff Spruce into Jython, put it on your server, and have a sentence spit out on every request. Then when I made my RSS library, <a href="rss/Spruce.rss">I made an RSS feed for Spruce.</a></p><p>So here you are.</p>'),
('page_spruce_header', 'en', 'Here''s some more wisdom from Spruce:'),
('page_spruce_link', 'en', 'Want more Spruce?'),
('page_topics', 'en', 'Topics:');
COMMIT WORK;

-- INSERT INTO imead.localization VALUES ('', 'en', '');
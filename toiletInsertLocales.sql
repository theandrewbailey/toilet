BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;
DELETE FROM tools.localization;
COMMIT WORK;
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

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
('page_amp', 'en', 'This is an AMP formatted page. See and comment on the original over here'),
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
('page_side_top', 'en', ''),
('page_side_bottom', 'en', ''),
('page_sideRecent', 'en', 'Latest entries:'),
('page_sideRecentCategory', 'en', 'Latest entries in {0}:'),
('page_spruce_entry', 'en', '<p>?</p>'),
('page_spruce_header', 'en', 'Here''s some more wisdom from Spruce:'),
('page_spruce_link', 'en', 'Want more Spruce?'),
('page_topics', 'en', 'Blog topics:'),
('page_valueMissing', 'en', 'You''re not giving me anything useful to go on.');
COMMIT WORK;

-- INSERT INTO tools.localization VALUES ('', 'en', '');
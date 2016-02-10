CREATE INDEX fileupload_filename
   ON files.fileupload (filename ASC NULLS LAST);
CREATE INDEX article_sectionid
   ON toilet.article (sectionid ASC NULLS LAST);
CREATE INDEX article_posted
   ON toilet.article (posted ASC NULLS LAST);
CREATE INDEX honeypot_ip
   ON odyssey.honeypot (ip ASC NULLS LAST);
CREATE INDEX honeypot_expiretime
   ON odyssey.honeypot (expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid
   ON toilet.comment (articleid ASC NULLS LAST);
CREATE INDEX comment_postedtime
   ON toilet.comment (posted ASC NULLS LAST);
CREATE INDEX article_postedtime_sectionid
   ON toilet.article (sectionid ASC NULLS LAST, posted ASC NULLS LAST);
CREATE INDEX honeypot_expiredtime_ip
   ON odyssey.honeypot (ip ASC NULLS LAST, expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid_postedtime
   ON toilet.comment (articleid ASC NULLS LAST, posted ASC NULLS LAST);

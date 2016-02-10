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
CREATE INDEX fileupload_filename ON tools.fileupload (filename ASC NULLS LAST);
CREATE INDEX article_sectionid ON toilet.article (sectionid ASC NULLS LAST);
CREATE INDEX article_posted ON toilet.article (posted ASC NULLS LAST);
CREATE INDEX honeypot_ip ON tools.honeypot (ip ASC NULLS LAST);
CREATE INDEX honeypot_expiretime ON tools.honeypot (expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid ON toilet.comment (articleid ASC NULLS LAST);
CREATE INDEX comment_postedtime ON toilet.comment (posted ASC NULLS LAST);
CREATE INDEX article_postedtime_sectionid ON toilet.article (sectionid ASC NULLS LAST, posted ASC NULLS LAST);
CREATE INDEX honeypot_expiredtime_ip ON tools.honeypot (ip ASC NULLS LAST, expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid_postedtime ON toilet.comment (articleid ASC NULLS LAST, posted ASC NULLS LAST);

ALTER TABLE toilet.article ALTER COLUMN searchindexdata SET DATA TYPE tsvector USING 
   setweight(to_tsvector(COALESCE(new.articletitle,'')), 'A') || setweight(to_tsvector(COALESCE(new.searchabletext,'')), 'D') || setweight(to_tsvector(COALESCE(new.description,'')), 'D');
CREATE INDEX article_search_index ON toilet.article USING gin(searchindexdata);

CREATE OR REPLACE FUNCTION toilet.populate_article_search_index() RETURNS trigger AS $$
begin  
  new.searchindexdata := setweight(to_tsvector(COALESCE(new.articletitle,'')), 'A') || setweight(to_tsvector(COALESCE(new.searchabletext,'')), 'D') || setweight(to_tsvector(COALESCE(new.description,'')), 'D');
  return new;
end $$ LANGUAGE plpgsql;
CREATE TRIGGER index_article BEFORE INSERT OR UPDATE ON toilet.article FOR EACH ROW EXECUTE PROCEDURE toilet.populate_article_search_index();

create table toilet.articlewords as select word from ts_stat('select to_tsvector(''simple'',searchabletext) from toilet.article') order by word;
alter table toilet.articlewords add primary key (word); create index articlewords_word on toilet.articlewords using btree (word);
create index articlewords_gist_index on toilet.articlewords using gist (word gist_trgm_ops);
ANALYZE toilet.articlewords;

--TRUNCATE TABLE toilet.articlewords;
--INSERT INTO toilet.articlewords (SELECT word FROM ts_stat('SELECT to_tsvector(''simple'', searchabletext) FROM toilet.article') ORDER BY word);
--ANALYZE toilet.articlewords;

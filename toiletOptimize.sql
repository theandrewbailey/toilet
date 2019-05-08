CREATE INDEX article_sectionid ON toilet.article (sectionid ASC NULLS LAST);
CREATE INDEX article_posted ON toilet.article (posted ASC NULLS LAST);
CREATE INDEX honeypot_ip ON tools.honeypot (ip ASC NULLS LAST);
CREATE INDEX honeypot_expiretime ON tools.honeypot (expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid ON toilet.comment (articleid ASC NULLS LAST);
CREATE INDEX comment_postedtime ON toilet.comment (posted ASC NULLS LAST);
CREATE INDEX article_postedtime_sectionid ON toilet.article (sectionid ASC NULLS LAST, posted ASC NULLS LAST);
CREATE INDEX honeypot_expiredtime_ip ON tools.honeypot (ip ASC NULLS LAST, expiresatatime ASC NULLS LAST);
CREATE INDEX comment_articleid_postedtime ON toilet.comment (articleid ASC NULLS LAST, posted ASC NULLS LAST);

CREATE MATERIALIZED VIEW toilet.articlesearch AS
select articleid, posted,
regexp_replace(postedmarkdown,'!?\[\"?([^\"|\]]+?)\"?\]\(\S+?(?:\s\"?([^\"|\]]+?)\"?)?\)','\1 \2','g') AS searchabletext,
setweight(to_tsvector(COALESCE(articletitle,'')), 'A') || setweight(to_tsvector(COALESCE(regexp_replace(postedmarkdown,'!?\[\"?([^\"|\]]+?)\"?\]\(\S+?(?:\s\"?([^\"|\]]+?)\"?)?\)','\1 \2','g'),'')), 'D') || setweight(to_tsvector(COALESCE(description,'')), 'D') AS searchindexdata
FROM toilet.article ORDER BY posted;

CREATE INDEX articlesearch_articleid ON toilet.articlesearch (articleid);
CREATE INDEX articlesearch_posted ON toilet.articlesearch (posted);
CREATE INDEX articlesearch_index ON toilet.articlesearch USING gin(searchindexdata);

CREATE MATERIALIZED VIEW toilet.articlewords AS
SELECT word FROM ts_stat('SELECT to_tsvector(''simple'', searchabletext) FROM toilet.articlesearch') ORDER BY word;

CREATE INDEX articlewords_word ON toilet.articlewords USING btree(word);
CREATE INDEX articlewords_gin_index ON toilet.articlewords USING gin(word gin_trgm_ops);

CREATE OR REPLACE FUNCTION toilet.refresh_articlesearch() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
REFRESH MATERIALIZED VIEW toilet.articlesearch;
REFRESH MATERIALIZED VIEW toilet.articlewords;
ANALYZE toilet.articlewords;
END $$;

CREATE OR REPLACE FUNCTION toilet.search_articles(initialterm TEXT) RETURNS SETOF toilet.article LANGUAGE plpgsql AS $$
DECLARE
arts REFCURSOR;
processedterm TEXT;
processed RECORD;
BEGIN
OPEN arts FOR SELECT array_to_string(array_agg(word),' | ') AS word FROM toilet.articlewords WHERE (word % initialterm) = TRUE;
FETCH arts INTO processed;
processedterm := processed.word;
CLOSE arts;
RETURN QUERY SELECT r.* FROM toilet.articlesearch a JOIN toilet.article r on a.articleid=r.articleid, to_tsquery(processedterm) query WHERE query @@ a.searchindexdata ORDER BY ts_rank_cd(a.searchindexdata, query) DESC, r.posted;
END $$;

CREATE MATERIALIZED VIEW tools.filemetadata AS 
SELECT filename, length(filedata) AS datasize, length(gzipdata) AS gzipsize, length(brdata) AS brsize, atime, etag, mimetype, url
FROM tools.fileupload;

CREATE INDEX filemetadata_index ON tools.filemetadata (filename);

CREATE OR REPLACE FUNCTION tools.update_filemetadata() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
REFRESH MATERIALIZED VIEW tools.filemetadata;
ANALYZE tools.filemetadata;
RETURN NEW;
END $$;

CREATE TRIGGER update_filemetadata AFTER INSERT OR UPDATE OR DELETE ON tools.fileupload
EXECUTE PROCEDURE tools.update_filemetadata();

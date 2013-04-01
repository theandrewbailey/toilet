﻿
START TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

CREATE SCHEMA toilet;

SET search_path TO TOILET,"$user",public;

CREATE TABLE toilet.URL
(
	URLId SERIAL NOT NULL,
	location CHARACTER VARYING(1000) NOT NULL,
	pageViews INTEGER,
	CONSTRAINT URL_PK PRIMARY KEY(URLId),
	CONSTRAINT URL_UC UNIQUE(location)
);

CREATE TABLE toilet.Comment
(
	commentId SERIAL NOT NULL,
	posted TIMESTAMP NOT NULL,
	postedText CHARACTER VARYING(65000) NOT NULL,
	postedName CHARACTER VARYING(250) NOT NULL,
	articleId INTEGER NOT NULL,
	isSpam BOOLEAN,
	isApproved BOOLEAN,
	HTTPSessionId INTEGER,
	CONSTRAINT Comment_PK PRIMARY KEY(commentId)
);

CREATE TABLE toilet.Article
(
	articleId SERIAL NOT NULL,
	articleTitle CHARACTER VARYING(250) NOT NULL,
	posted TIMESTAMP NOT NULL,
	postedText CHARACTER VARYING(65000) NOT NULL,
	ETag CHARACTER VARYING(250) NOT NULL,
	modified TIMESTAMP NOT NULL,
	postedName CHARACTER VARYING(250) NOT NULL,
	sectionId INTEGER NOT NULL,
	comments BOOLEAN,
	description CHARACTER VARYING(65000),
	URLId INTEGER,
	CONSTRAINT Article_PK PRIMARY KEY(articleId)
);

CREATE TABLE toilet.HTTPSession
(
	HTTPSessionId SERIAL NOT NULL,
	IP CHARACTER(40) NOT NULL,
	ATime TIMESTAMP NOT NULL,
	JSessionId CHARACTER VARYING(250) NOT NULL,
	userAgent CHARACTER VARYING(1000),
	referrer INTEGER,
	CONSTRAINT HTTPSession_PK PRIMARY KEY(HTTPSessionId)
);

CREATE TABLE toilet.Section
(
	sectionId SERIAL NOT NULL,
	name CHARACTER VARYING(250) NOT NULL,
	CONSTRAINT Section_PK PRIMARY KEY(sectionId)
);

CREATE TABLE toilet.PageRequest
(
	ATime TIMESTAMP NOT NULL,
	HTTPSessionId INTEGER NOT NULL,
	URLId INTEGER NOT NULL,
	CONSTRAINT PageRequest_PK PRIMARY KEY(HTTPSessionId, URLId, ATime)
);

CREATE TABLE toilet.FileUpload
(
	fileUploadId SERIAL NOT NULL,
	fileName CHARACTER VARYING(1000) NOT NULL,
	mimeType CHARACTER VARYING(250) NOT NULL,
	binaryData BYTEA NOT NULL,
	uploaded TIMESTAMP NOT NULL,
	ETag CHARACTER VARYING(250) NOT NULL,
	URLId INTEGER,
	CONSTRAINT FileUpload_PK PRIMARY KEY(fileUploadId),
	CONSTRAINT FileUpload_UC UNIQUE(fileName)
);

CREATE TABLE toilet.ErrorEvent
(
	errorEventId SERIAL NOT NULL,
	title CHARACTER VARYING(65000) NOT NULL,
	description CHARACTER VARYING(65000) NOT NULL,
	ATime TIMESTAMP NOT NULL,
	CONSTRAINT ErrorEvent_PK PRIMARY KEY(errorEventId)
);

ALTER TABLE toilet.Comment ADD CONSTRAINT Comment_FK1 FOREIGN KEY (articleId) REFERENCES toilet.Article (articleId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.Comment ADD CONSTRAINT Comment_FK2 FOREIGN KEY (HTTPSessionId) REFERENCES toilet.HTTPSession (HTTPSessionId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.Article ADD CONSTRAINT Article_FK1 FOREIGN KEY (URLId) REFERENCES toilet.URL (URLId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.Article ADD CONSTRAINT Article_FK2 FOREIGN KEY (sectionId) REFERENCES toilet.Section (sectionId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.HTTPSession ADD CONSTRAINT HTTPSession_FK FOREIGN KEY (referrer) REFERENCES toilet.URL (URLId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.PageRequest ADD CONSTRAINT PageRequest_FK1 FOREIGN KEY (HTTPSessionId) REFERENCES toilet.HTTPSession (HTTPSessionId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.PageRequest ADD CONSTRAINT PageRequest_FK2 FOREIGN KEY (URLId) REFERENCES toilet.URL (URLId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE toilet.FileUpload ADD CONSTRAINT FileUpload_FK FOREIGN KEY (URLId) REFERENCES toilet.URL (URLId) ON DELETE RESTRICT ON UPDATE RESTRICT;

COMMIT WORK;


START TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ WRITE;

CREATE SCHEMA odyssey;

SET search_path TO ODYSSEY,"$user",public;

CREATE DOMAIN odyssey.DayInterval AS CHARACTER(1) CONSTRAINT ValueTypeValueConstraint2 CHECK (VALUE IN ('D', 'W', 'M', 'Q', 'Y'));

CREATE TABLE odyssey.PageRequest
(
	pageRequestId SERIAL NOT NULL,
	ATime TIMESTAMP NOT NULL,
	"method" CHARACTER VARYING(100) NOT NULL,
	requestedPageId INTEGER NOT NULL,
	responseCode INTEGER NOT NULL,
	served INTEGER NOT NULL,
	cameFromPageRequestId INTEGER,
	parameters text,
	referredByPageId INTEGER,
	rendered INTEGER,
	CONSTRAINT PageRequest_PK PRIMARY KEY(pageRequestId)
);

CREATE TABLE odyssey.Page
(
	pageId SERIAL NOT NULL,
	URL CHARACTER VARYING(65000) NOT NULL,
	parameters text,
	CONSTRAINT Page_PK PRIMARY KEY(pageId)
);

CREATE TABLE odyssey.PageDay
(
	"day" DATE NOT NULL,
	dayInterval odyssey.DayInterval NOT NULL,
	pageId INTEGER NOT NULL,
	average REAL NOT NULL,
	hitPercent REAL NOT NULL,
	standardDeviation REAL NOT NULL,
	times BIGINT NOT NULL,
	CONSTRAINT PageDay_PK PRIMARY KEY(pageId, "day", dayInterval)
);

CREATE TABLE odyssey.PageOnPageDay
(
	"day" DATE NOT NULL,
	dayInterval odyssey.DayInterval NOT NULL,
	pageId INTEGER NOT NULL,
	secondaryPage INTEGER NOT NULL,
	linkedFromPercent REAL NOT NULL,
	linkedFromTimes BIGINT NOT NULL,
	linkedToPercent REAL NOT NULL,
	linkedToTimes BIGINT NOT NULL,
	CONSTRAINT PageOnPageDay_PK PRIMARY KEY(secondaryPage, pageId, "day", dayInterval)
);

CREATE TABLE odyssey.ExceptionEvent
(
	exceptionEventId SERIAL NOT NULL,
	ATime TIMESTAMP NOT NULL,
	description text NOT NULL,
	title CHARACTER VARYING(65000) NOT NULL,
	pageRequestId INTEGER,
	CONSTRAINT ExceptionEvent_PK PRIMARY KEY(exceptionEventId)
);

CREATE TABLE odyssey.Honeypot
(
	honeypotId SERIAL NOT NULL,
	expiresAtATime TIMESTAMP NOT NULL,
	ip CHARACTER VARYING(100) NOT NULL,
	startedAtATime TIMESTAMP NOT NULL,
	CONSTRAINT Honeypot_PK PRIMARY KEY(honeypotId)
);

ALTER TABLE odyssey.PageRequest ADD CONSTRAINT PageRequest_FK1 FOREIGN KEY (requestedPageId) REFERENCES odyssey.Page (pageId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.PageRequest ADD CONSTRAINT PageRequest_FK2 FOREIGN KEY (cameFromPageRequestId) REFERENCES odyssey.PageRequest (pageRequestId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.PageRequest ADD CONSTRAINT PageRequest_FK3 FOREIGN KEY (referredByPageId) REFERENCES odyssey.Page (pageId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.PageDay ADD CONSTRAINT PageDay_FK FOREIGN KEY (pageId) REFERENCES odyssey.Page (pageId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.PageOnPageDay ADD CONSTRAINT PageOnPageDay_FK1 FOREIGN KEY (pageId, "day", dayInterval) REFERENCES odyssey.PageDay (pageId, "day", dayInterval) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.PageOnPageDay ADD CONSTRAINT PageOnPageDay_FK2 FOREIGN KEY (secondaryPage) REFERENCES odyssey.Page (pageId) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE odyssey.ExceptionEvent ADD CONSTRAINT ExceptionEvent_FK FOREIGN KEY (pageRequestId) REFERENCES odyssey.PageRequest (pageRequestId) ON DELETE RESTRICT ON UPDATE RESTRICT;

COMMIT WORK;

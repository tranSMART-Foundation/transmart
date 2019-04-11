--
-- Type: TABLE; Owner: I2B2PM; Name: PM_PROJECT_REQUEST
--
 CREATE TABLE "I2B2PM"."PM_PROJECT_REQUEST"
  (	"ID" NUMBER,
"TITLE" VARCHAR2(255 BYTE),
"REQUEST_XML" CLOB NOT NULL ENABLE,
"CHANGE_DATE" DATE,
"ENTRY_DATE" DATE,
"CHANGEBY_CHAR" VARCHAR2(50 BYTE),
"STATUS_CD" VARCHAR2(50 BYTE),
"PROJECT_ID" VARCHAR2(50 BYTE),
"SUBMIT_CHAR" VARCHAR2(50 BYTE),
 PRIMARY KEY ("ID")
 USING INDEX
 TABLESPACE "I2B2"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("REQUEST_XML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
--
-- Type: TRIGGER; Owner: I2B2PM; Name: PM_PROJECT_REQUEST_INC
--
  CREATE OR REPLACE TRIGGER "I2B2PM"."PM_PROJECT_REQUEST_INC"
	BEFORE INSERT
	ON PM_PROJECT_REQUEST
	FOR EACH ROW
	BEGIN
	SELECT PM_PARAMS.NEXTVAL INTO :NEW.ID FROM DUAL;
END;
/
ALTER TRIGGER "I2B2PM"."PM_PROJECT_REQUEST_INC" ENABLE;
--
-- Type: TABLE; Owner: I2B2PM; Name: PM_GLOBAL_PARAMS
--
 CREATE TABLE "I2B2PM"."PM_GLOBAL_PARAMS"
  (	"ID" NUMBER,
"DATATYPE_CD" VARCHAR2(50 BYTE),
"PARAM_NAME_CD" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"PROJECT_PATH" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"VALUE" CLOB,
"CAN_OVERRIDE" NUMBER(1,0),
"CHANGE_DATE" DATE,
"ENTRY_DATE" DATE,
"CHANGEBY_CHAR" VARCHAR2(50 BYTE),
"STATUS_CD" VARCHAR2(50 BYTE),
 PRIMARY KEY ("ID")
 USING INDEX
 TABLESPACE "I2B2"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("VALUE") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
--
-- Type: TRIGGER; Owner: I2B2PM; Name: PM_GLOBAL_PARAMS_INC
--
  CREATE OR REPLACE TRIGGER "I2B2PM"."PM_GLOBAL_PARAMS_INC"
	BEFORE INSERT
	ON PM_GLOBAL_PARAMS
	REFERENCING NEW AS NEW
	FOR EACH ROW
	BEGIN
	SELECT PM_PARAMS.NEXTVAL INTO :NEW.ID FROM DUAL;
END;
/
ALTER TRIGGER "I2B2PM"."PM_GLOBAL_PARAMS_INC" ENABLE;

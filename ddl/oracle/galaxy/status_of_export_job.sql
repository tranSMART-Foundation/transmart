--
-- Type: TABLE; Owner: GALAXY; Name: STATUS_OF_EXPORT_JOB
--
 CREATE TABLE "GALAXY"."STATUS_OF_EXPORT_JOB" 
  (	"JOB_STATUS" VARCHAR2(200 BYTE), 
"LAST_EXPORT_NAME" VARCHAR2(200 BYTE), 
"LAST_EXPORT_TIME" DATE, 
"JOB_NAME_ID" VARCHAR2(200 BYTE), 
"ID" NUMBER(*,0) NOT NULL ENABLE, 
 CONSTRAINT "STATUS_OF_EXPORT_JOB_PK" PRIMARY KEY ("ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "TRANSMART" ;


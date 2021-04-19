--
-- Type: TABLE; Owner: TM_CZ; Name: CZ_JOB_ERROR
--
 CREATE TABLE "TM_CZ"."CZ_JOB_ERROR" 
  (	"JOB_ID" NUMBER(18,0) NOT NULL ENABLE, 
"ERROR_NUMBER" VARCHAR2(300 BYTE), 
"ERROR_MESSAGE" VARCHAR2(1000), 
"ERROR_STACK" VARCHAR2(2000), 
"SEQ_ID" NUMBER(18,0) NOT NULL ENABLE, 
"ERROR_BACKTRACE" VARCHAR2(2000)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


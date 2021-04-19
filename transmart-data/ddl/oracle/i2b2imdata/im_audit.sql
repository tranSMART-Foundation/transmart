--
-- Type: TABLE; Owner: I2B2IMDATA; Name: IM_AUDIT
--
 CREATE TABLE "I2B2IMDATA"."IM_AUDIT"
  (	"QUERY_DATE" DATE DEFAULT SYSDATE NOT NULL ENABLE,
"LCL_SITE" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"LCL_ID" VARCHAR2(200 BYTE) NOT NULL ENABLE,
"USER_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"PROJECT_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"COMMENTS" CLOB
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("COMMENTS") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;

--
-- Type: TABLE; Owner: I2B2WORKDATA; Name: WORKPLACE
--
 CREATE TABLE "I2B2WORKDATA"."WORKPLACE"
  (	"C_NAME" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_USER_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_GROUP_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_SHARE_ID" VARCHAR2(255 BYTE),
"C_INDEX" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_PARENT_INDEX" VARCHAR2(255 BYTE),
"C_VISUALATTRIBUTES" CHAR(3 BYTE) NOT NULL ENABLE,
"C_PROTECTED_ACCESS" CHAR(1 BYTE),
"C_TOOLTIP" VARCHAR2(255 BYTE),
"C_WORK_XML" CLOB,
"C_WORK_XML_SCHEMA" CLOB,
"C_WORK_XML_I2B2_TYPE" VARCHAR2(255 BYTE),
"C_ENTRY_DATE" DATE,
"C_CHANGE_DATE" DATE,
"C_STATUS_CD" CHAR(1 BYTE),
 CONSTRAINT "WORKPLACE_PK" PRIMARY KEY ("C_INDEX")
 USING INDEX
 TABLESPACE "I2B2"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("C_WORK_XML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES )
LOB ("C_WORK_XML_SCHEMA") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
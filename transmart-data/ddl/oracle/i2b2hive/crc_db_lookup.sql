--
-- Type: TABLE; Owner: I2B2HIVE; Name: CRC_DB_LOOKUP
--
 CREATE TABLE "I2B2HIVE"."CRC_DB_LOOKUP"
  (	"C_DOMAIN_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_PROJECT_PATH" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_OWNER_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_DB_FULLSCHEMA" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_DB_DATASOURCE" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_DB_SERVERTYPE" VARCHAR2(255 BYTE) NOT NULL ENABLE,
"C_DB_NICENAME" VARCHAR2(255 BYTE),
"C_DB_TOOLTIP" VARCHAR2(255 BYTE),
"C_COMMENT" CLOB,
"C_ENTRY_DATE" DATE,
"C_CHANGE_DATE" DATE,
"C_STATUS_CD" CHAR(1 BYTE),
 CONSTRAINT "CRC_DB_LOOKUP_PK" PRIMARY KEY ("C_DOMAIN_ID", "C_PROJECT_PATH", "C_OWNER_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "I2B2"
LOB ("C_COMMENT") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;

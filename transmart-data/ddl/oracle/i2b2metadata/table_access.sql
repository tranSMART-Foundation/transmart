--
-- Type: TABLE; Owner: I2B2METADATA; Name: TABLE_ACCESS
--
 CREATE TABLE "I2B2METADATA"."TABLE_ACCESS"
  (	"C_TABLE_CD" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_TABLE_NAME" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_PROTECTED_ACCESS" CHAR(1 BYTE),
"C_HLEVEL" NUMBER(22,0) NOT NULL ENABLE,
"C_FULLNAME" VARCHAR2(700 BYTE) NOT NULL ENABLE,
"C_NAME" VARCHAR2(2000 BYTE) NOT NULL ENABLE,
"C_SYNONYM_CD" CHAR(1 BYTE) NOT NULL ENABLE,
"C_VISUALATTRIBUTES" CHAR(3 BYTE) NOT NULL ENABLE,
"C_TOTALNUM" NUMBER(22,0),
"C_BASECODE" VARCHAR2(50 BYTE),
"C_METADATAXML" CLOB,
"C_FACTTABLECOLUMN" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_DIMTABLENAME" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_COLUMNNAME" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_COLUMNDATATYPE" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"C_OPERATOR" VARCHAR2(10 BYTE) NOT NULL ENABLE,
"C_DIMCODE" VARCHAR2(700 BYTE) NOT NULL ENABLE,
"C_COMMENT" CLOB,
"C_TOOLTIP" VARCHAR2(900 BYTE),
"C_ENTRY_DATE" DATE,
"C_CHANGE_DATE" DATE,
"C_STATUS_CD" CHAR(1 BYTE),
"VALUETYPE_CD" VARCHAR2(50 BYTE),
"C_ONTOLOGY_PROTECTION" CLOB,
 CONSTRAINT "TABLE_ACCESS_PK" PRIMARY KEY ("C_TABLE_CD") --not in i2b2
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "I2B2"
LOB ("C_METADATAXML") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES )
LOB ("C_COMMENT") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;

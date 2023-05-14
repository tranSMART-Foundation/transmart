--
-- Type: TABLE; Owner: I2B2PM; Name: PM_PROJECT_DATA
--
 CREATE TABLE "I2B2PM"."PM_PROJECT_DATA"
  (	"PROJECT_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"PROJECT_NAME" VARCHAR2(255 BYTE),
"PROJECT_WIKI" VARCHAR2(255 BYTE),
"PROJECT_KEY" VARCHAR2(255 BYTE),
"PROJECT_PATH" VARCHAR2(255 BYTE),
"PROJECT_DESCRIPTION" VARCHAR2(2000 BYTE),
"CHANGE_DATE" DATE,
"ENTRY_DATE" DATE,
"CHANGEBY_CHAR" VARCHAR2(50 BYTE),
"STATUS_CD" VARCHAR2(50 BYTE),
 CONSTRAINT "PM_PROJECT_DATA_PK" PRIMARY KEY ("PROJECT_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "I2B2" ;

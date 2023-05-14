--
-- Type: TABLE; Owner: I2B2PM; Name: PM_USER_DATA
--
 CREATE TABLE "I2B2PM"."PM_USER_DATA"
  (	"USER_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"FULL_NAME" VARCHAR2(255 BYTE),
"PASSWORD" VARCHAR2(255 BYTE),
"EMAIL" VARCHAR2(255 BYTE),
"PROJECT_PATH" VARCHAR2(255 BYTE),
"CHANGE_DATE" DATE,
"ENTRY_DATE" DATE,
"CHANGEBY_CHAR" VARCHAR2(50 BYTE),
"STATUS_CD" VARCHAR2(50 BYTE),
 CONSTRAINT "PM_USER_DATA_PK" PRIMARY KEY ("USER_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "I2B2" ;

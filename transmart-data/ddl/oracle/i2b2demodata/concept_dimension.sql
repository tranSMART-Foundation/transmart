--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: CONCEPT_DIMENSION
--
 CREATE TABLE "I2B2DEMODATA"."CONCEPT_DIMENSION"
     (	"CONCEPT_PATH" VARCHAR2(700 BYTE) NOT NULL ENABLE,
"CONCEPT_CD" VARCHAR2(50 BYTE),
"NAME_CHAR" VARCHAR2(2000 BYTE),
"CONCEPT_BLOB" CLOB,
"UPDATE_DATE" DATE,
"DOWNLOAD_DATE" DATE,
"IMPORT_DATE" DATE,
"SOURCESYSTEM_CD" VARCHAR2(50 BYTE),
"UPLOAD_ID" NUMBER(38,0),
 CONSTRAINT "CONCEPT_DIMENSION_PK" PRIMARY KEY ("CONCEPT_PATH")
  ) SEGMENT CREATION IMMEDIATE
COMPRESS BASIC NOLOGGING
 TABLESPACE "I2B2"
LOB ("CONCEPT_BLOB") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: CD_UPLOADID_IDX
--
CREATE INDEX "I2B2DEMODATA"."CD_UPLOADID_IDX" ON "I2B2DEMODATA"."CONCEPT_DIMENSION" ("UPLOAD_ID")
TABLESPACE "I2B2_INDEX" ;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: CD_CONCEPTCD_IDX
--
-- not in i2b2
CREATE INDEX "I2B2DEMODATA"."CD_CONCEPTCD_IDX" ON "I2B2DEMODATA"."CONCEPT_DIMENSION" ("CONCEPT_CD")
TABLESPACE "I2B2_INDEX" ;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: CD_PATH_CD_IDX
--
-- not in i2b2
CREATE INDEX "I2B2DEMODATA"."CD_PATH_CD_IDX" ON "I2B2DEMODATA"."CONCEPT_DIMENSION" ("CONCEPT_PATH", "CONCEPT_CD")
TABLESPACE "I2B2_INDEX" ;

--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TRG_CONCEPT_DIMENSION_CD
--
  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TRG_CONCEPT_DIMENSION_CD"
	 before insert on "CONCEPT_DIMENSION"
	 for each row begin
	 if inserting then
	 if :NEW."CONCEPT_CD" is null then
	 select CONCEPT_ID.nextval into :NEW."CONCEPT_CD" from dual;
	 end if;
	 end if;
	 end;
/
ALTER TRIGGER "I2B2DEMODATA"."TRG_CONCEPT_DIMENSION_CD" ENABLE;

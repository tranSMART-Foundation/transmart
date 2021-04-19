--
-- Type: TABLE; Owner: BIOMART; Name: BIO_EXPERIMENT
--
 CREATE TABLE "BIOMART"."BIO_EXPERIMENT" 
  (	"BIO_EXPERIMENT_ID" NUMBER(18,0) NOT NULL ENABLE, 
"BIO_EXPERIMENT_TYPE" VARCHAR2(200 BYTE), 
"TITLE" VARCHAR2(1000 BYTE), 
"DESCRIPTION" VARCHAR2(4000 BYTE), 
"DESIGN" VARCHAR2(2000 BYTE), 
"START_DATE" DATE, 
"COMPLETION_DATE" DATE, 
"PRIMARY_INVESTIGATOR" VARCHAR2(400 BYTE), 
"CONTACT_FIELD" VARCHAR2(400 BYTE), 
"ETL_ID" VARCHAR2(100 BYTE), 
"STATUS" VARCHAR2(100 BYTE), 
"OVERALL_DESIGN" VARCHAR2(2000 BYTE), 
"ACCESSION" VARCHAR2(100 BYTE), 
"ENTRYDT" DATE, 
"UPDATED" DATE, 
"INSTITUTION" VARCHAR2(400 BYTE), 
"COUNTRY" VARCHAR2(1000 BYTE), 
"BIOMARKER_TYPE" VARCHAR2(255 BYTE), 
"TARGET" VARCHAR2(255 BYTE), 
"ACCESS_TYPE" VARCHAR2(100 BYTE), 
 CONSTRAINT "EXPERIMENTDIM_PK" PRIMARY KEY ("BIO_EXPERIMENT_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: INDEX; Owner: BIOMART; Name: BIO_EXP_ACEN_IDX
--
CREATE INDEX "BIOMART"."BIO_EXP_ACEN_IDX" ON "BIOMART"."BIO_EXPERIMENT" ("ACCESSION")
TABLESPACE "INDX" 
PARALLEL 4 ;
--
-- Type: INDEX; Owner: BIOMART; Name: BIO_EXP_TYPE_IDX
--
CREATE INDEX "BIOMART"."BIO_EXP_TYPE_IDX" ON "BIOMART"."BIO_EXPERIMENT" ("BIO_EXPERIMENT_TYPE")
TABLESPACE "INDX" 
PARALLEL 4 ;
--
-- Type: TRIGGER; Owner: BIOMART; Name: TRG_BIO_EXPERIMENT_ID
--
  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_EXPERIMENT_ID"
before insert on "BIO_EXPERIMENT"
  for each row begin
    if inserting then
      if :NEW."BIO_EXPERIMENT_ID" is null then
        select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_EXPERIMENT_ID" from dual;
      end if;
    end if;
  end;
/

ALTER TRIGGER "BIOMART"."TRG_BIO_EXPERIMENT_ID" ENABLE;
 
--
-- Type: TRIGGER; Owner: BIOMART; Name: TRG_BIO_EXPERIMENT_UID
--
  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_EXPERIMENT_UID" after insert on "BIO_EXPERIMENT"    
for each row
DECLARE
  rec_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO rec_count 
  FROM bio_data_uid 
  WHERE bio_data_id = :new.BIO_EXPERIMENT_ID;
  
  if rec_count = 0 then
    insert into biomart.bio_data_uid (bio_data_id, unique_id, bio_data_type)
    values (:NEW."BIO_EXPERIMENT_ID", BIO_EXPERIMENT_UID(:NEW."ACCESSION"), 'BIO_EXPERIMENT');
  end if;
end;
/
ALTER TRIGGER "BIOMART"."TRG_BIO_EXPERIMENT_UID" ENABLE;
 

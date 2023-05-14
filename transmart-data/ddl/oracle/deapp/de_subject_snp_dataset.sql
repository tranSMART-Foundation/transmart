--
-- Type: TABLE; Owner: DEAPP; Name: DE_SUBJECT_SNP_DATASET
--
 CREATE TABLE "DEAPP"."DE_SUBJECT_SNP_DATASET"
  (	"SUBJECT_SNP_DATASET_ID" NUMBER(22,0) NOT NULL ENABLE,
"DATASET_NAME" VARCHAR2(255 BYTE),
"CONCEPT_CD" VARCHAR2(255 BYTE),
"PLATFORM_NAME" VARCHAR2(255 BYTE),
"TRIAL_NAME" VARCHAR2(100 BYTE),
"PATIENT_NUM" NUMBER(38,0),
"TIMEPOINT" VARCHAR2(250 BYTE),
"SUBJECT_ID" VARCHAR2(100 BYTE),
"SAMPLE_TYPE" VARCHAR2(100 BYTE),
"PAIRED_DATASET_ID" NUMBER(22,0),
"PATIENT_GENDER" VARCHAR2(1 BYTE),
 CONSTRAINT "DE_SUBJECT_SNP_DATASET_PK" PRIMARY KEY ("SUBJECT_SNP_DATASET_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;
--
-- Type: TRIGGER; Owner: DEAPP; Name: TRG_DE_SUBJECT_SNP_DATASET_ID
--
  CREATE OR REPLACE TRIGGER "DEAPP"."TRG_DE_SUBJECT_SNP_DATASET_ID"
before insert on DE_SUBJECT_SNP_DATASET
for each row
begin
   if inserting then
      if :NEW.SUBJECT_SNP_DATASET_ID is null then
         select SEQ_DATA_ID.nextval into :NEW.SUBJECT_SNP_DATASET_ID from dual;
      end if;
  end if;
end;
/
ALTER TRIGGER "DEAPP"."TRG_DE_SUBJECT_SNP_DATASET_ID" ENABLE;

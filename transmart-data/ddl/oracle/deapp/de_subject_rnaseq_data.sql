--
-- Type: TABLE; Owner: DEAPP; Name: DE_SUBJECT_RNASEQ_DATA
--
 CREATE TABLE "DEAPP"."DE_SUBJECT_RNASEQ_DATA" 
  (	"TRIAL_SOURCE" VARCHAR2(200 BYTE), 
"TRIAL_NAME" VARCHAR2(100 BYTE), 
"REGION_ID" NUMBER NOT NULL ENABLE, 
"ASSAY_ID" NUMBER NOT NULL ENABLE, 
"PATIENT_ID" NUMBER, 
"READCOUNT" NUMBER, 
"NORMALIZED_READCOUNT" DOUBLE PRECISION,
"LOG_NORMALIZED_READCOUNT" DOUBLE PRECISION,
"ZSCORE" DOUBLE PRECISION,
"PARTITION_ID" NUMBER,
 CONSTRAINT "DE_SUBJECT_RNASEQ_DATA_PKEY" PRIMARY KEY ("ASSAY_ID", "REGION_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: INDEX; Owner: DEAPP; Name: DE_SUBJECT_RNASEQ_DATA_REGION
--
CREATE INDEX "DEAPP"."DE_SUBJECT_RNASEQ_DATA_REGION" ON "DEAPP"."DE_SUBJECT_RNASEQ_DATA" ("REGION_ID")
TABLESPACE "INDX" ;

--
-- Type: INDEX; Owner: DEAPP; Name: DE_SUBJECT_RNASEQ_DATA_PATIENT
--
CREATE INDEX "DEAPP"."DE_SUBJECT_RNASEQ_DATA_PATIENT" ON "DEAPP"."DE_SUBJECT_RNASEQ_DATA" ("PATIENT_ID")
TABLESPACE "INDX" ;

--
-- Type: REF_CONSTRAINT; Owner: DEAPP; Name: DE_SUBJ_RNASEQ_REGION_ID_FKEY
--
ALTER TABLE "DEAPP"."DE_SUBJECT_RNASEQ_DATA" ADD CONSTRAINT "DE_SUBJ_RNASEQ_REGION_ID_FKEY" FOREIGN KEY ("REGION_ID")
 REFERENCES "DEAPP"."DE_CHROMOSOMAL_REGION" ("REGION_ID") ENABLE;


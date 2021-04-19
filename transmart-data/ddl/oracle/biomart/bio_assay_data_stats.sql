--
-- Type: TABLE; Owner: BIOMART; Name: BIO_ASSAY_DATA_STATS
--
 CREATE TABLE "BIOMART"."BIO_ASSAY_DATA_STATS" 
  (	"BIO_ASSAY_DATA_STATS_ID" NUMBER(18,0) NOT NULL ENABLE, 
"BIO_SAMPLE_COUNT" NUMBER(18,0), 
"QUARTILE_1" NUMBER(18,5), 
"QUARTILE_2" NUMBER(18,5), 
"QUARTILE_3" NUMBER(18,5), 
"MAX_VALUE" NUMBER(18,5), 
"MIN_VALUE" NUMBER(18,5), 
"BIO_SAMPLE_ID" NUMBER(18,0), 
"FEATURE_GROUP_NAME" VARCHAR2(120), 
"VALUE_NORMALIZE_METHOD" VARCHAR2(50), 
"BIO_EXPERIMENT_ID" NUMBER(18,0), 
"MEAN_VALUE" NUMBER(18,5), 
"STD_DEV_VALUE" NUMBER(18,5), 
"BIO_ASSAY_DATASET_ID" NUMBER(18,0), 
"BIO_ASSAY_FEATURE_GROUP_ID" NUMBER(18,0) NOT NULL ENABLE, 
 CONSTRAINT "BIO_ASY_DT_STATS_S_PK" PRIMARY KEY ("BIO_ASSAY_DATA_STATS_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_ASY_DT_FG_FK
--
ALTER TABLE "BIOMART"."BIO_ASSAY_DATA_STATS" ADD CONSTRAINT "BIO_ASY_DT_FG_FK" FOREIGN KEY ("BIO_ASSAY_FEATURE_GROUP_ID")
 REFERENCES "BIOMART"."BIO_ASSAY_FEATURE_GROUP" ("BIO_ASSAY_FEATURE_GROUP_ID") ENABLE;

--
-- Type: INDEX; Owner: BIOMART; Name: BIO_A_D_S_FGI_S_IDX
--
CREATE INDEX "BIOMART"."BIO_A_D_S_FGI_S_IDX" ON "BIOMART"."BIO_ASSAY_DATA_STATS" ("BIO_ASSAY_FEATURE_GROUP_ID", "BIO_ASSAY_DATA_STATS_ID")
TABLESPACE "INDX" 
PARALLEL 3 ;

--
-- Type: INDEX; Owner: BIOMART; Name: BIO_A_D_S_EXP__S_IDX
--
CREATE INDEX "BIOMART"."BIO_A_D_S_EXP__S_IDX" ON "BIOMART"."BIO_ASSAY_DATA_STATS" ("BIO_EXPERIMENT_ID")
TABLESPACE "INDX" 
PARALLEL 4 ;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_ASY_DT_STATS_DS_S_FK
--
ALTER TABLE "BIOMART"."BIO_ASSAY_DATA_STATS" ADD CONSTRAINT "BIO_ASY_DT_STATS_DS_S_FK" FOREIGN KEY ("BIO_ASSAY_DATASET_ID")
 REFERENCES "BIOMART"."BIO_ASSAY_DATASET" ("BIO_ASSAY_DATASET_ID") ENABLE;

--
-- Type: INDEX; Owner: BIOMART; Name: BIO_A_D_S_F_G_S_IDX
--
CREATE INDEX "BIOMART"."BIO_A_D_S_F_G_S_IDX" ON "BIOMART"."BIO_ASSAY_DATA_STATS" ("FEATURE_GROUP_NAME", "BIO_ASSAY_DATA_STATS_ID")
TABLESPACE "INDX" 
PARALLEL 4 ;

--
-- Type: INDEX; Owner: BIOMART; Name: BIO_A_D_S_DS__S_IDX
--
CREATE INDEX "BIOMART"."BIO_A_D_S_DS__S_IDX" ON "BIOMART"."BIO_ASSAY_DATA_STATS" ("BIO_ASSAY_DATASET_ID")
TABLESPACE "INDX" 
PARALLEL 4 ;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_ASY_DT_STATS_SMP_S_FK
--
ALTER TABLE "BIOMART"."BIO_ASSAY_DATA_STATS" ADD CONSTRAINT "BIO_ASY_DT_STATS_SMP_S_FK" FOREIGN KEY ("BIO_SAMPLE_ID")
 REFERENCES "BIOMART"."BIO_SAMPLE" ("BIO_SAMPLE_ID") ENABLE;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_ASY_DT_STAT_EXP_S_FK
--
ALTER TABLE "BIOMART"."BIO_ASSAY_DATA_STATS" ADD CONSTRAINT "BIO_ASY_DT_STAT_EXP_S_FK" FOREIGN KEY ("BIO_EXPERIMENT_ID")
 REFERENCES "BIOMART"."BIO_EXPERIMENT" ("BIO_EXPERIMENT_ID") ENABLE;


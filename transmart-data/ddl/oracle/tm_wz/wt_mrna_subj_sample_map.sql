--
-- Type: TABLE; Owner: TM_WZ; Name: WT_MRNA_SUBJ_SAMPLE_MAP
--
 CREATE TABLE "TM_WZ"."WT_MRNA_SUBJ_SAMPLE_MAP" 
  (	"TRIAL_NAME" VARCHAR2(100 BYTE), 
"SITE_ID" VARCHAR2(100 BYTE), 
"SUBJECT_ID" VARCHAR2(100 BYTE), 
"SAMPLE_CD" VARCHAR2(100 BYTE), 
"PLATFORM" VARCHAR2(100 BYTE), 
"TISSUE_TYPE" VARCHAR2(100 BYTE), 
"ATTRIBUTE_1" VARCHAR2(256 BYTE), 
"ATTRIBUTE_2" VARCHAR2(200 BYTE), 
"CATEGORY_CD" VARCHAR2(2000 BYTE)
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "TRANSMART" ;


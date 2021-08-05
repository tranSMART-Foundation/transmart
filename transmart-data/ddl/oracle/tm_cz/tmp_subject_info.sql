--
-- Type: TABLE; Owner: TM_CZ; Name: TMP_SUBJECT_INFO
--
 CREATE TABLE "TM_CZ"."TMP_SUBJECT_INFO" 
  (	"USUBJID" VARCHAR2(100 BYTE), 
"AGE_IN_YEARS_NUM" NUMBER(3,0), 
"SEX_CD" VARCHAR2(50 BYTE), 
"RACE_CD" VARCHAR2(100 BYTE)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: INDEX; Owner: TM_CZ; Name: TMP_SUBJ_USUBJID_IDX
--
CREATE INDEX "TM_CZ"."TMP_SUBJ_USUBJID_IDX" ON "TM_CZ"."TMP_SUBJECT_INFO" ("USUBJID")
TABLESPACE "INDX" ;


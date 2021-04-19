--
-- Type: TABLE; Owner: BIOMART; Name: FACETED_SEARCH
--
 CREATE TABLE "BIOMART"."FACETED_SEARCH" 
  (	"ANALYSIS_ID" NUMBER(18,0), 
"STUDY" NUMBER(18,0), 
"STUDY_ID" NUMBER(18,0), 
"DISEASE" VARCHAR2(510), 
"ANALYSES" VARCHAR2(200), 
"DATA_TYPE" VARCHAR2(50 BYTE), 
"PLATFORM" VARCHAR2(20 BYTE), 
"OBSERVATION" VARCHAR2(200), 
"FACET_ID" NUMBER(10,0), 
 PRIMARY KEY ("FACET_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
NOCOMPRESS NOLOGGING
 TABLESPACE "TRANSMART" ;

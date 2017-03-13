--
-- Type: TABLE; Owner: BIOMART; Name: BIO_ASY_ANALYSIS_DATA_EXT
--
 CREATE TABLE "BIOMART"."BIO_ASY_ANALYSIS_DATA_EXT" 
  (	"BIO_ASY_ANALYSIS_DATA_ID" NUMBER(22,0) NOT NULL ENABLE,
"EXT_TYPE" VARCHAR2(20 BYTE) NOT NULL ENABLE,
"EXT_DATA" VARCHAR2(4000 BYTE) NOT NULL ENABLE,
 CONSTRAINT "BIO_ASY_ANALYSIS_DATA_ID_PK" PRIMARY KEY ("BIO_ASY_ANALYSIS_DATA_ID")
 USING INDEX
 TABLESPACE "INDX" ENABLE
 ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_ASY_ANALYSIS_DATA_EXT_FK
--
ALTER TABLE "BIOMART"."BIO_ASY_ANALYSIS_DATA_EXT" ADD CONSTRAINT "BIO_ASY_ANALYSIS_DATA_EXT_FK" FOREIGN KEY ("BIO_ASY_ANALYSIS_DATA_ID")
 REFERENCES "BIOMART"."BIO_ASSAY_ANALYSIS_GWAS" ("BIO_ASY_ANALYSIS_GWAS_ID") ENABLE;

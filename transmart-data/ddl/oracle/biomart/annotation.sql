--
-- Type: TABLE; Owner: BIOMART; Name: ANNOTATION
--
 CREATE TABLE "BIOMART"."ANNOTATION" 
  (	"PLATFORM" VARCHAR2(50 BYTE), 
"PROBESET" VARCHAR2(50 BYTE), 
"GENE_DESCR" VARCHAR2(4000 BYTE), 
"GENE_SYMBOL" VARCHAR2(4000 BYTE), 
"GENE_ID" VARCHAR2(50 BYTE), 
"ORGANISM" VARCHAR2(200 BYTE), 
"ID" NUMBER(10,0) NOT NULL,
 CONSTRAINT "ANNOTATION_PK" PRIMARY KEY ("ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

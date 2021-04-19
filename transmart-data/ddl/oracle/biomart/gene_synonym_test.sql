--
-- Type: TABLE; Owner: BIOMART; Name: GENE_SYNONYM_TEST
--
 CREATE TABLE "BIOMART"."GENE_SYNONYM_TEST" 
  (	"TAX_ID" NUMBER(10,0), 
"GENE_ID" NUMBER(20,0), 
"GENE_SYMBOL" VARCHAR2(100 BYTE), 
"GENE_SYNONYM" VARCHAR2(100 BYTE)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


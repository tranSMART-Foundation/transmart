--
-- Type: TABLE; Owner: TM_CZ; Name: DE_MRNA_ANNOTATION_RELEASE
--
 CREATE TABLE "TM_CZ"."DE_MRNA_ANNOTATION_RELEASE" 
  (	"GPL_ID" VARCHAR2(50 BYTE), 
"PROBE_ID" VARCHAR2(100 BYTE), 
"GENE_SYMBOL" VARCHAR2(100 BYTE), 
"GENE_ID" VARCHAR2(100 BYTE), 
"PROBESET_ID" NUMBER(22,0)
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


--
-- Type: TABLE; Owner: DEAPP; Name: KEGG_DATA
--
 CREATE TABLE "DEAPP"."KEGG_DATA" 
  (	"PATHWAY" VARCHAR2(100 BYTE), 
"GENE_ID" VARCHAR2(100 BYTE), 
"GENE" VARCHAR2(100 BYTE)
  ) SEGMENT CREATION IMMEDIATE
NOCOMPRESS LOGGING
 TABLESPACE "TRANSMART" ;

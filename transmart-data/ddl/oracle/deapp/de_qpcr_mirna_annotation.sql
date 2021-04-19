--
-- Type: TABLE; Owner: DEAPP; Name: DE_QPCR_MIRNA_ANNOTATION
--
 CREATE TABLE "DEAPP"."DE_QPCR_MIRNA_ANNOTATION"
  (	"ID_REF" VARCHAR2(100 BYTE),
"PROBE_ID" VARCHAR2(100 BYTE),
"MIRNA_SYMBOL" VARCHAR2(100 BYTE),
"MIRNA_ID" VARCHAR2(100 BYTE),
"PROBESET_ID" NUMBER(22,0) NOT NULL ENABLE,
"ORGANISM" VARCHAR2(100 BYTE),
"GPL_ID" VARCHAR2(50 BYTE),
 CONSTRAINT "DE_QPCR_MIRNA_ANNOTATION_PKEY" PRIMARY KEY ("PROBESET_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
NOCOMPRESS LOGGING
 TABLESPACE "TRANSMART" ;

--
-- Type: TABLE; Owner: TM_LZ; Name: WT_SNP_NODES
--
 CREATE TABLE "TM_LZ"."WT_SNP_NODES" 
  (	"LEAF_NODE" VARCHAR2(2000 BYTE), 
"CATEGORY_CD" VARCHAR2(500 BYTE), 
"PLATFORM" VARCHAR2(500 BYTE), 
"SAMPLE_TYPE" VARCHAR2(100 BYTE), 
"TIMEPOINT" VARCHAR2(250 BYTE), 
"NODE_TYPE" VARCHAR2(100 BYTE)
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "TRANSMART" ;


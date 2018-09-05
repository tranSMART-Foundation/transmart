-- used in view vw_faceted_search_disease
-- with hard-coded table names

--
-- Type: TABLE; Owner: BIOMART; Name: MESH
--
 CREATE TABLE "BIOMART"."MESH" 
  (	"UI" VARCHAR2(20 BYTE), 
"MH" VARCHAR2(200 BYTE), 
"MN" VARCHAR2(200 BYTE), 
 PRIMARY KEY ("UI")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


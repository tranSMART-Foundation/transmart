--
-- Type: TABLE; Owner: I2B2IMDATA; Name: IM_MPI_MAPPING
--
 CREATE TABLE "I2B2IMDATA"."IM_MPI_MAPPING"
  (	"GLOBAL_ID" VARCHAR2(200 BYTE) NOT NULL ENABLE,
"LCL_SITE" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"LCL_ID" VARCHAR2(200 BYTE) NOT NULL ENABLE,
"LCL_STATUS" VARCHAR2(50 BYTE),
"UPDATE_DATE" DATE NOT NULL ENABLE,
"DOWNLOAD_DATE" DATE,
"IMPORT_DATE" DATE,
"SOURCESYSTEM_CD" VARCHAR2(50 BYTE),
"UPLOAD_ID" NUMBER(38,0),
 CONSTRAINT "IM_MPI_MAPPING_PK" PRIMARY KEY ("LCL_SITE", "LCL_ID", "UPDATE_DATE")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "I2B2" ;

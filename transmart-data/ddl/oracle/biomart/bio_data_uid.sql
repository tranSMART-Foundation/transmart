--
-- Type: TABLE; Owner: BIOMART; Name: BIO_DATA_UID
--
 CREATE TABLE "BIOMART"."BIO_DATA_UID" 
  (	"BIO_DATA_ID" NUMBER(18,0) NOT NULL ENABLE, 
"UNIQUE_ID" VARCHAR2(300) NOT NULL ENABLE, 
"BIO_DATA_TYPE" VARCHAR2(100) NOT NULL ENABLE, 
 CONSTRAINT "BIO_DATA_UID_PK" PRIMARY KEY ("BIO_DATA_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE, 
 CONSTRAINT "BIO_DATA_UID_UK" UNIQUE ("UNIQUE_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;


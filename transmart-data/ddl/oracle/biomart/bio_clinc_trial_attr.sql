--
-- Type: TABLE; Owner: BIOMART; Name: BIO_CLINC_TRIAL_ATTR
--
 CREATE TABLE "BIOMART"."BIO_CLINC_TRIAL_ATTR" 
  (	"BIO_CLINC_TRIAL_ATTR_ID" NUMBER(18,0) NOT NULL ENABLE, 
"PROPERTY_CODE" VARCHAR2(200) NOT NULL ENABLE, 
"PROPERTY_VALUE" VARCHAR2(200), 
"BIO_EXPERIMENT_ID" NUMBER(18,0) NOT NULL ENABLE, 
 CONSTRAINT "BIO_CLINC_TRIAL_ATTR_PK" PRIMARY KEY ("BIO_CLINC_TRIAL_ATTR_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: REF_CONSTRAINT; Owner: BIOMART; Name: BIO_CLINICAL_TRIAL_PROPERTY_BI
--
ALTER TABLE "BIOMART"."BIO_CLINC_TRIAL_ATTR" ADD CONSTRAINT "BIO_CLINICAL_TRIAL_PROPERTY_BI" FOREIGN KEY ("BIO_EXPERIMENT_ID")
 REFERENCES "BIOMART"."BIO_CLINICAL_TRIAL" ("BIO_EXPERIMENT_ID") ENABLE;

--
-- Type: TRIGGER; Owner: BIOMART; Name: TRG_BIO_CLN_TRL_ATTR_ID
--
  CREATE OR REPLACE TRIGGER "BIOMART"."TRG_BIO_CLN_TRL_ATTR_ID"
before insert on "BIO_CLINC_TRIAL_ATTR"
  for each row begin
    if inserting then
      if :NEW."BIO_CLINC_TRIAL_ATTR_ID" is null then
        select SEQ_BIO_DATA_ID.nextval into :NEW."BIO_CLINC_TRIAL_ATTR_ID" from dual;
      end if;
    end if;
  end;
/
ALTER TRIGGER "BIOMART"."TRG_BIO_CLN_TRL_ATTR_ID" ENABLE;
 

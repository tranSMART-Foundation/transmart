--
-- Type: TABLE; Owner: SEARCHAPP; Name: SEARCH_REQUIRED_UPLOAD_FIELD
--

 CREATE TABLE "SEARCHAPP"."SEARCH_REQUIRED_UPLOAD_FIELD" 
  (	"REQUIRED_UPLOAD_FIELD_ID" NUMBER(22,0) NOT NULL ENABLE, 
"TYPE" VARCHAR2(50), 
"FIELD" VARCHAR2(50),
 CONSTRAINT "SEARCH_REQ_UPLOAD_FIELD_PK" PRIMARY KEY ("REQUIRED_UPLOAD_FIELD_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE 
 TABLESPACE "TRANSMART" ;

--
-- Type: SEQUENCE; Owner: SEARCHAPP; Name: SEQ_REQ_UPLOAD_FIELD_ID
--
CREATE SEQUENCE  "SEARCHAPP"."SEQ_REQ_UPLOAD_FIELD_ID"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: TRIGGER; Owner: SEARCHAPP; Name: TRG_SRCH_REQ_UP_FIELD_ID
--

  CREATE OR REPLACE TRIGGER "SEARCHAPP"."TRG_SRCH_REQ_UP_FIELD_ID"
before insert on "SEARCHAPP"."SEARCH_REQUIRED_UPLOAD_FIELD"
  for each row begin
    if inserting then
      if :NEW."REQUIRED_UPLOAD_FIELD_ID" is null then
        select SEQ_REQ_UPLOAD_FIELD_ID.nextval into :NEW."REQUIRED_UPLOAD_FIELD_ID" from dual;
      end if;
    end if;
  end;
/
ALTER TRIGGER "SEARCHAPP"."TRG_SRCH_REQ_UP_FIELD_ID" ENABLE;


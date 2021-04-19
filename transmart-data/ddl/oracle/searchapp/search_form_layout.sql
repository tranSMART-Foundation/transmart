--
-- Type: TABLE; Owner: SEARCHAPP; Name: SEARCH_FORM_LAYOUT
--
 CREATE TABLE "SEARCHAPP"."SEARCH_FORM_LAYOUT"
  (	"FORM_LAYOUT_ID" NUMBER(22,0) NOT NULL ENABLE,
"FORM_KEY" VARCHAR2(50),
"FORM_COLUMN" VARCHAR2(50),
"DISPLAY_NAME" VARCHAR2(50),
"DATA_TYPE" VARCHAR2(50),
"SEQUENCE" NUMBER(22,0),
"DISPLAY" CHAR(1 BYTE),
 CONSTRAINT "SEARCH_FORM_LAYOUT_PK" PRIMARY KEY ("FORM_LAYOUT_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;
--
-- Type: SEQUENCE; Owner: SEARCHAPP; Name: SEQ_SEARCH_FORM_LAYOUT_ID
--
CREATE SEQUENCE  "SEARCHAPP"."SEQ_SEARCH_FORM_LAYOUT_ID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 41 CACHE 20 NOORDER  NOCYCLE ;
--
-- Type: TRIGGER; Owner: SEARCHAPP; Name: TRG_SEARCH_FORM_LAYOUT_ID
--
  CREATE OR REPLACE TRIGGER "SEARCHAPP"."TRG_SEARCH_FORM_LAYOUT_ID"
before insert on "SEARCHAPP"."SEARCH_FORM_LAYOUT"
  for each row begin
    if inserting then
      if :NEW."FORM_LAYOUT_ID" is null then
        select SEQ_SEARCH_FORM_LAYOUT_ID.nextval INTO :NEW."FORM_LAYOUT_ID" FROM dual;
      end if;
    end if;
  end;
/
ALTER TRIGGER "SEARCHAPP"."TRG_SEARCH_FORM_LAYOUT_ID" ENABLE;

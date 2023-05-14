--
-- Type: TABLE; Owner: SEARCHAPP; Name: XNAT_SUBJECT
--
 CREATE TABLE "SEARCHAPP"."XNAT_SUBJECT"
  (	"TSMART_SUBJECTID" VARCHAR2(100 BYTE),
"XNAT_SUBJECTID" VARCHAR2(100 BYTE),
"XNAT_PROJECT" VARCHAR2(80 BYTE),
"ID" NUMBER(10,0) NOT NULL ENABLE,
 CONSTRAINT "XNAT_SUBJECT_PK" PRIMARY KEY ("ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;
--
-- Type: TRIGGER; Owner: SEARCHAPP; Name: TRG_XNAT_SUBJECT_ID
--
  CREATE OR REPLACE TRIGGER "SEARCHAPP"."TRG_XNAT_SUBJECT_ID"
BEFORE INSERT ON "SEARCHAPP"."XNAT_SUBJECT"
FOR EACH ROW BEGIN
	  if inserting then
		if :NEW."ID" is null then
SELECT SEQ_SEARCH_DATA_ID.nextval
INTO :NEW."ID"
FROM dual;
end if;
end if;
END;
/
ALTER TRIGGER "SEARCHAPP"."TRG_XNAT_SUBJECT_ID" ENABLE;

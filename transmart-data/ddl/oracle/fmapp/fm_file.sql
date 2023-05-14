--
-- Type: TABLE; Owner: FMAPP; Name: FM_FILE
--
 CREATE TABLE "FMAPP"."FM_FILE" 
  (	"FILE_ID" NUMBER(18,0) NOT NULL ENABLE, 
"DISPLAY_NAME" VARCHAR2(1000) NOT NULL ENABLE, 
"ORIGINAL_NAME" VARCHAR2(1000) NOT NULL ENABLE, 
"FILE_VERSION" NUMBER(18,0), 
"FILE_TYPE" VARCHAR2(100), 
"FILE_SIZE" NUMBER(18,0), 
"FILESTORE_LOCATION" VARCHAR2(1000), 
"FILESTORE_NAME" VARCHAR2(1000), 
"LINK_URL" VARCHAR2(1000), 
"ACTIVE_IND" CHAR(1 BYTE) NOT NULL ENABLE, 
"CREATE_DATE" DATE NOT NULL ENABLE, 
"UPDATE_DATE" DATE NOT NULL ENABLE, 
 CONSTRAINT "FM_FILE_PK" PRIMARY KEY ("FILE_ID")
 USING INDEX
 TABLESPACE "INDX"  ENABLE
  ) SEGMENT CREATION IMMEDIATE
 TABLESPACE "TRANSMART" ;

--
-- Type: TRIGGER; Owner: FMAPP; Name: TRG_FM_FILE_ID
--
  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FILE_ID"
before insert on fmapp."FM_FILE"    
  for each row begin    
    if inserting then      
      if :NEW."FILE_ID" is null then          
        select SEQ_FM_ID.nextval into :NEW."FILE_ID" from dual;       
      end if;    
    end if; 
  end;
/
ALTER TRIGGER "FMAPP"."TRG_FM_FILE_ID" ENABLE;
 
--
-- Type: TRIGGER; Owner: FMAPP; Name: TRG_FM_FILE_UID
--
  CREATE OR REPLACE TRIGGER "FMAPP"."TRG_FM_FILE_UID" after insert on fmapp."FM_FILE"    
for each row
DECLARE
  rec_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO rec_count 
  FROM fm_data_uid 
  WHERE fm_data_id = :new.FILE_ID;
  
  if rec_count = 0 then
    insert into fmapp.fm_data_uid (fm_data_id, unique_id, fm_data_type)
    values (:NEW."FILE_ID", FM_FILE_UID(:NEW."FILE_ID"), 'FM_FILE');
  end if;
end;

/
ALTER TRIGGER "FMAPP"."TRG_FM_FILE_UID" ENABLE;
 

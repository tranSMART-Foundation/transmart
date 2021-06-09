--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: QTM_QUERY_INSTANCE
--
 CREATE TABLE "I2B2DEMODATA"."QTM_QUERY_INSTANCE"
  (	"QUERY_INSTANCE_ID" NUMBER(5,0) NOT NULL ENABLE,
"QUERY_MASTER_ID" NUMBER(5,0),
"USER_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"GROUP_ID" VARCHAR2(50 BYTE) NOT NULL ENABLE,
"BATCH_MODE" VARCHAR2(50 BYTE),
"START_DATE" DATE NOT NULL ENABLE,
"END_DATE" DATE,
"DELETE_FLAG" VARCHAR2(3 BYTE),
"STATUS_TYPE_ID" NUMBER(5,0),
"MESSAGE" CLOB,
 PRIMARY KEY ("QUERY_INSTANCE_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("MESSAGE") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;
--
-- Type: SEQUENCE; Owner: I2B2DEMODATA; Name: QTM_SQ_QI_QIID
--
CREATE SEQUENCE  "I2B2DEMODATA"."QTM_SQ_QI_QIID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;
--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QTM_FK_QI_STID
--
ALTER TABLE "I2B2DEMODATA"."QTM_QUERY_INSTANCE" ADD CONSTRAINT "QTM_FK_QI_STID" FOREIGN KEY ("STATUS_TYPE_ID")
 REFERENCES "I2B2DEMODATA"."QT_QUERY_STATUS_TYPE" ("STATUS_TYPE_ID") ENABLE;
--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QTM_FK_QI_MID
--
ALTER TABLE "I2B2DEMODATA"."QTM_QUERY_INSTANCE" ADD CONSTRAINT "QTM_FK_QI_MID" FOREIGN KEY ("QUERY_MASTER_ID")
 REFERENCES "I2B2DEMODATA"."QTM_QUERY_MASTER" ("QUERY_MASTER_ID") ENABLE;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: QTM_IDX_QI_UGID
--
CREATE INDEX "I2B2DEMODATA"."QTM_IDX_QI_UGID" ON "I2B2DEMODATA"."QTM_QUERY_INSTANCE" ("USER_ID", "GROUP_ID")
TABLESPACE "I2B2_INDEX" ;
--
-- Type: INDEX; Owner: I2B2DEMODATA; Name: QTM_IDX_QI_MSTARTID
--
CREATE INDEX "I2B2DEMODATA"."QTM_IDX_QI_MSTARTID" ON "I2B2DEMODATA"."QTM_QUERY_INSTANCE" ("QUERY_MASTER_ID", "START_DATE")
    TABLESPACE "I2B2_INDEX" ;

-- trigger not in i2b2

--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TRG_QTM_QI_QI_ID
--
---  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TRG_QTM_QI_QI_ID"
---   before insert on "I2B2DEMODATA"."QTM_QUERY_INSTANCE"
---   for each row
---begin
---   if inserting then
---      if :NEW."QUERY_INSTANCE_ID" is null then
---         select QTM_SQ_QI_QIID.nextval into :NEW."QUERY_INSTANCE_ID" from dual;
---      end if;
---   end if;
---end;
---/
---ALTER TRIGGER "I2B2DEMODATA"."TRG_QTM_QI_QI_ID" ENABLE;

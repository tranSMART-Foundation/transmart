--
-- Type: TABLE; Owner: I2B2DEMODATA; Name: QT_QUERY_RESULT_INSTANCE
--
 CREATE TABLE "I2B2DEMODATA"."QT_QUERY_RESULT_INSTANCE"
  (	"RESULT_INSTANCE_ID" NUMBER(5,0) NOT NULL ENABLE,
"QUERY_INSTANCE_ID" NUMBER(5,0),
"RESULT_TYPE_ID" NUMBER(3,0) NOT NULL ENABLE,
"SET_SIZE" NUMBER(10,0),
"START_DATE" DATE NOT NULL ENABLE,
"END_DATE" DATE,
"DELETE_FLAG" VARCHAR2(3 BYTE),
"STATUS_TYPE_ID" NUMBER(3,0) NOT NULL ENABLE,
"MESSAGE" CLOB,
"DESCRIPTION" VARCHAR2(200 BYTE),
"REAL_SET_SIZE" NUMBER(10,0),
"OBFUSC_METHOD" VARCHAR2(500 BYTE),
 CONSTRAINT "QT_QUERY_RESULT_INSTANCE_PK" PRIMARY KEY ("RESULT_INSTANCE_ID")
 USING INDEX
 TABLESPACE "I2B2_INDEX"  ENABLE
  ) SEGMENT CREATION DEFERRED
 TABLESPACE "I2B2"
LOB ("MESSAGE") STORE AS SECUREFILE (
 TABLESPACE "I2B2" ENABLE STORAGE IN ROW CHUNK 8192
 NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;

--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QT_FK_QRI_RID
--
ALTER TABLE "I2B2DEMODATA"."QT_QUERY_RESULT_INSTANCE" ADD CONSTRAINT "QT_FK_QRI_RID" FOREIGN KEY ("QUERY_INSTANCE_ID")
 REFERENCES "I2B2DEMODATA"."QT_QUERY_INSTANCE" ("QUERY_INSTANCE_ID") ENABLE;
--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QT_FK_QRI_RTID
--
ALTER TABLE "I2B2DEMODATA"."QT_QUERY_RESULT_INSTANCE" ADD CONSTRAINT "QT_FK_QRI_RTID" FOREIGN KEY ("RESULT_TYPE_ID")
 REFERENCES "I2B2DEMODATA"."QT_QUERY_RESULT_TYPE" ("RESULT_TYPE_ID") ENABLE;
--
-- Type: REF_CONSTRAINT; Owner: I2B2DEMODATA; Name: QT_FK_QRI_STID
--
ALTER TABLE "I2B2DEMODATA"."QT_QUERY_RESULT_INSTANCE" ADD CONSTRAINT "QT_FK_QRI_STID" FOREIGN KEY ("STATUS_TYPE_ID")
 REFERENCES "I2B2DEMODATA"."QT_QUERY_STATUS_TYPE" ("STATUS_TYPE_ID") ENABLE;

--
-- Type: SEQUENCE; Owner: I2B2DEMODATA; Name: QT_SQ_QRI_QRIID
--
CREATE SEQUENCE  "I2B2DEMODATA"."QT_SQ_QRI_QRIID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

-- no trigger in i2b2

--
-- Type: TRIGGER; Owner: I2B2DEMODATA; Name: TRG_QT_QRI_RI_ID
--
---  CREATE OR REPLACE TRIGGER "I2B2DEMODATA"."TRG_QT_QRI_RI_ID"
---   before insert on "I2B2DEMODATA"."QT_QUERY_RESULT_INSTANCE"
---   for each row
---begin
---   if inserting then
---      if :NEW."RESULT_INSTANCE_ID" is null then
---         select QT_SQ_QRI_QRIID.nextval into :NEW."RESULT_INSTANCE_ID" from dual;
---      end if;
---   end if;
---end;
---/
---ALTER TRIGGER "I2B2DEMODATA"."TRG_QT_QRI_RI_ID" ENABLE;

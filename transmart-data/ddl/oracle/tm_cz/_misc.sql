--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_CZ_TEST
--
CREATE SEQUENCE  "TM_CZ"."SEQ_CZ_TEST"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 2 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_CZ
--
CREATE SEQUENCE  "TM_CZ"."SEQ_CZ"  MINVALUE 1 MAXVALUE 9999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: RTQALIMITS_TESTID_SEQ
--
CREATE SEQUENCE  "TM_CZ"."RTQALIMITS_TESTID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_PROBESET_ID
--
CREATE SEQUENCE  "TM_CZ"."SEQ_PROBESET_ID"  MINVALUE 1 MAXVALUE 99999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: TYPE; Owner: TM_CZ; Name: VARCHAR_TABLE
--
  CREATE OR REPLACE TYPE "TM_CZ"."VARCHAR_TABLE" 
                                         IS TABLE OF VARCHAR2(1000);
/

 
--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_CHILD_ROLLUP_ID
--
CREATE SEQUENCE  "TM_CZ"."SEQ_CHILD_ROLLUP_ID"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: RTQASTATSLIST_TESTID_SEQ
--
CREATE SEQUENCE  "TM_CZ"."RTQASTATSLIST_TESTID_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_REGION_ID
--
CREATE SEQUENCE  "TM_CZ"."SEQ_REGION_ID"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: EMT_TEMP_SEQ
--
CREATE SEQUENCE  "TM_CZ"."EMT_TEMP_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--
-- Type: SEQUENCE; Owner: TM_CZ; Name: SEQ_CZ_TEST_CATEGORY
--
CREATE SEQUENCE  "TM_CZ"."SEQ_CZ_TEST_CATEGORY"  MINVALUE 1 MAXVALUE 999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 2 NOORDER  NOCYCLE ;

--
-- Type: TYPE; Owner: TM_CZ; Name: T_STRING_AGG
--
  CREATE OR REPLACE TYPE "TM_CZ"."T_STRING_AGG" 
                                         AS OBJECT
(
  g_string  VARCHAR2(32767),

  STATIC FUNCTION ODCIAggregateInitialize(sctx  IN OUT  t_string_agg)
    RETURN NUMBER,

  MEMBER FUNCTION ODCIAggregateIterate(self   IN OUT  t_string_agg,
                                       value  IN      VARCHAR2 )
     RETURN NUMBER,

  MEMBER FUNCTION ODCIAggregateTerminate(self         IN   t_string_agg,
                                         returnValue  OUT  VARCHAR2,
                                         flags        IN   NUMBER)
    RETURN NUMBER,

  MEMBER FUNCTION ODCIAggregateMerge(self  IN OUT  t_string_agg,
                                     ctx2  IN      t_string_agg)
    RETURN NUMBER
);
/
CREATE OR REPLACE TYPE BODY "TM_CZ"."T_STRING_AGG" IS
  STATIC FUNCTION ODCIAggregateInitialize(sctx  IN OUT  t_string_agg)
    RETURN NUMBER IS
  BEGIN
    sctx := t_string_agg(NULL);
    RETURN ODCIConst.Success;
  END;

  MEMBER FUNCTION ODCIAggregateIterate(self   IN OUT  t_string_agg,
                                       value  IN      VARCHAR2 )
    RETURN NUMBER IS
  BEGIN
    SELF.g_string := self.g_string || ',' || value;
    RETURN ODCIConst.Success;
  END;

  MEMBER FUNCTION ODCIAggregateTerminate(self         IN   t_string_agg,
                                         returnValue  OUT  VARCHAR2,
                                         flags        IN   NUMBER)
    RETURN NUMBER IS
  BEGIN
    returnValue := RTRIM(LTRIM(SELF.g_string, ','), ',');
    RETURN ODCIConst.Success;
  END;

  MEMBER FUNCTION ODCIAggregateMerge(self  IN OUT  t_string_agg,
                                     ctx2  IN      t_string_agg)
    RETURN NUMBER IS
  BEGIN
    SELF.g_string := SELF.g_string || ',' || ctx2.g_string;
    RETURN ODCIConst.Success;
  END;
END;
/
 

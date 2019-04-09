--
-- Type: USER; Name: GALAXY
--
CREATE USER "GALAXY" IDENTIFIED BY VALUES 'S:98CCC5E266C34929A802431A8B7D789256483A9A96E679A2A0E98ADCF1E1;20D007CA05075E67'
   DEFAULT TABLESPACE "TRANSMART"
   TEMPORARY TABLESPACE "TEMP";
--
-- Type: ROLE_GRANT; Name: GALAXY
--
GRANT "CONNECT" TO "GALAXY";
--
-- Type: TABLESPACE_QUOTA; Name: GALAXY
--
  DECLARE 
  TEMP_COUNT NUMBER; 
  SQLSTR VARCHAR2(200); 
BEGIN 
  SQLSTR := 'ALTER USER "GALAXY" QUOTA UNLIMITED ON "INDX"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION 
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN 
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES 
              WHERE TABLESPACE_NAME = ''INDX'' AND CONTENTS = ''TEMPORARY''';
      EXECUTE IMMEDIATE SQLSTR INTO TEMP_COUNT;
      IF TEMP_COUNT = 1 THEN RETURN; 
      ELSE RAISE; 
      END IF;
    ELSE
      RAISE;
    END IF;
END;
/
  DECLARE 
  TEMP_COUNT NUMBER; 
  SQLSTR VARCHAR2(200); 
BEGIN 
  SQLSTR := 'ALTER USER "GALAXY" QUOTA UNLIMITED ON "TRANSMART"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION 
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN 
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES 
              WHERE TABLESPACE_NAME = ''TRANSMART'' AND CONTENTS = ''TEMPORARY''';
      EXECUTE IMMEDIATE SQLSTR INTO TEMP_COUNT;
      IF TEMP_COUNT = 1 THEN RETURN; 
      ELSE RAISE; 
      END IF;
    ELSE
      RAISE;
    END IF;
END;
/
  DECLARE 
  TEMP_COUNT NUMBER; 
  SQLSTR VARCHAR2(200); 
BEGIN 
  SQLSTR := 'ALTER USER "GALAXY" QUOTA UNLIMITED ON "USERS"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION 
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN 
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES 
              WHERE TABLESPACE_NAME = ''USERS'' AND CONTENTS = ''TEMPORARY''';
      EXECUTE IMMEDIATE SQLSTR INTO TEMP_COUNT;
      IF TEMP_COUNT = 1 THEN RETURN; 
      ELSE RAISE; 
      END IF;
    ELSE
      RAISE;
    END IF;
END;
/
--
-- Type: SYSTEM_GRANT; Name: GALAXY
--

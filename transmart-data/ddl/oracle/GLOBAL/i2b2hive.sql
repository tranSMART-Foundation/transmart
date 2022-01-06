--
-- Type: USER; Name: I2B2HIVE
--
--CREATE USER "I2B2HIVE" IDENTIFIED BY VALUES 'S:C73A8B4E8B2A62D16A9AB6BA9B9C5980E37D87D05BB946D1C222BEF89011;61AA7B443983457E'
CREATE USER "I2B2HIVE" IDENTIFIED BY demouser
   DEFAULT TABLESPACE "I2B2"
   TEMPORARY TABLESPACE "TEMP";
--
-- Type: ROLE_GRANT; Name: I2B2HIVE
--
GRANT "CONNECT" TO "I2B2HIVE";
GRANT "RESOURCE" TO "I2B2HIVE";
--
-- Type: TABLESPACE_QUOTA; Name: I2B2HIVE
--
  DECLARE
  TEMP_COUNT NUMBER;
  SQLSTR VARCHAR2(200);
BEGIN
  SQLSTR := 'ALTER USER "I2B2HIVE" QUOTA UNLIMITED ON "I2B2"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES
              WHERE TABLESPACE_NAME = ''I2B2'' AND CONTENTS = ''TEMPORARY''';
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
  SQLSTR := 'ALTER USER "I2B2HIVE" QUOTA UNLIMITED ON "I2B2"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES
              WHERE TABLESPACE_NAME = ''I2B2'' AND CONTENTS = ''TEMPORARY''';
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
  SQLSTR := 'ALTER USER "I2B2HIVE" QUOTA UNLIMITED ON "I2B2_INDEX"';
  EXECUTE IMMEDIATE SQLSTR;
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE = -30041 THEN
      SQLSTR := 'SELECT COUNT(*) FROM USER_TABLESPACES
              WHERE TABLESPACE_NAME = ''I2B2_INDEX'' AND CONTENTS = ''TEMPORARY''';
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
-- Type: SYSTEM_GRANT; Name: I2B2HIVE
--
GRANT CREATE SESSION TO "I2B2HIVE";
GRANT CREATE TABLE TO "I2B2HIVE";
GRANT CREATE VIEW TO "I2B2HIVE";
GRANT CREATE SEQUENCE TO "I2B2HIVE";
GRANT CREATE ROLE TO "I2B2HIVE";
GRANT CREATE PROCEDURE TO "I2B2HIVE";
GRANT CREATE TRIGGER TO "I2B2HIVE";
GRANT CREATE TYPE TO "I2B2HIVE";
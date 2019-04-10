--------------------------------------------------------
-- ARRAY TYPE FOR PDO QUERY
--------------------------------------------------------
CREATE OR REPLACE TYPE QT_PDO_QRY_INT_ARRAY AS varray(100000) of  NUMBER(20)
; 

CREATE OR REPLACE TYPE QT_PDO_QRY_STRING_ARRAY AS varray(100000) of  VARCHAR2(150)
;

--
-- expand width of race_cd
--

ALTER TABLE IF EXISTS i2b2demodata.patient_dimension ALTER COLUMN race_cd TYPE character varying(100);

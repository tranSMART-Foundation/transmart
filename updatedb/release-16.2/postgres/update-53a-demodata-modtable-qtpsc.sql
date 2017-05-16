--
-- Require values for 3 columns
--

ALTER TABLE IF EXISTS i2b2demodata.qt_patient_sample_collection ALTER COLUMN sample_id SET NOT NULL;
ALTER TABLE IF EXISTS i2b2demodata.qt_patient_sample_collection ALTER COLUMN patient_id SET NOT NULL;
ALTER TABLE IF EXISTS i2b2demodata.qt_patient_sample_collection ALTER COLUMN result_instance_id SET NOT NULL;


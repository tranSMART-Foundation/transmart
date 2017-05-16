--
-- default for i2b2metadata.i2b2_secure column m_applied_path
--

ALTER TABLE IF EXISTS i2b2metadata.i2b2_secure ALTER COLUMN m_applied_path SET DEFAULT '@'::character varying;

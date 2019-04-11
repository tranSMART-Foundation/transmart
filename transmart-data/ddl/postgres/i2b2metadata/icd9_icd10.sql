--
-- Name: icd9_icd10; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE icd10_icd9 (
    c_hlevel int NOT NULL, 
    c_fullname character varying(700) NOT NULL, 
    c_name character varying(2000) NOT NULL, 
    c_synonym_cd character(1) NOT NULL, 
    c_visualattributes character(3) NOT NULL, 
    c_totalnum int,
    c_basecode character varying(50),
    c_metadataxml text,
    c_facttablecolumn character varying(50) NOT NULL, 
    c_tablename character varying(50) NOT NULL, 
    c_columnname character varying(50) NOT NULL, 
    c_columndatatype character varying(50) NOT NULL, 
    c_operator character varying(10) NOT NULL, 
    c_dimcode character varying(700) NOT NULL, 
    c_comment text,
    c_tooltip character varying(900),
    m_applied_path character varying(700) NOT NULL, 
    update_date timestamp NOT NULL, 
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    valuetype_cd character varying(50),
    m_exclusion_CD character varying(25),
    c_path character varying(700),
    c_symbol character varying(50),
    plain_code character varying(25)
    ) 
   ;
   
--
-- Name: META_FULLNAME_IDX_ICD10_ICD9; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_FULLNAME_IDX_ICD10_ICD9 ON ICD10_ICD9(C_FULLNAME);
--
-- Name: META_APPL_PATH_ICD10_ICD9_IDX; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_APPL_PATH_ICD10_ICD9_IDX ON icd10_icd9 USING btree (m_applied_path);
--
-- Name: META_EXCLUSION_ICD10_ICD9_IDX; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_EXCLUSION_ICD10_ICD9_IDX ON icd10_icd9 USING btree (m_exclusion_cd);
--
-- Name: META_HLEVEL_ICD10_ICD9_IDX; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_HLEVEL_ICD10_ICD9_IDX ON icd10_icd9 USING btree (c_hlevel);
--
-- Name: META_SYNONYM_ICD10_ICD9_IDX; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_SYNONYM_ICD10_ICD9_IDX ON icd10_icd9 USING btree (c_synonym_cd);

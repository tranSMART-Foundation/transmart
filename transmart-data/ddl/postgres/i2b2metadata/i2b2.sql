--
-- Name: i2b2; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE i2b2 (
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
    update_date timestamp NOT NULL,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    valuetype_cd character varying(50),
    m_applied_path character varying(700) DEFAULT '@'::character varying NOT NULL,
    m_exclusion_cd character varying(25),
    c_path character varying(700),
    c_symbol character varying(50)
);

--
-- Name: META_FULLNAME_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_FULLNAME_i2b2_idx ON i2b2 USING btree (c_fullname);
--
-- Name: META_APPLIED_PATH_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_APPLIED_PATH_i2b2_idx ON i2b2 USING btree (m_applied_path);
--
-- Name: META_EXCLUSION_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_EXCLUSION_i2b2_idx ON i2b2 USING btree (m_exclusion_cd);
--
-- Name: META_HLEVEL_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_HLEVEL_i2b2_idx ON i2b2 USING btree (c_hlevel);
--
-- Name: META_SYNONYM_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_SYNONYM_i2b2_idx ON i2b2 USING btree (c_synonym_cd);
--
-- Name: i2b2_c_comment_char_length_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
-- not in i2b2 of oracle
-- for the i2b2_trial_node view to be replaced by a table
CREATE INDEX i2b2_c_comment_char_length_idx ON i2b2 USING btree (c_comment, char_length((c_fullname)::text));
--
-- Name: meta_base_full_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
--not in i2b2
CREATE INDEX meta_base_full_i2b2_idx ON i2b2 USING btree (c_basecode, c_fullname);
--
-- Name: meta_visatt_i2b2_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
--not in i2b2
CREATE INDEX meta_visatt_i2b2_idx ON i2b2 USING btree (c_visualattributes);

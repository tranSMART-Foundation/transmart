--
-- Name: phi; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE phi (
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
    m_applied_path character varying(700) DEFAULT '@'::character varying NOT NULL,
    update_date timestamp NOT NULL,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    valuetype_cd character varying(50),
    m_exclusion_cd character varying(25),
    c_path character varying(700),
    c_symbol character varying(50)
);

--
-- Name: META_FULLNAME_phi_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_FULLNAME_phi_idx ON phi USING btree (c_fullname);
--
-- Name: META_APPLIED_PATH_phi_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_APPLIED_PATH_phi_idx ON phi USING btree (m_applied_path);
--
-- Name: META_EXCLUSION_phi_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_EXCLUSION_phi_idx ON phi USING btree (m_exclusion_cd);
--
-- Name: META_HLEVEL_phi_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_HLEVEL_phi_idx ON phi USING btree (c_hlevel);
--
-- Name: META_SYNONYM_phi_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX META_SYNONYM_phi_idx ON phi USING btree (c_synonym_cd);
--
-- Name: phi_c_comment_char_length_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX phi_c_comment_char_length_idx ON phi USING btree (c_comment, char_length((c_fullname)::text)); --not in i2b2
--
-- Name: phi_basecode_fullname_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX phi_basecode_fullname_idx ON phi USING btree (c_basecode, c_fullname); --not in i2b2
--
-- Name: phi_visatt_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX phi_visatt_idx ON phi USING btree (c_visualattributes); --not in i2b2

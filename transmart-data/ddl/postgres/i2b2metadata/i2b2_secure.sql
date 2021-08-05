--
-- Name: i2b2_secure; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE i2b2_secure (
    c_hlevel int,
    c_fullname character varying(900) NOT NULL,
    c_name character varying(2000),
    c_synonym_cd character(1),
    c_visualattributes character(3),
    c_totalnum int,
    c_basecode character varying(450),
    c_metadataxml text,
    c_facttablecolumn character varying(50),
    c_tablename character varying(150),
    c_columnname character varying(50),
    c_columndatatype character varying(50),
    c_operator character varying(10),
    c_dimcode character varying(900),
    c_comment text,
    c_tooltip character varying(900),
    m_applied_path character varying(700) DEFAULT '@'::character varying,
    update_date timestamp,
    download_date timestamp,
    import_date timestamp,
    sourcesystem_cd character varying(50),
    valuetype_cd character varying(50),
    m_exclusion_cd character varying(25),
    c_path character varying(900),
    c_symbol character varying(50),
    i2b2_id int,
    secure_obj_token character varying(50)
);

--
-- Name: i2b2_sec_srcsys; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_srcsys ON i2b2_secure USING btree (sourcesystem_cd);

--
-- Name: i2b2_sec_name_cd_lvl; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_name_cd_lvl ON i2b2_secure USING btree (c_fullname, c_synonym_cd, c_hlevel);

--
-- Name: i2b2_sec_name_cd_vis; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_name_cd_vis ON i2b2_secure USING btree (c_fullname, c_basecode, c_visualattributes);

--
-- Name: i2b2_sec_upname; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_upname ON i2b2_secure USING btree (upper(c_name));

--
-- Name: i2b2_sec_name_plus; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_name_plus ON i2b2_secure USING btree (c_fullname, c_hlevel, c_synonym_cd, c_visualattributes);

--
-- Name: i2b2_sec_name; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX i2b2_sec_name ON i2b2_secure USING btree (c_fullname);



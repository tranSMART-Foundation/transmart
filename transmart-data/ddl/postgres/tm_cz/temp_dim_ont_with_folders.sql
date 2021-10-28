--
-- Name: temp_dim_ont_with_folders; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE temp_dim_ont_with_folders (
    c_fullname character varying(700),
    c_basecode character varying(50)
);

--
-- Name: dimOntWithFoldersIndex; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX dimOntWithFoldersIndex ON temp_dim_ont_with_folders(c_fullname);


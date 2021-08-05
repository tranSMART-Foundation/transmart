--
-- Name: tmp_subject_info; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE tmp_subject_info (
    usubjid character varying(100),
    age_in_years_num int,
    sex_cd character varying(50),
    race_cd character varying(100)
);

--
-- Name: tmp_subj_usubjid_idx; Type: INDEX; Schema: tm_cz; Owner: -
--
CREATE INDEX tmp_subj_usubjid_idx ON tmp_subject_info USING btree (usubjid);

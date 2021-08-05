--
-- Name: de_snp_calls_by_gsm; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_calls_by_gsm (
    gsm_num character varying(100),
    trial_name character varying(100),
    patient_num int,
    snp_name character varying(100),
    snp_calls character varying(4)
);

--
-- Name: idx_snp_calls_by_gsm_pg; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX idx_snp_calls_by_gsm_pg ON de_snp_calls_by_gsm USING btree (patient_num, gsm_num);


--
-- Name: de_snp_copy_number; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE de_snp_copy_number (
    patient_num int,
    trial_name character varying(100),
    snp_name character varying(50),
    chrom character varying(2),
    chrom_pos int,
    copy_number int
);

--
-- Name: idx_snp_copy_number_ps; Type: INDEX; Schema: deapp; Owner: -
--
CREATE INDEX idx_snp_copy_number_ps ON de_snp_copy_number USING btree (patient_num, snp_name);


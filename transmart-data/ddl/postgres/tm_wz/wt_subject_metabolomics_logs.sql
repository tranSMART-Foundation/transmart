--
-- Name: wt_subject_metabolomics_logs; Type: TABLE; Schema: tm_wz; Owner: -
--
CREATE TABLE wt_subject_metabolomics_logs (
    probeset character varying(500),
    intensity_value double precision,
    pvalue double precision,
    num_calls bigint,
    assay_id bigint,
    patient_id bigint,
    sample_id bigint,
    subject_id character varying(100),
    trial_name character varying(50),
    timepoint character varying(100),
    log_intensity double precision
);

--
-- Name: wt_subject_mbolomics_logs_i1; Type: INDEX; Schema: tm_wz; Owner: -
--
--CREATE INDEX wt_subject_mbolomics_logs_i1 ON wt_subject_metabolomics_logs USING btree (trial_name, probeset);


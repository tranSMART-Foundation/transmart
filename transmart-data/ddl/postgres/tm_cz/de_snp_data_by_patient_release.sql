--
-- Name: de_snp_data_by_patient_release; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE de_snp_data_by_patient_release (
    snp_data_by_patient_id int,
    snp_dataset_id int,
    trial_name character varying(100),
    patient_num int,
    chrom character varying(16),
    data_by_patient_chr text,
    ped_by_patient_chr text,
    release_study character varying(255)
);


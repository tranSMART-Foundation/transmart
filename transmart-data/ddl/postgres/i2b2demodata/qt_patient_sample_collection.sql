--
-- Name: qt_patient_set_collection; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_patient_sample_collection (
    sample_id bigint NOT NULL,
    patient_id bigint NOT NULL,
    result_instance_id bigint NOT NULL
);

--
-- Name: haploview_data; Type: TABLE; Schema: deapp; Owner: -
--
CREATE TABLE haploview_data (
    i2b2_id int,
    jnj_id character varying(30),
    father_id int,
    mother_id int,
    sex int,
    affection_status int,
    chromosome character varying(10),
    gene character varying(50),
    release int,
    release_date timestamp,
    trial_name character varying(100),
    snp_data text
);


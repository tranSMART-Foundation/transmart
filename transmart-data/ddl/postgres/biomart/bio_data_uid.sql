--
-- Name: bio_data_uid; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_data_uid (
    bio_data_id int NOT NULL,
    unique_id character varying(300) NOT NULL,
    bio_data_type character varying(100) NOT NULL
);

--
-- Name: bio_data_uid_pk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_data_uid
    ADD CONSTRAINT bio_data_uid_pk PRIMARY KEY (bio_data_id);

--
-- Name: bio_data_uid_uk; Type: CONSTRAINT; Schema: biomart; Owner: -
--
ALTER TABLE ONLY bio_data_uid
    ADD CONSTRAINT bio_data_uid_uk UNIQUE (unique_id);


--
-- Name: seq_bio_data_id; Type: SEQUENCE; Schema: biomart; Owner: -
--
CREATE SEQUENCE seq_bio_data_id
    START WITH 896347
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


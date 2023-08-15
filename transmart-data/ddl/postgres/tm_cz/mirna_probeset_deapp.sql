--
-- Name: seq_probeset_id; Type: SEQUENCE; Schema: tm_cz; Owner: -
--
CREATE SEQUENCE seq_probeset_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 20;

--
-- Name: mirna_probeset_deapp; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE mirna_probeset_deapp (
    probeset_id int DEFAULT nextval('tm_cz.seq_probeset_id') NOT NULL,
    probeset character varying(100),
    platform character varying(100),
    organism character varying(100)
);


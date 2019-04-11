--
-- Name: bio_recombination_rates; Type: TABLE; Schema: biomart; Owner: -
--
CREATE TABLE bio_recombination_rates (
    chromosome character varying(20),
    "position" int,
    rate decimal(18,6),
    map decimal(18,6)
);


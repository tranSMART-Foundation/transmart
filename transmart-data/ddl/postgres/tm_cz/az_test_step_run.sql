--
-- Name: az_test_step_run; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE az_test_step_run (
    test_step_run_id int NOT NULL,
    test_run_id int NOT NULL,
    test_id int NOT NULL,
    start_date timestamp,
    end_date timestamp,
    status character varying(20),
    seq_id int,
    param1 character varying(4000)
);

--
-- Name: az_test_step_run_pk; Type: CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY az_test_step_run
    ADD CONSTRAINT az_test_step_run_pk PRIMARY KEY (test_step_run_id);

--
-- Name: tf_trg_az_test_step_run_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_az_test_step_run_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    if new.test_step_run_id is null then
	select nextval('tm_cz.seq_cz_test') into new.test_step_run_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_az_test_step_run_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_az_test_step_run_id BEFORE INSERT ON az_test_step_run FOR EACH ROW EXECUTE PROCEDURE tf_trg_az_test_step_run_id();

--
-- Name: az_test_step_run_cz_job_fk1; Type: FK CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY az_test_step_run
    ADD CONSTRAINT az_test_step_run_cz_job_fk1 FOREIGN KEY (test_id) REFERENCES cz_test(test_id);

--
-- Name: az_tst_step_run_az_test_ru_fk1; Type: FK CONSTRAINT; Schema: tm_cz; Owner: -
--
ALTER TABLE ONLY az_test_step_run
    ADD CONSTRAINT az_tst_step_run_az_test_ru_fk1 FOREIGN KEY (test_run_id) REFERENCES az_test_run(test_run_id);


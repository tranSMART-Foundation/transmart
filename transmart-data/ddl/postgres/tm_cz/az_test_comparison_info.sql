--
-- Name: az_test_comparison_info; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE az_test_comparison_info (
    test_id int,
    param1 character varying(4000),
    prev_dw_version_id int,
    prev_test_step_run_id int,
    prev_act_record_cnt double precision,
    curr_dw_version_id int,
    curr_test_step_run_id int,
    curr_act_record_cnt double precision,
    curr_run_date timestamp,
    prev_run_date timestamp
);


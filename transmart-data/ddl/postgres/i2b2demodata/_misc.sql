--
-- Name: async_job_seq; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE async_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: protocol_id_seq; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE protocol_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: seq_subject_reference; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE seq_subject_reference
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: sq_up_encdim_encounternum; Type: SEQUENCE; Schema: i2b2demodata; Owner: -
--
CREATE SEQUENCE sq_up_encdim_encounternum
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;




-- DX
CREATE TEMPORARY TABLE DX  (
    encounter_num	INT,
    patient_num		INT,
    instance_num	INT,
    concept_cd 		varchar(50), 
    start_date 		TIMESTAMP,
    provider_id 	varchar(50), 
    temporal_start_date TIMESTAMP, 
    temporal_end_date TIMESTAMP
 ) ON COMMIT PRESERVE ROWS
;

create TEMPORARY TABLE TEMP_PDO_INPUTLIST ( 
    char_param1 varchar(100)
 ) ON COMMIT PRESERVE ROWS
;


-- QUERY_GLOBAL_TEMP
CREATE TEMPORARY TABLE QUERY_GLOBAL_TEMP ( 
    encounter_num	INT,
    patient_num		INT,
    instance_num	INT,
    concept_cd      VARCHAR(50),
    start_date	    DATE,
    provider_id     VARCHAR(50),
    panel_count		INT,
    fact_count		INT,
    fact_panels		INT
 ) ON COMMIT PRESERVE ROWS
;

-- GLOBAL_TEMP_PARAM_TABLE
 CREATE TEMPORARY TABLE GLOBAL_TEMP_PARAM_TABLE (
    set_index	INT,
    char_param1	VARCHAR(500),
    char_param2	VARCHAR(500),
    num_param1	INT,
    num_param2	INT
) ON COMMIT PRESERVE ROWS
;

-- GLOBAL_TEMP_FACT_PARAM_TABLE
CREATE TEMPORARY TABLE GLOBAL_TEMP_FACT_PARAM_TABLE (
    set_index	INT,
    char_param1	VARCHAR(500),
    char_param2	VARCHAR(500),
    num_param1	INT,
    num_param2	INT
) ON COMMIT PRESERVE ROWS
;

-- MASTER_QUERY_GLOBAL_TEMP
CREATE TEMPORARY TABLE MASTER_QUERY_GLOBAL_TEMP ( 
    encounter_num	INT,
    patient_num		INT,
    instance_num	INT,
    concept_cd      VARCHAR(50),
    start_date	    DATE,
    provider_id     VARCHAR(50),
    master_id		VARCHAR(50),
    level_no		INT,
    temporal_start_date DATE,
    temporal_end_date DATE
 ) ON COMMIT PRESERVE ROWS
;

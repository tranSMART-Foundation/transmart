--
-- Name: cz_xtrial_ctrl_vocab; Type: TABLE; Schema: tm_cz; Owner: -
--
CREATE TABLE cz_xtrial_ctrl_vocab (
    ctrl_vocab_code character varying(200) NOT NULL,
    ctrl_vocab_name character varying(200) NOT NULL,
    ctrl_vocab_category character varying(200),
    ctrl_vocab_id int NOT NULL
);

--
-- Name: tf_trg_cz_xtrial_ctrl_vocab_id(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE FUNCTION tf_trg_cz_xtrial_ctrl_vocab_id() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin     
    if new.ctrl_vocab_id is null then          
	select nextval('tm_cz.seq_cz') into new.ctrl_vocab_id ;
    end if;

    return new;
end;
$$;

--
-- Name: trg_cz_xtrial_ctrl_vocab_id; Type: TRIGGER; Schema: tm_cz; Owner: -
--
CREATE TRIGGER trg_cz_xtrial_ctrl_vocab_id BEFORE INSERT ON cz_xtrial_ctrl_vocab FOR EACH ROW EXECUTE PROCEDURE tf_trg_cz_xtrial_ctrl_vocab_id();


--
-- Name: tm_trial_nodes; Type: TABLE; Schema: i2b2metadata; Owner: -
--
CREATE TABLE tm_trial_nodes (
    trial character varying(50) NOT NULL,
    c_fullname character varying(700) NOT NULL
);

--
-- Name: tm_trial_nodes_pk; Type: CONSTRAINT; Schema: i2b2metadata; Owner: -
--
ALTER TABLE ONLY tm_trial_nodes
    ADD CONSTRAINT tm_trial_nodes_pk PRIMARY KEY (c_fullname);
--
-- Name: tm_tn_trial_idx; Type: INDEX; Schema: i2b2metadata; Owner: -
--
CREATE INDEX TM_TN_TRIAL ON tm_trial_nodes USING btree (trial);




--
-- Name: jms_messages; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE jms_messages (
    messageid int NOT NULL,
    destination character varying(255) NOT NULL,
    txid int,
    txop character(1),
    messageblob text
);

--
-- Name: jms_messages_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY jms_messages
    ADD CONSTRAINT jms_messages_pk PRIMARY KEY (messageid, destination);

--
-- Name: jms_messages_destination; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX jms_messages_destination ON jms_messages USING btree (destination);

--
-- Name: jms_messages_txop_txid; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX jms_messages_txop_txid ON jms_messages USING btree (txop, txid);


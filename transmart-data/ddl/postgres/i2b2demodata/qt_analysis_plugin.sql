--
-- Name: qt_analysis_plugin; Type: TABLE; Schema: i2b2demodata; Owner: -
--
CREATE TABLE qt_analysis_plugin (
    plugin_id serial NOT NULL,
    plugin_name character varying(2000),
    description character varying(2000),
    version_cd character varying(50),	--support for version
    parameter_info text,		-- plugin parameter stored as xml
    parameter_info_xsd text,
    command_line text,
    working_folder text,
    commandoption_cd text,
    plugin_icon text,
    status_cd character varying(50),	-- active,deleted,..
    user_id character varying(50),
    group_id character varying(50),
    create_date timestamp,
    update_date timestamp
);

--
-- Name: analysis_plugin_pk; Type: CONSTRAINT; Schema: i2b2demodata; Owner: -
--
ALTER TABLE ONLY qt_analysis_plugin
    ADD CONSTRAINT analysis_plugin_pk PRIMARY KEY (plugin_id);

--
-- Name: qt_apnamevergrp_idx; Type: INDEX; Schema: i2b2demodata; Owner: -
--
CREATE INDEX qt_apnamevergrp_idx ON qt_analysis_plugin USING btree (plugin_name, version_cd, group_id);


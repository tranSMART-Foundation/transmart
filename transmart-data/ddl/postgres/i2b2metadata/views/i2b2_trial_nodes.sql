--
-- Name: i2b2_trial_nodes; Type: VIEW; Schema: i2b2metadata; Owner: -
--
CREATE VIEW i2b2metadata.i2b2_trial_nodes AS
    SELECT DISTINCT ON (trial) c_fullname, trial
        FROM (
	    SELECT sourcesystem_cd
                   , CASE WHEN i2b2.c_comment like 'trial:%'
                          THEN "substring"(i2b2.c_comment, 7)
       		          ELSE 'I2B2' END
	              AS trial
                   , char_length(i2b2.c_fullname) AS length
    		   , CASE WHEN i2b2.c_comment like 'trial:%'
                          THEN i2b2.c_fullname
       		          ELSE '\i2b2\' END
	              AS c_fullname
    	    FROM i2b2metadata.i2b2) SUB
    ORDER BY trial, length ASC;

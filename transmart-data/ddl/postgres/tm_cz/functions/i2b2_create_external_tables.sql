--
-- Name: i2b2_create_external_tables(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_create_external_tables(tpmextfn character varying, catgextfn character varying) RETURNS void
    LANGUAGE plpgsql
AS $$

    -- Attention:
    -- Oracle procedure to define tables
    -- in biomart_lz which is no longer used
    -- is there a postgreSQL equivalent for these tasks?

    declare


    sqltxt varchar(5000);


begin

    --  recreate CATEGORY_EXTRNL tables with CATGExtFN parameter (filename in external file system)

    sqltxt:='drop table i2b2_lz.category_extrnl';

    execute sqltxt;

    sqltxt:='create or replace  table "i2b2_lz"."category_extrnl"
	    ( study_id varchar(100 byte),
	    category_cd varchar(100 BYTE),
	    category_path varchar(250 BYTE)
	    )
	    organization external
	    ( type oracle_loader
	    default directory "biomart_lz"
	    access parameters
	    ( records delimited by newline nologfile skip 1
            fields terminated by 0X"09"
            missing field values are null
            )
	    location
	    ( ' || '''' || CATGExtFn || '''' || '))';

    execute sqltxt;

    --  recreate TIME_POINT_MEASUREMENT_EXTRNL tabls with TPMExtFN parameter (filename in external file system)

    sqltxt:='drop table i2b2_lz.time_point_measurement_extrnl';

    execute sqltxt;

    sqltxt:='    create or replace  table "i2b2_lz"."time_point_measurement_extrnl"
	    (study_id varchar(25 BYTE),
	    usubjid varchar(50 BYTE),
	    site_id varchar(10 BYTE),
	    subject_id varchar(10 BYTE),
	    visit_name varchar(100 BYTE),
	    dataset_name varchar(500 BYTE),
	    sample_type varchar(100 BYTE),
	    data_label varchar(500 BYTE),
	    data_value varchar(500 BYTE),
	    category_cd varchar(100 BYTE),
	    period varchar(100 BYTE)
	    )
	    organization external
	    ( type oracle_loader
	    default directory "biomart_lz"
	    access parameters
	    ( records delimited by newline nologfile skip 1
            fields terminated by 0X"09"
            missing field values are null
            )
	    location ( ' || '''' || TPMExtFn || '''' ||  ') )';

    execute sqltxt;

end;

$$;


--
-- Name: i2b2_table_defrag(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_table_defrag() RETURNS void
    LANGUAGE plpgsql
AS $$
begin

    -- Attention:
    -- Apparently an Oracle function

    -------------------------------------------------------------
    -- Moves the I2B2 tables to reduce defragmentation
    -- KCR@20090527 - First Rev
    -- JEA@20090923 - Removed I2B2DEMODATA.IDX_OB_FACT_3, Oracle doesn't need to index every column like SQL Server (per Aaron A.)
    -------------------------------------------------------------
    execute 'alter table i2b2metadata.i2b2 move';
    execute 'alter table i2b2metadata.tm_concept_counts move';
    execute 'alter table i2b2demodata.concept_dimension move';
    execute 'alter table i2b2demodata.observation_fact move';
    execute 'alter table i2b2demodata.patient_dimension move';
    --rebuild indexes
    execute 'alter index i2b2demodata.ob_fact_pk rebuild';
    execute 'alter index i2b2demodata.idx_ob_fact_1 rebuild';
    execute 'alter index i2b2demodata.idx_ob_fact_2 rebuild';

    execute 'alter index i2b2demodata.idx_concept_dim_1 rebuild';
    execute 'alter index i2b2demodata.idx_concept_dim_2 rebuild';

    execute 'alter index i2b2metadata.idx_i2b2_a rebuild';
    execute 'alter index i2b2metadata.idx_i2b2_b rebuild';

    execute 'alter index i2b2metadata.tm_concept_counts_index1 rebuild';

end;

$$;


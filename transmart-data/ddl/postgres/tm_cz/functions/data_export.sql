--
-- Name: data_export(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.data_export() RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    --Iterate through a cursor of all patient IDs
    --Dynamically build a sql statement
    --Run the statement returning the results

    cPatients cursor for
			 select distinct a.patient_num
			 from i2b2demodata.observation_fact a
			 join i2b2metadataq.i2b2 b
			 on a.concept_cd = b.c_basecode
			 where c_fullname like '%BRC Depression Study%'
			 and c_visualattributes not like '%H%'
			 order by patient_num;

    dynamicSQL varchar(32767);
    dynamicSQL2 varchar(32767);


begin
    dynamicSQL := 'select c_name ,c_fullname ';
    dynamicSQL2 := 'select c_name ,c_fullname ';

    for r_cPatients in cPatients loop

	dynamicSQL  := dynamicSQL  || ',max(decode(patient_num,' || cast(r_cPatients.patient_num as varchar) || ',tval_char,null)) "' || cast(r_cPatients.patient_num as varchar) || '"';
	dynamicSQL2 := dynamicSQL2 || ',max(decode(patient_num,' || cast(r_cPatients.patient_num as varchar) || ',cast(nval_num as varchar(20)),null)) "' || cast(r_cPatients.patient_num as varchar) || '"';

    end loop;

    dynamicSQL := dynamicSQL || ' from i2b2demodata.observation_fact a join i2b2metadata.i2b2 b on a.concept_cd = b.c_basecode where c_fullname like ''%BRC Depression Study%'' and c_columndatatype = ''T'' and c_visualattributes not like ''%H%'' group by c_name, c_fullname';
    dynamicSQL2 := dynamicSQL2 || ' from i2b2demodata.observation_fact a join i2b2metadata.i2b2 b on a.concept_cd = b.c_basecode where c_fullname like ''%BRC Depression Study%'' and c_columndatatype = ''N'' and c_visualattributes not like ''%H%'' group by c_name, c_fullname order by c_fullname';

    execute(dynamicSQL || ' UNION ALL ' || dynamicsql2);

    raise notice '%', dynamicSQL;
    raise notice 'UNION ALL';
    raise notice '%', dynamicsql2;
end;

$$;


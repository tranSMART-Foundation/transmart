--
-- Name: rename_program(character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.rename_program(oldprogramname character varying, newprogramname character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    oldTopNode		varchar(2000);
    newTopNode		varchar(2000);
    regex1		varchar(2000);
    regex2		varchar(2000);

begin

    oldTopNode := '\' || oldProgramName || '\' ;
    newTopNode := '\' || newProgramName || '\' ;
    regex1 := '\\' || oldProgramName || '\\' || '(.*)';
    regex2 := '\\' || newProgramName || '\\' || '\1';

    update i2b2metadata.i2b2
       set c_fullname=regexp_replace(c_fullname, regex1, regex2)
     where c_fullname like oldTopNode||'%';
    update i2b2metadata.i2b2
       set c_dimcode=regexp_replace(c_dimcode, regex1, regex2)
     where c_dimcode like oldTopNode||'%';
    update i2b2metadata.i2b2
       set c_tooltip=regexp_replace(c_tooltip, regex1, regex2)
     where c_tooltip like oldTopNode||'%';
    update i2b2metadata.i2b2
       set c_name=newProgramName
     where c_fullname=newTopNode;

    update i2b2metadata.i2b2_secure
       set c_fullname=regexp_replace(c_fullname, regex1, regex2)
     where c_fullname like oldTopNode||'%';
    update i2b2metadata.i2b2_secure
       set c_dimcode=regexp_replace(c_dimcode, regex1, regex2)
     where c_dimcode like oldTopNode||'%';
    update i2b2metadata.i2b2_secure
       set c_tooltip=regexp_replace(c_tooltip, regex1, regex2)
     where c_tooltip like oldTopNode||'%';
    update i2b2metadata.i2b2_secure
       set c_name=newProgramName
     where c_fullname=newTopNode;

    update i2b2metadata.table_access
       set c_fullname=regexp_replace(c_fullname, regex1, regex2)
     where c_fullname like oldTopNode||'%';
    update i2b2metadata.table_access
       set c_dimcode=regexp_replace(c_dimcode, regex1, regex2)
     where c_dimcode like oldTopNode||'%';
    update i2b2metadata.table_access
       set c_tooltip=regexp_replace(c_tooltip, regex1, regex2)
     where c_tooltip like oldTopNode||'%';
    update i2b2metadata.table_access
       set c_name=newProgramName
     where c_fullname=newTopNode;
    update i2b2metadata.table_access
       set c_table_cd=newProgramName
     where c_fullname=newTopNode;

    update I2B2METADATA.tm_concept_counts
       set concept_path=regexp_replace(concept_path, regex1, regex2)
     where concept_path like oldTopNode||'%';
    update I2B2METADATA.tm_concept_counts
       set parent_concept_path=regexp_replace(parent_concept_path, regex1, regex2)
     where parent_concept_path like oldTopNode||'%';

    update I2B2DEMODATA.concept_dimension
       set concept_path=regexp_replace(concept_path, regex1, regex2)
     where concept_path like oldTopNode||'%';

    commit;

end;

$$;


--
-- Name: rename_study(character varying, character varying, character varying); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.rename_study(programname character varying, oldtitle character varying, newtitle character varying) RETURNS void
    LANGUAGE plpgsql
AS $$
    declare

    oldTopNode		varchar(2000);
    newTopNode		varchar(2000);
    regex1		varchar(2000);
    regex2		varchar(2000);

begin

    oldTopNode := '\' || programName || '\' || oldTitle|| '\';
    newTopNode := '\' || programName || '\' || newTitle|| '\';
    regex1 := '\\' || replace(replace(programname, '(', '\('), ')', '\)') || '\\' || replace(replace(oldtitle, '(', '\('), ')', '\)')|| '\\' || '(.*)';
    regex2 := '\\' || programname || '\\' || newtitle|| '\\' || '\1';

    update i2b2metadata.i2b2
       set c_fullname=regexp_replace(c_fullname, regex1, regex2),
           c_dimcode=regexp_replace(c_dimcode, regex1, regex2),
           c_tooltip=regexp_replace(c_tooltip, regex1, regex2);
    update i2b2metadata.i2b2
       set c_name=newTitle where c_fullname=newTopNode;

    update i2b2metadata.i2b2_secure
       set c_fullname=regexp_replace(c_fullname, regex1, regex2),
           c_dimcode=regexp_replace(c_dimcode, regex1, regex2),
           c_tooltip=regexp_replace(c_tooltip, regex1, regex2);
    update i2b2metadata.i2b2_secure
       set c_name=newTitle where c_fullname=newTopNode;

    update i2b2metadata.tm_concept_counts
       set concept_path=regexp_replace(concept_path, regex1, regex2);
    update i2b2metadata.tm_concept_counts
       set parent_concept_path=regexp_replace(parent_concept_path, regex1, regex2);

    update i2b2demodata.concept_dimension
       set concept_path=regexp_replace(concept_path, regex1, regex2);
    update i2b2demodata.concept_dimension
       set name_char=newTitle where concept_path=newTopNode;

    commit;

END;

$$;


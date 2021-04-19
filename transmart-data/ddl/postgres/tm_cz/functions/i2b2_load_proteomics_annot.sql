--
-- Name: i2b2_load_proteomics_annot(numeric); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_proteomics_annot(currentjobid numeric DEFAULT NULL::numeric) RETURNS numeric
    LANGUAGE plpgsql SECURITY DEFINER
AS $$
    /*************************************************************************
     *This stored procedure is for ETL to load proteomics ANNOTATION
     * Date:10/29/2013
     ******************************************************************/

    declare

    --Audit variables
    newJobFlag NUMERIC(1);
    databaseName character varying(100);
    procedureName character varying(100);
    jobID numeric(18,0);
    stepCt numeric(18,0);
    gplId	character varying(100);
    errorNumber character varying;
    errorMessage character varying;
    rowCt integer;

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_proteomics_annot';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(jobID IS NULL or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName) into jobID;
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting I2B2_LOAD_PROTEOMICS_ANNOTATION',0,stepCt,'Done');

    --	get  id_ref  from external table

    select distinct gpl_id into gplId from tm_lz.lt_protein_annotation ;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from de_protein_annotation',rowCt,stepCt,'Done');
    --	delete any existing data from deapp.de_protein_annotation
    begin
	delete from deapp.de_subject_protein_data where protein_annotation_id in (select id from deapp.de_protein_annotation where gpl_id = gplId);
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;
    begin
	delete from deapp.de_protein_annotation
	 where gpl_id = gplId;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load annotation data into DEAPP de_protein_annotation',rowCt,stepCt,'Done');
    begin
	insert into  deapp.de_protein_annotation
		     (gpl_id
		     ,peptide
		     ,uniprot_id
		     ,biomarker_id
		     ,organism)
	select distinct d.gpl_id
			,trim(d.peptide)
			,d.uniprot_id
			,p.bio_marker_id
			,coalesce(d.organism,'Homo sapiens')
	  from tm_lz.lt_protein_annotation d
	       ,biomart.bio_marker p
	 where d.gpl_id = gplId
           and p.primary_external_id = d.uniprot_id
	    --  and coalesce(d.organism,'Homo sapiens') = coalesce(p.organism,'Homo sapiens')
	    -- and (d.gpl_id is not null or d.gene_symbol is not null)
	       ;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated missing uniprot_id in de_protein_annotation',rowCt,stepCt,'Done');

    begin
        update deapp.de_protein_annotation set uniprot_name = (select bio_marker_name
								 from biomart.bio_marker
								where biomart.bio_marker.primary_external_id = deapp.de_protein_annotation.uniprot_id)
         where gpl_id = gplId;
    exception
	when others then
	    perform tm_cz.cz_error_handler (jobID, procedureName, SQLSTATE, SQLERRM);
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    get diagnostics rowCt := ROW_COUNT;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Update uniprot_name in DEAPP de_protein_annotation',rowCt,stepCt,'Done');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_load_proteomics_annotation',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

exception
    when others then
	errorNumber := SQLSTATE;
	errorMessage := SQLERRM;
	perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	perform tm_cz.cz_end_audit (jobID, 'FAIL');

	return -16;

end;
$$;


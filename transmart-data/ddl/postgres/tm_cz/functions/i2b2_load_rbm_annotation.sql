--
-- Name: i2b2_load_rbm_annotation(bigint); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.i2b2_load_rbm_annotation(currentjobid bigint DEFAULT NULL::bigint) RETURNS numeric
    LANGUAGE plpgsql
AS $$
    declare

    /*************************************************************************
     * This is for RBM Annotation ETL for Sanofi
     * Date:12/05/2013
     ******************************************************************/

    --Audit variables
    newJobFlag numeric(1);
    databaseName character varying(100);
    procedureName character varying(100);
    jobID bigint;
    stepCt bigint;
    gplId	character varying(100);
    errorNumber		character varying;
    errorMessage	character varying;
    rowCt			numeric(18,0);

begin

    stepCt := 0;

    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := currentJobID;

    databaseName := 'tm_cz';
    procedureName := 'i2b2_load_rbm_annotation';

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    if(coalesce(jobID::text, '') = '' or jobID < 1) then
	newJobFlag := 1; -- True
	select tm_cz.cz_start_audit (procedureName, databaseName, jobID) into jobId;
    end if;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_load_rbm_annotation',0,stepCt,'Done');

    --	get GPL id from external table

    select distinct gpl_id into gplId from tm_lz.lt_src_rbm_annotation;

    --	delete any existing data from antigen_deapp

    begin
	delete from tm_cz.antigen_deapp
	 where platform = gplId;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from REFERENCE antigen_deapp',rowCt,stepCt,'Done');

    --	delete any existing data from annotation_deapp

    begin
	delete from tm_cz.annotation_deapp
	 where gpl_id = gplId;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from annotation_deapp',rowCt,stepCt,'Done');

    --	delete any existing data from deapp.de_mrna_annotation

    begin
	delete from deapp.de_rbm_annotation
	 where gpl_id = gplId;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from de_mrna_annotation',rowCt,stepCt,'Done');

    --	insert any new probesets into probeset_deapp
    begin
	insert into tm_cz.antigen_deapp (
	    antigen_name
	    ,platform)
	select
	    distinct antigen_name
	    ,gpl_id
	  from tm_lz.lt_src_rbm_annotation t
	 where not exists
	       (select 1 from tm_cz.antigen_deapp x
		 where t.gpl_id = x.platform
		   and t.antigen_name = x.antigen_name);
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert new probesets into antigen_deapp',rowCt,stepCt,'Done');

    --	insert data into annotation_deapp

    begin
	insert into tm_cz.annotation_deapp (
	    gpl_id
	    ,probe_id
	    ,gene_symbol
	    ,gene_id
	    ,probeset_id
	    ,organism)
	select
	    distinct d.gpl_id
	    ,d.uniprotid
	    ,d.gene_symbol
	    ,d.gene_id
	    ,p.antigen_id
	    ,'Homo sapiens'
	  from tm_lz.lt_src_rbm_annotation d
	       ,tm_cz.antigen_deapp p
	 where d.antigen_name = p.antigen_name
	   and d.gpl_id = p.platform
	   and ((d.gene_id IS NOT NULL AND d.gene_id::text <> '') or (d.gene_symbol IS NOT NULL AND d.gene_symbol::text <> '')) ;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load annotation data into REFERENCE annotation_deapp',rowCt,stepCt,'Done');

    --	insert data into deapp.de_rbm_annotation
    begin
	insert into deapp.de_rbm_annotation (
	    gpl_id
	    ,id
	    ,antigen_name
	    ,uniprot_id
	    ,gene_symbol
	    ,gene_id
	)
	select
	    distinct d.gpl_id
	    ,antigen_id
	    ,d.antigen_name
	    ,d.uniprotid
	    ,d.gene_symbol
	    ,CASE WHEN d.gene_id = null THEN null ELSE d.gene_id::numeric END as gene_id
	  from tm_lz.lt_src_rbm_annotation d
	       ,tm_cz.antigen_deapp p --check
	 where d.antigen_name = p.antigen_name
	   and d.gpl_id = p.platform;
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Load annotation data into DEAPP de_rbm_annotation',rowCt,stepCt,'Done');

    --	update gene_id if null
    begin
	update deapp.de_rbm_annotation t
	   set gene_id=(select min(b.primary_external_id)::numeric as gene_id
			  from biomart.bio_marker b
			 where t.gene_symbol = b.bio_marker_name
			  -- and upper(b.organism) = upper(t.organism)
			   and upper(b.bio_marker_type) = 'RBM')
	 where t.gpl_id = gplId
	       and coalesce(t.gene_id::text, '') = ''
	       and (t.gene_symbol IS NOT NULL AND t.gene_symbol::text <> '')
	       and exists
	       (select 1 from biomart.bio_marker x
		 where t.gene_symbol = x.bio_marker_name
	       --and upper(x.organism) = upper(t.organism)
		   and upper(x.bio_marker_type) = 'RBM');
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated missing gene_id in de_rbm_annotation',rowCt,stepCt,'Done');

    --	update gene_symbol if null
    begin
	update deapp.de_rbm_annotation t
	   set gene_symbol=(select min(b.bio_marker_name) as gene_symbol
			      from biomart.bio_marker b
			     where t.gene_id::varchar = b.primary_external_id
			      --  and upper(b.organism) = upper(t.organism)
			       and upper(b.bio_marker_type) = 'RBM')
	 where t.gpl_id = gplId
	       and coalesce(t.gene_symbol::text, '') = ''
	       and (t.gene_id is NOT NULL
		    and t.gene_id::text <> '')
	       and exists
	       (select 1 from biomart.bio_marker x
		 where t.gene_id::varchar = x.primary_external_id
	       --and upper(x.organism) = upper(t.organism)
		   and upper(x.bio_marker_type) = 'RBM');
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Updated missing gene_id in de_rbm_annotation',rowCt,stepCt,'Done');

    --	insert probesets into biomart.bio_assay_feature_group

    begin
	insert into biomart.bio_assay_feature_group (
	    feature_group_name
	    ,feature_group_type)
	select distinct t.uniprotid, 'PROTEIN'
	  from tm_lz.lt_src_rbm_annotation t
	 where not exists
	       (select 1 from biomart.bio_assay_feature_group x
		 where t.uniprotid = x.feature_group_name)
	   and (t.uniprotid is NOT NULL
		and t.uniprotid::text <> '');
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Insert probesets into biomart.bio_assay_feature_group',rowCt,stepCt,'Done');

    --	insert probesets into biomart.bio_assay_data_annotation
    begin
	insert into biomart.bio_assay_data_annotation (
	    bio_assay_feature_group_id
	    ,bio_marker_id)
	select
	    distinct fg.bio_assay_feature_group_id
	    ,coalesce(bgs.bio_marker_id,bgi.bio_marker_id)
	  from tm_lz.lt_src_rbm_annotation t
		   inner join biomart.bio_assay_feature_group fg
			   on t.uniprotid = fg.feature_group_name
		   left outer join biomart.bio_marker bgs
				      on t.gene_symbol = bgs.bio_marker_name
		   left outer join biomart.bio_marker bgi
				      on t.gene_id::varchar = bgi.primary_external_id
	 where ((t.gene_symbol is NOT NULL
		 and t.gene_symbol::text <> '')
		 or (t.gene_id is NOT NULL
		     and t.gene_id::text <> ''))
	   and coalesce(bgs.bio_marker_id,bgi.bio_marker_id,-1) > 0
	   and not exists
	       (select 1
		  from biomart.bio_assay_data_annotation x
		 where fg.bio_assay_feature_group_id = x.bio_assay_feature_group_id
		   and coalesce(bgs.bio_marker_id,bgi.bio_marker_id,-1) = x.bio_marker_id);
	get diagnostics rowCt := ROW_COUNT;
    exception
	when others then
	    errorNumber := SQLSTATE;
	    errorMessage := SQLERRM;
	--Handle errors.
	    perform tm_cz.cz_error_handler (jobID, procedureName, errorNumber, errorMessage);
	--End Proc
	    perform tm_cz.cz_end_audit (jobID, 'FAIL');
	    return -16;
    end;

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'Link feature_group to bio_marker in biomart.bio_assay_data_annotation',rowCt,stepCt,'Done');

    stepCt := stepCt + 1;
    perform tm_cz.cz_write_audit(jobId,databaseName,procedureName,'End i2b2_load_rbm_annotation',0,stepCt,'Done');

    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1 then
	perform tm_cz.cz_end_audit (jobID, 'SUCCESS');
    end if;

    return 1;

end;

$$;


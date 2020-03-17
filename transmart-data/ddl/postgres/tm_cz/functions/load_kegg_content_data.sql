--
-- Name: load_kegg_content_data(); Type: FUNCTION; Schema: tm_cz; Owner: -
--
CREATE OR REPLACE FUNCTION tm_cz.load_kegg_content_data() RETURNS void
    LANGUAGE plpgsql
AS $$
begin

    begin

	delete from biomart.bio_content_reference
	 where bio_content_id in
	       (select bio_file_content_id
		  from biomart.bio_content
		 where repository_id in
		       (select bio_content_repo_id
			  from biomart.bio_content_repository
			 where upper(repository_type)='KEGG')
	       );
	--806
	delete from biomart.bio_content
	 where repository_id =
	       (select bio_content_repo_id
		  from biomart.bio_content_repository
		 where upper(repository_type)='KEGG');
	--806
	delete from biomart.bio_content_repository
	 where upper(repository_type)='KEGG';
	--1
	commit;
    end;

    begin
	-- populate bio_content_repository
	insert into biomart.bio_content_repository(
	    location
	    ,active_y_n
	    ,repository_type
	    ,location_type
	)
	values (
	    'http://www.genome.jp/'
	    , 'Y'
	    , 'Kegg'
	    , 'URL'
	);
	commit;
    end;

    begin

	insert into biomart.bio_content(
	    --  file_name
	    repository_id
	    , location
	    --, title  , abstract
	    , file_type
	    --, etl_id
	)
	select distinct
	    bcr.bio_content_repo_id
	    ,bcr.location||'dbget-bin/show_pathway?'|| bm.primary_external_id
	    ,'Data'
	  from biomart.bio_content_repository bcr
	       ,biomart.bio_marker bm
	 where upper(bcr.repository_type)='KEGG'
	   and upper(bm.primary_source_code)='KEGG';
	--806 rows inserted
	commit;
    end;

    begin

	insert into biomart.bio_content_reference(
	    bio_content_id
	    ,bio_data_id
	    ,content_reference_type
	)
	select distinct
	    bc.bio_file_content_id
	    ,path.bio_marker_id
	    ,bcr.location_type
	  from biomart.bio_content bc
	       ,biomart.bio_marker path
	       ,biomart.bio_content_repository bcr
	 where bc.repository_id = bcr.bio_content_repo_id
	   and path.primary_external_id=substring(bc.location from length(bc.location)-7)
	   and path.primary_source_code='KEGG';
	--806
	commit;
    end;


end;

$$;


--
-- name: rbm_dbl_comp_qry(character varying, character varying, character varying, character varying, character varying, character varying, refcursor); Type: FUNCTION; Schema: deapp; Owner: -
--
CREATE FUNCTION rbm_dbl_comp_qry(patient_ids1 character varying, patient_ids2 character varying, concept_cds1 character varying, concept_cds2 character varying, timepoints1 character varying, timepoints2 character varying, INOUT cv_1 refcursor) RETURNS refcursor
    LANGUAGE plpgsql
AS $$
    DECLARE

    tp_cnt1 integer;
    tp_cnt2 integer;

BEGIN
    select count(*) into tp_cnt1
      from de_subject_sample_mapping
     where timepoint_cd in
	   (select * from table(text_parser(timepoints1)));

    select count(*) into tp_cnt2
      from de_subject_sample_mapping
     where timepoint_cd in
	   (select * from table(text_parser(timepoints2)));

    if ((tp_cnt1=0) and (tp_cnt2=0)) then
        open cv_1 for
            select distinct antigen_name
			    ,gene_symbol
			    ,zscore as value
			    ,'S1_' || patient_id as patient_id
			    ,assay_id
              from de_subject_rbm_data
             where patient_id in (select * from table(text_parser(patient_ids1)))
             union
            select distinct antigen_name
			    ,gene_symbol
			    ,zscore as value
			    ,'S2_' || patient_id as patient_id
			    ,assay_id
              from de_subject_rbm_data t1
             where patient_id in (select * from table(text_parser(patient_ids2)))
             order by antigen_name
		      ,gene_symbol
		      ,patient_id;

    elsif ((tp_cnt1=0) and (tp_cnt2 > 0)) then
        open cv_1 for
            select distinct antigen_name
			    ,gene_symbol
			    ,zscore as value
			    ,'S1_' || patient_id as patient_id
			    ,assay_id
              from de_subject_rbm_data t1
             where patient_id in (select * from table(text_parser(patient_ids1)))
             union
            select distinct t1.antigen_name
			    ,t1.gene_symbol
			    ,t1.zscore as value
			    ,'S1_' || t1.patient_id as patient_id
			    ,t1.assay_id
              from de_subject_rbm_data t1
		   ,de_subject_sample_mapping t2
             where t2.patient_id in (select * from table(text_parser(patient_ids2)))
	       and t2.timepoint_cd in (select * from TABLE(text_parser(timepoints2)))
		      and t1.data_uid = t2.data_uid
		      and t1.assay_id = t2.assay_id
             order by antigen_name
		      ,gene_symbol
		      ,patient_id;

    elsif ((tp_cnt1 > 0 ) and (tp_cnt2 = 0)) then
        open cv_1 for
            select distinct t1.antigen_name
			    ,t1.gene_symbol
			    ,t1.zscore as value
			    ,'S1_' || t1.patient_id as patient_id
			    ,t1.assay_id
              from de_subject_rbm_data t1
		   ,de_subject_sample_mapping t2
             where t2.patient_id in (select * from table(text_parser(patient_ids1)))
	       and t2.timepoint_cd in (select * from TABLE(text_parser(timepoints1)))
	       and t1.data_uid = t2.data_uid
	       and t1.assay_id=  t2.assay_id
             union
            select distinct t1.antigen_name
			    ,t1.gene_symbol
			    ,t1.zscore as value
			    ,'S2_' || patient_id as patient_id
			    ,t1.assay_id
              from de_subject_rbm_data t1
             where t1.patient_id in (select * from table(text_parser(patient_ids2)))
             order by antigen_name
		      ,gene_symbol
		      ,patient_id;
    else
        open cv_1 for
            select distinct t1.antigen_name
			    ,t1.gene_symbol
			    ,t1.zscore as value
			    ,'S1_' || t1.patient_id as patient_id
			    ,t1.assay_id
              from de_subject_rbm_data t1
		   ,de_subject_sample_mapping t2
             where t2.patient_id in (select * from table(text_parser(patient_ids1)))
	       and t2.timepoint_cd in (select * from table(text_parser(timepoints1)))
	       and t1.data_uid = t2.data_uid
	       and t1.assay_id = t2.assay_id
             union
            select distinct t1.antigen_name
			    ,t1.gene_symbol
			    ,t1.zscore as value
			    ,'S2_' || t1.patient_id as patient_id
			    ,t1.assay_id
              from de_subject_rbm_data t1
		   ,de_subject_sample_mapping t2
             where t2.patient_id in (select * from table(text_parser(patient_ids2)))
	       and t2.timepoint_cd in (select * from table(text_parser(timepoints2)))
	       and t1.data_uid = t2.data_uid
	       and t1.assay_id = t2.assay_id
             order by antigen_name
		      ,gene_symbol
		      ,patient_id;
    end if;

end;
$$;


--
-- Type: PROCEDURE; Owner: TM_CZ; Name: I2B2_PROTEIN_INC_SUB_ZSCORE
--
CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_PROTEIN_INC_SUB_ZSCORE (
    trial_id IN VARCHAR
)

AS

    TrialID		varchar2(100);

    coun number;

    cursor zscore_params is
	SELECT round(median(d.log_intensity),5)median_value,round(STDDEV(d.log_intensity),5)stddev_value,d.component
	  FROM deapp.de_subject_protein_data d
	 WHERE d.trial_name = trial_id
	 GROUP BY d.trial_name,d.component;

BEGIN

    --dbms_output.put_line('1');
    TrialID := upper(trial_id);
    coun:=0;

    -- call the cursor and update the z-score value for incremental data;
    for UpdateZscore in zscore_params
        loop

        update tm_cz.updatezscore_proteomics d
	   set d.zscore=(case when UpdateZscore.stddev_value=0 then 0
			 else ((d.log_intensity - UpdateZscore.median_value ) / UpdateZscore.stddev_value) end)
         where d.trial_name=TrialID
               and d.component=UpdateZscore.component;
        commit;

        /*coun:=coun+1;
          dbms_output.put_line(coun); */
    end loop;

    --dbms_output.put_line('12');
    --Normalize the  zscore value when greater than 2.5 and lesser than -2.5

    update deapp.de_subject_protein_data
       set zscore=round(case when zscore < -2.5 then -2.5
			when zscore >  2.5 then  2.5
			else round(zscore,5) end,5)
     where trial_name=TrialID;
    commit;

    -- dbms_output.put_line('111');
    -- select 0 into rtn_code from dual;

END I2B2_PROTEIN_INC_SUB_ZSCORE;
/

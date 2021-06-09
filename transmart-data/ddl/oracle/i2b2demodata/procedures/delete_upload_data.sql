--
-- Type: PROCEDURE; Owner: I2B2DEMODATA; Name: DELETE_UPLOAD_DATA
--
CREATE OR REPLACE PROCEDURE "I2B2DEMODATA"."DELETE_UPLOAD_DATA" (uploadId IN NUMBER) 
IS 
BEGIN

	--procedure to update upload_status table after upload is completed.

	-- delete from observation_fact for the given upload_id
	
	execute immediate '	delete from observation_fact where upload_id = '|| uploadId ||'';
	execute immediate '	delete encounter_mapping where encounter_num in (select encounter_num from visit_dimension where upload_id = '|| uploadId ||')';
	execute immediate ' delete visit_dimension where upload_id = '|| uploadId ||'';
	execute immediate ' UPDATE upload_status set load_status=''DELETED'' where upload_id = '|| uploadId ||'';
EXCEPTION
	WHEN OTHERS THEN
		dbms_output.put_line(SQLCODE|| ' - ' ||SQLERRM);
END;
/

package fr.sanofi.fcl4transmart.controllers.listeners.proteomicsData;


import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.job.Job;

import fr.sanofi.fcl4transmart.controllers.LoadDataListener;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.dataType.ProteomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.proteomicsData.ProteomicsLoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class ProteomicsLoadDataListener extends LoadDataListener implements Listener {
	public ProteomicsLoadDataListener(DataTypeItf dataType, ProteomicsLoadDataUI ui){
		super(ui, dataType);
	}
	@Override
	protected void setParameters() {
		storedProcedureLaunched=".*Starting entry \\[run i2b2_process_proteomics_data\\].*";
		storedProcedreEnded=".*Finished job entry \\[run i2b2_process_proteomics_data\\].*";
		queryStoredProcedureStarted="select max(JOB_ID) from CZ_JOB_AUDIT where STEP_DESC='Starting i2b2_process_proteomics_data'";
		queryStoredProcedureEnded="select * from cz_job_audit where (step_desc like '%End i2b2_process_proteomics_data%' or step_status='FAIL') and job_id=";
		unixDir="proteomics";
		checkPs=".*load_proteomics_data.*";
	}
	@Override
	protected String getJobPath() throws Exception{
		URL jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_proteomics_data.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl);  
		String jobPath = jobUrl.getPath();

		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/valid_proteomics_params.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_proteomics_columns.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/check_proteomics_filenames.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_all_proteomics_data.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_proteomics_sample_map_to_lt.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/get_list_of_proteomics_filenames.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_proteomics_expression_one_study.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/set_proteomics_filename.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/stream_lookup.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_proteomics_expression_data_to_lz.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl);
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/pivot_proteomics_file.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/run_i2b2_process_proteomics_data.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl);
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/write_proteomics_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_end_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_start_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_write_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl);
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/write_study_id_to_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl);
		
		return jobPath;
	}
	
	@Override
	protected void setJobMetadata(Job job) throws Exception{
		job.getJobMeta().setParameterValue("DATA_LOCATION", path);
		job.getJobMeta().setParameterValue("STUDY_ID", dataType.getStudy().toString());
		job.getJobMeta().setParameterValue("MAP_FILENAME", ((ProteomicsData)dataType).getMappingFile().getName());
		job.getJobMeta().setParameterValue("COLUMN_MAPPING_FILE", ((ProteomicsData)dataType).getColumnMappingFile().getName());
		String dimensionMap="";
		if(((HDDData)dataType).getDimFile()!=null) dimensionMap=((HDDData)dataType).getDimFile().getName();
		else dimensionMap=((HDDData)dataType).getMappingFile().getName();
		job.getJobMeta().setParameterValue("SAMPLE_MAP_FILENAME", dimensionMap);
		job.getJobMeta().setParameterValue("DATA_TYPE","R");
		job.getJobMeta().setParameterValue("SORT_DIR", sortName);
		job.getJobMeta().setParameterValue("TOP_NODE", topNode);
		if(((HDDData)dataType).isIncremental()) job.getJobMeta().setParameterValue("INC_LOAD", "Y");
		else job.getJobMeta().setParameterValue("INC_LOAD", "N");
		job.getJobMeta().setParameterValue("LOAD_TYPE", "I");						
		job.getJobMeta().setParameterValue("DATA_FILE_PREFIX", "raw");
		File sort=new File(sortName);
		if(!sort.exists()){
			FileUtils.forceMkdir(sort);
		}
		job.getJobMeta().setParameterValue("TM_CZ_DB_SERVER", PreferencesHandler.getDbServer());
		job.getJobMeta().setParameterValue("TM_CZ_DB_NAME", PreferencesHandler.getDbName());
		job.getJobMeta().setParameterValue("TM_CZ_DB_PORT", PreferencesHandler.getDbPort());
		job.getJobMeta().setParameterValue("TM_CZ_DB_USER", PreferencesHandler.getTm_czUser());
		job.getJobMeta().setParameterValue("TM_CZ_DB_PWD", PreferencesHandler.getTm_czPwd());
		job.getJobMeta().setParameterValue("TM_LZ_DB_SERVER",PreferencesHandler.getDbServer());
		job.getJobMeta().setParameterValue("TM_LZ_DB_NAME", PreferencesHandler.getDbName());
		job.getJobMeta().setParameterValue("TM_LZ_DB_PORT", PreferencesHandler.getDbPort());
		job.getJobMeta().setParameterValue("TM_LZ_DB_USER", PreferencesHandler.getTm_lzUser());
		job.getJobMeta().setParameterValue("TM_LZ_DB_PWD", PreferencesHandler.getTm_lzPwd());
		job.getJobMeta().setParameterValue("DEAPP_DB_SERVER",PreferencesHandler.getDbServer());
		job.getJobMeta().setParameterValue("DEAPP_DB_NAME", PreferencesHandler.getDbName());
		job.getJobMeta().setParameterValue("DEAPP_DB_PORT", PreferencesHandler.getDbPort());
		job.getJobMeta().setParameterValue("DEAPP_DB_USER", PreferencesHandler.getDeappUser());
		job.getJobMeta().setParameterValue("DEAPP_DB_PWD", PreferencesHandler.getDeappPwd());
		job.getJobMeta().setParameterValue("I2B2DEMODATA_DB_SERVER",PreferencesHandler.getDbServer());
		job.getJobMeta().setParameterValue("I2B2DEMODATA_DB_NAME", PreferencesHandler.getDbName());
		job.getJobMeta().setParameterValue("I2B2DEMODATA_DB_PORT", PreferencesHandler.getDbPort());
		job.getJobMeta().setParameterValue("I2B2DEMODATA_DB_USER", PreferencesHandler.getDeappUser());
		job.getJobMeta().setParameterValue("I2B2DEMODATA_DB_PWD", PreferencesHandler.getDeappPwd());
	}
	
	@Override
	protected String createUnixCommand() throws Exception{
		String fileLoc=c.pwd();

		String command=etlPreferences.getKettleDirectory()+"/kitchen.sh -norep=Y ";
		command+="-file="+etlPreferences.getJobsDirectory()+"/load_proteomics_data.kjb ";
		command+="-param:DATA_LOCATION="+fileLoc+" ";
		command+="-param:STUDY_ID="+dataType.getStudy().toString()+" ";
		command+="-param:COLUMN_MAPPING_FILE="+((ProteomicsData)dataType).getColumnMappingFile().getName()+" ";
		command+="-param:MAP_FILENAME="+((ProteomicsData)dataType).getMappingFile().getName()+" ";
		command+="-param:DATA_TYPE="+"R ";
		String dimensionMap="";
		if(((HDDData)dataType).getDimFile()!=null) dimensionMap=((HDDData)dataType).getDimFile().getName();
		else dimensionMap=((HDDData)dataType).getMappingFile().getName();
		command+="-param:SAMPLE_MAP_FILENAME="+dimensionMap+" ";
		if(((HDDData)dataType).isIncremental()) command+="-param:INC_LOAD="+ "Y ";
		else command+="-param:INC_LOAD="+ "N ";
		String sortPath="";
		try{
			c.cd(etlPreferences.getFilesDirectory()+"/.sort");
			sortPath=etlPreferences.getFilesDirectory()+"/.sort";
		}catch(Exception e){
			try{
				c.cd(etlPreferences.getFilesDirectory());
				c.mkdir(".sort");
				sortPath=etlPreferences.getFilesDirectory()+"/.sort";
			}catch(Exception e2){
				loadDataUI.displayMessage("Error when creating sort directory");
				return null;
			}
		}						
		command+="-param:SORT_DIR="+sortPath+" ";
		command+="-param:TOP_NODE='"+dataType.getStudy().getTopNode()+"' ";
		command+="-param:LOAD_TYPE=I ";
		command+="-param:DATA_FILE_PREFIX=raw ";
		command+="-param:JAVA_HOME=$JAVA_HOME";
		return command;
	}
}

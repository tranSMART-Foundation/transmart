/*******************************************************************************
 * Copyright (c) 2012 Sanofi-Aventis Recherche et Developpement.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    Sanofi-Aventis Recherche et Developpement - initial API and implementation
 ******************************************************************************/
package fr.sanofi.fcl4transmart.controllers.listeners.geneExpression;

import java.io.File;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.job.Job;

import fr.sanofi.fcl4transmart.controllers.LoadDataListener;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionData;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.workUI.geneExpression.GeneExpressionLoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

/**
 *This class controls the gene expression data loading step
 */	
public class LoadGeneExpressionDataListener extends LoadDataListener implements Listener{
	private String logBase;
	private String rawDataType;
	private boolean security;
	private boolean indexes;
	public LoadGeneExpressionDataListener(GeneExpressionLoadDataUI loadDataUI, DataTypeItf dataType){
		super(loadDataUI, dataType);
	}
	@Override
	protected void setParameters() {
		storedProcedureLaunched=".*Starting entry \\[run i2b2_process_mrna_data_inc\\].*";
		storedProcedreEnded=".*Finished job entry \\[run i2b2_process_mrna_data_inc\\].*";
		queryStoredProcedureStarted="select max(JOB_ID) from CZ_JOB_AUDIT where STEP_DESC='Starting i2b2_process_mrna_data'";
		queryStoredProcedureEnded="select * from cz_job_audit where (step_desc like '%End i2b2_process_mrna_data%' or step_status='FAIL') and job_id=";
		unixDir="gene";
		checkPs=".*load_gene_expression_data.*";
	}
	@Override
	protected  boolean validateInputs(){
		if(logBase!=null && logBase.compareTo("")!=0){
			try{
				@SuppressWarnings("unused")
				int n=Integer.valueOf(logBase);
			}catch(NumberFormatException ne){
				this.loadDataUI.displayMessage("The log base has to be a number");
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected String getJobPath() throws Exception{
		//find the kettle job to initiate the loading
		URL jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_gene_inc_expression_data.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl);  
		String jobPath = jobUrl.getPath();
		
		//find the other files needed for this job and put them in the cache
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_expression_params.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_inc_expression_params.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_expression_columns.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/check_gene_expression_filenames.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_all_gene_expression_files_for_study.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/run_i2b2_process_mrna_data.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_subject_sample_map_to_lt.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/get_list_of_gene_expression_filenames.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_gene_expression_one_study.kjb");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/set_gene_expression_filename.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_expression_columns.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_gene_expression_data_to_lz.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 

		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_inc_expression_columns.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/check_gene_inc_expression_filenames.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_all_gene_inc_expression_files_for_study.kjb");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/run_i2b2_process_mrna_data_inc.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_inc_subject_sample_map_to_lt.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/get_list_of_gene_inc_expression_filenames.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_gene_inc_expression_one_study.kjb");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/set_gene_inc_expression_filename.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/validate_gene_inc_expression_columns.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
                jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_gene_inc_expression_data_to_lz.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);

		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/pivot_gene_file.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/pivot_gene_inc_file.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_end_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_start_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/cz_write_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl);
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/write_study_id_to_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/write_gene_expression_audit.ktr");
		jobUrl = FileLocator.toFileURL(jobUrl); 
		jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/write_gene_inc_expression_audit.ktr");
                jobUrl = FileLocator.toFileURL(jobUrl);
		
		return jobPath;
	}
	
	@Override
	protected void setJobMetadata(Job job) throws Exception{
		logBase=((GeneExpressionLoadDataUI)loadDataUI).getLogBase();
		rawDataType=((GeneExpressionLoadDataUI)loadDataUI).getDataType();
		security=((GeneExpressionLoadDataUI)loadDataUI).getSecurity();
		indexes=((GeneExpressionLoadDataUI)loadDataUI).getIndexes();
		job.getJobMeta().setParameterValue("DATA_FILE_PREFIX", "raw");
		job.getJobMeta().setParameterValue("DATA_LOCATION", path);
		job.getJobMeta().setParameterValue("MAP_FILENAME", ((GeneExpressionData)dataType).getMappingFile().getName());
		String dimensionMap="";
		if(((HDDData)dataType).getDimFile()!=null) dimensionMap=((HDDData)dataType).getDimFile().getName();
		else dimensionMap=((GeneExpressionData)dataType).getMappingFile().getName();
		job.getJobMeta().setParameterValue("SAMPLE_MAP_FILENAME", dimensionMap);
		job.getJobMeta().setParameterValue("DATA_TYPE",rawDataType);
		job.getJobMeta().setParameterValue("LOG_BASE", logBase);
		job.getJobMeta().setParameterValue("FilePivot_LOCATION","");
		job.getJobMeta().setParameterValue("LOAD_TYPE", "I");
		job.getJobMeta().setParameterValue("SAMPLE_REMAP_FILENAME", "NOSAMPLEREMAP");
		job.getJobMeta().setParameterValue("SAMPLE_SUFFIX", ".rma-Signal");
		if(((HDDData)dataType).isIncremental()) job.getJobMeta().setParameterValue("INC_LOAD", "Y");
		else job.getJobMeta().setParameterValue("INC_LOAD", "N");
		if(security) job.getJobMeta().setParameterValue("SECURITY_REQUIRED", "Y");
		else job.getJobMeta().setParameterValue("SECURITY_REQUIRED", "N");
		job.getJobMeta().setParameterValue("SOURCE_CD", "STD");

		File sort=new File(sortName);
		if(!sort.exists()){
			FileUtils.forceMkdir(sort);
		}
		job.getJobMeta().setParameterValue("SORT_DIR", sortName);
		
		job.getJobMeta().setParameterValue("STUDY_ID", dataType.getStudy().toString());
		job.getJobMeta().setParameterValue("TOP_NODE", topNode);

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
	}
	
	@Override
	protected void preLoading() throws Exception{
		if(indexes){
			//drop indexes
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			
			String sql = "{call i2b2_mrna_index_maint(?)}";
			CallableStatement call = con.prepareCall(sql);
			call.setString(1,"DROP");
			call.executeUpdate();

			con.close();
		}
	}
	
	@Override
	protected void postLoading() throws Exception{

		if(indexes){
			//add indexes
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			
			String sql =  "begin i2b2_mrna_index_maint(?); end;" ; // stored proc
			CallableStatement call = con.prepareCall(sql);
			call.setString(1,"ADD");
			try{
				call.executeUpdate();
			}catch(SQLException e){
				e.printStackTrace();
			}
			
			con.close();
		}
	}
	
	@Override
	protected String createUnixCommand() throws Exception{
		String fileLoc=c.pwd();

		String command=etlPreferences.getKettleDirectory()+"/kitchen.sh -norep=Y ";
		command+="-file="+etlPreferences.getJobsDirectory()+"/load_gene_expression_data.kjb ";
		command+="-param:DATA_FILE_PREFIX=raw ";
		command+="-param:DATA_LOCATION="+fileLoc+" ";
		command+="-param:DATA_TYPE="+rawDataType+" ";
		command+="-param:LOG_BASE="+logBase+" ";
		command+="-param:FilePivot_LOCATION="+etlPreferences.getJobsDirectory()+" ";
		command+="-param:LOAD_TYPE=I ";
		command+="-param:MAP_FILENAME="+((GeneExpressionData)dataType).getMappingFile().getName()+" ";
		command+="-param:SAMPLE_REMAP_FILENAME=NOSAMPLEREMAP ";
		command+="-param:SAMPLE_SUFFIX=.rma-Signal ";
		if(security){
			command+="-param:SECURITY_REQUIRED=Y ";
		}else{
			command+="-param:SECURITY_REQUIRED=N ";
		}
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
		command+="-param:SOURCE_CD=STD ";
		command+="-param:STUDY_ID="+dataType.getStudy().toString()+" ";
		command+="-param:TOP_NODE='"+dataType.getStudy().getTopNode()+"' ";
		command+="-param:JAVA_HOME=$JAVA_HOME";
		return command;
	}
}

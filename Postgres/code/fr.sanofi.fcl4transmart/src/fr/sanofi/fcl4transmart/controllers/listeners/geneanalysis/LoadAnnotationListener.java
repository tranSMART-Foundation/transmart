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
package fr.sanofi.fcl4transmart.controllers.listeners.geneanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import fr.sanofi.fcl4transmart.controllers.StudySelectionController;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.LoadAnnotationUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class controls the platform annotation loading 
 */	
public class LoadAnnotationListener implements Listener{
	private LoadAnnotationUI loadAnnotationUI;
	private String pathToFile;
	private String platformId;
	private String annotationDate;
	private String annotationRelease;
	private String annotationTitle;
	private DataTypeItf dataType;
	private ChannelSftp c;
	public LoadAnnotationListener(LoadAnnotationUI loadAnnotationUI, DataTypeItf dataType){
		this.loadAnnotationUI=loadAnnotationUI;
		this.dataType=dataType;
	}
	/**
	 *Loads the annotation:
	 *-initiate Kettle environment
	 *-Find Kettle files
	 *-Set Kettle parameters
	 *-Calls the Kettle job
	 *-Save the log file
	 */	
	@Override
	public void handleEvent(Event event) {
		this.pathToFile=this.loadAnnotationUI.getPathToFile();
		this.platformId=this.loadAnnotationUI.getPlatformId();
		this.annotationDate=this.loadAnnotationUI.getAnnotationDate();
		this.annotationRelease=this.loadAnnotationUI.getAnnotationRelease();
		this.annotationTitle=this.loadAnnotationUI.getAnnotationTitle();
		this.loadAnnotationUI.openLoadingShell();
		Thread thread=new Thread(){
			public void run() {
				if(!loadAnnotationUI.getEtlServer()){
					try {  
						//initiate kettle environment
						URL kettleUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/lib/pentaho");
						kettleUrl = FileLocator.toFileURL(kettleUrl);  
						System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", kettleUrl.getPath());
						KettleEnvironment.init(false);
						
						LanguageChoice language=LanguageChoice.getInstance();
						language.setDefaultLocale(Locale.US);	
						
						//find the kettle job to initiate the loading
						URL jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_annotation.kjb");
						jobUrl = FileLocator.toFileURL(jobUrl);  
						String jobPath = jobUrl.getPath();
						//create a new job from the kettle file
						JobMeta jobMeta = new JobMeta(jobPath, null);
						Job job = new Job(null, jobMeta);		
						
						//find the other files needed for this job and put them in the cache
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/extract_AFFY_annotation_from_file.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/extract_GEO_annotation_from_file.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_annotation_to_lt.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/run_i2b2_load_annotation_deapp.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/extract_annotation_from_file.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
						jobUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/load_annotation_to_de_gpl_info.ktr");
						jobUrl = FileLocator.toFileURL(jobUrl); 
					
						job.getJobMeta().setParameterValue("DATA_LOCATION", pathToFile);
			
						File sort=new File(StudySelectionController.getWorkspace().getAbsoluteFile()+File.separator+".sort");
						if(!sort.exists()){
							FileUtils.forceMkdir(sort);
						}
						job.getJobMeta().setParameterValue("SORT_DIR", sort.getAbsolutePath());
						job.getJobMeta().setParameterValue("DATA_SOURCE", "A");
						//check if gpl id is not empty
						if(platformId==null){
							loadAnnotationUI.setMessage("Please provide the platform identifier");
						    loadAnnotationUI.setIsLoading(false);
						    return;
						}
						job.getJobMeta().setParameterValue("GPL_ID", platformId);
						job.getJobMeta().setParameterValue("SKIP_ROWS","1");
						job.getJobMeta().setParameterValue("GENE_ID_COL","4");
						job.getJobMeta().setParameterValue("GENE_SYMBOL_COL","3");
						job.getJobMeta().setParameterValue("ORGANISM_COL","5");
						job.getJobMeta().setParameterValue("PROBE_COL","2");
						if(annotationDate!=null){
							job.getJobMeta().setParameterValue("ANNOTATION_DATE", annotationDate);
						}
						if(annotationRelease!=null){
							job.getJobMeta().setParameterValue("ANNOTATION_RELEASE", annotationRelease);
						}
						//check if annotation title is not empty
						if(annotationTitle==null){
							loadAnnotationUI.setMessage("Please provide the annotation title");
						    loadAnnotationUI.setIsLoading(false);
						    return;
						}
						job.getJobMeta().setParameterValue("ANNOTATION_TITLE", annotationTitle);			
						job.getJobMeta().setParameterValue("LOAD_TYPE", "I");
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
						job.getJobMeta().setParameterValue("DEAPP_DB_SERVER", PreferencesHandler.getDbServer());
						job.getJobMeta().setParameterValue("DEAPP_DB_NAME", PreferencesHandler.getDbName());
						job.getJobMeta().setParameterValue("DEAPP_DB_PORT", PreferencesHandler.getDbPort());
						job.getJobMeta().setParameterValue("DEAPP_DB_USER", PreferencesHandler.getDeappUser());
						job.getJobMeta().setParameterValue("DEAPP_DB_PWD", PreferencesHandler.getDeappPwd());		
						
						job.start();
						job.waitUntilFinished(3000000);
						job.interrupt();
						
						@SuppressWarnings("unused")
						Result result = job.getResult();
						
						Log4jBufferAppender appender = CentralLogStore.getAppender();
						String logText = appender.getBuffer(job.getLogChannelId(), false).toString();
						
						Pattern pattern=Pattern.compile(".*run_i2b2_load_annotation_deapp - Dispatching started for transformation \\[run_i2b2_load_annotation_deapp\\].*", Pattern.DOTALL);
						Matcher matcher=pattern.matcher(logText);
						if(matcher.matches()){
							String connection=RetrieveData.getConnectionString();
							Connection con = DriverManager.getConnection(connection, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
							Statement stmt = con.createStatement();
							
							//remove rows for this study before adding new ones
							ResultSet rs=stmt.executeQuery("select max(JOB_ID) from CZ_JOB_AUDIT where STEP_DESC='Starting i2b2_load_annotation_deapp'");
							int jobId;
							if(rs.next()){
								jobId=rs.getInt("max(JOB_ID)");
							}
							else{
								con.close();
								loadAnnotationUI.setIsLoading(false);
								return;
							}
							
							logText+="\nOracle job id:\n"+String.valueOf(jobId);
							rs=stmt.executeQuery("select job_status from cz_job_master where job_id="+String.valueOf(jobId));
							if(rs.next()){
								if(rs.getString("job_status").compareTo("Running")==0){
									loadAnnotationUI.setMessage("Kettle job time out because the stored procedure is not over. Please check in a while if loading has succeed");
									loadAnnotationUI.setIsLoading(false);
									return;
								}
							}
							rs=stmt.executeQuery("select ERROR_MESSAGE from CZ_JOB_ERROR where JOB_ID="+String.valueOf(jobId));
							String procedureErrors="";
							if(rs.next()){
								procedureErrors=rs.getString("ERROR_MESSAGE");
							}
							con.close();
							if(procedureErrors.compareTo("")==0){
								loadAnnotationUI.setMessage("Platform annotation has been loaded");
							}
							else{
								loadAnnotationUI.setMessage("Error during procedure: "+procedureErrors);
							}
						}
						else{
							loadAnnotationUI.setMessage("Error in Kettle job: see log file");
						}
						
						writeLog(logText);
						CentralLogStore.discardLines(job.getLogChannelId(), false);
						
						//
						loadAnnotationUI.setIsLoading(false);
					} 
					catch (Exception e1) {
						loadAnnotationUI.setMessage("Error: "+e1.getLocalizedMessage());
						loadAnnotationUI.setIsLoading(false);
						e1.printStackTrace();
					}
				}else{//use ETL server
					if(platformId==null){
						loadAnnotationUI.setMessage("Please provide the platform identifier");
						loadAnnotationUI.setIsLoading(false);
						return;
					}
					if(annotationTitle==null){
						loadAnnotationUI.setMessage("Please provide the annotation title");
						loadAnnotationUI.setIsLoading(false);
						return;
					}
					try{
						JSch jsch=new JSch();
						Session session=jsch.getSession(etlPreferences.getUser(), etlPreferences.getHost(), Integer.valueOf(etlPreferences.getPort()));
						session.setPassword(etlPreferences.getPass());
					 
						java.util.Properties config = new java.util.Properties(); 
						config.put("StrictHostKeyChecking", "no");
						config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
						session.setConfig(config);
						
						session.connect();
					 
						Channel channel=session.openChannel("sftp");
						channel.connect();
						c=(ChannelSftp)channel;
										
						//try to go to the right directory for file transfer
						String dir=etlPreferences.getFilesDirectory();
						if(dir.compareTo("")!=0){
							try{
								c.cd(dir);
							}catch(SftpException e){
								loadAnnotationUI.setMessage("The file directory does not exist in this server");
								loadAnnotationUI.setIsLoading(false);
								return;
							}
						}else{
							loadAnnotationUI.setMessage("No file directory is indicated");
							loadAnnotationUI.setIsLoading(false);
							return;
						}
						try{
							try{
								c.mkdir(dataType.getStudy().toString());
							}catch(Exception exist){
								//normal if directory already exists
							}
							c.cd(dataType.getStudy().toString());
							try{
								c.mkdir("annotation");
							}catch(Exception exist2){
								//normal if directory already exists
							}
							c.cd("annotation");
						}catch(SftpException e){
							loadAnnotationUI.setMessage("Directory can not be created");
							loadAnnotationUI.setIsLoading(false);
		            	   	return;
						}

						File f=new File(pathToFile);
						try{
							c.put(f.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
													
						}catch(SftpException e){
							loadAnnotationUI.setMessage("Error when transferring files");
							loadAnnotationUI.setIsLoading(false);
							return;
						}
						
						//run Kettle script
						String fileLoc="";
						try {
							fileLoc=c.pwd();
						} catch (SftpException e) {
							e.printStackTrace();
							loadAnnotationUI.setIsLoading(false);
							return;
						}
						String command=etlPreferences.getKettleDirectory()+"/kitchen.sh -norep=Y ";
						command+="-file="+etlPreferences.getJobsDirectory()+"/load_annotation.kjb ";
						command+="-param:DATA_LOCATION="+fileLoc+"  ";
						command+="-param:SOURCE_FILENAME="+f.getName()+" ";
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
								e2.printStackTrace();
								loadAnnotationUI.setMessage("Error when creating sort directory");
								loadAnnotationUI.setIsLoading(false);
								return;
							}
						}
						command+="-param:SORT_DIR="+sortPath+" ";
						command+="-param:DATA_SOURCE=A ";
						command+="-param:GPL_ID="+platformId+"  ";
						command+="-param:SKIP_ROWS=1 ";
						command+="-param:GENE_ID=4 ";
						command+="-param:GENE_SYMBOL_COL=3 ";
						command+="-param:ORGANISM_COL=5 ";
						command+="-param:PROBE_COL=2 ";
						if(annotationDate!=null){
							command+="-param:ANNOTATION_DATE="+annotationDate+"  ";
						}
						if(annotationRelease!=null){
							command+="-param:ANNOTATION_RELEASE="+annotationRelease+"  ";
						}
						command+="-param:ANNOTATION_TITLE="+annotationTitle+"  ";
						command+="-param:LOAD_TYPE=I ";

						
						//close streams
						channel.disconnect();
						
						try{
							channel=session.openChannel("exec");
					      ((ChannelExec)channel).setCommand(command);
						 
					      channel.setInputStream(null);					      					 
					      InputStream in=channel.getInputStream();
					 
					      channel.connect();
					 
					      String out="";
					      boolean began=false;
					      byte[] tmp=new byte[1024];
					      boolean running=true;
					      while(running){
					        while(in.available()>0){
					          int i=in.read(tmp, 0, 1024);
					          if(i<0)break;
						          out+=new String(tmp, 0, i);
						          began=true;
					        	}
					        	if(began){
					        		Channel channel2=session.openChannel("exec");
								    ((ChannelExec)channel2).setCommand("ps -u "+etlPreferences.getUser()+" -U "+etlPreferences.getUser()+" u");
								    channel2.setInputStream(null);
				      				InputStream in2=channel2.getInputStream();
				      				channel2.connect();
				      				String out2="";
				      				byte[] tmp2=new byte[1024];
				      				while(true){
				      					while(in2.available()>0){
				      						int i=in2.read(tmp2, 0, 1024);
				      						if(i<0)break;
				      						out2+=new String(tmp2, 0, i);
				      					}
				      					if(channel2.isClosed()){
				      						break;
				      					}
								        try{Thread.sleep(1000);}catch(Exception ee){
								        	ee.printStackTrace();
								        }
				      				}
				      				channel2.disconnect();
				      				 Pattern pattern=Pattern.compile(".*load_annotation.*", Pattern.DOTALL);
								     Matcher matcher=pattern.matcher(out2);
								     if(!matcher.matches()){
								    	 running=false;
								     }
					        	}
						        try{Thread.sleep(1000);}
						        catch(Exception ee){
						        	ee.printStackTrace();
						        }
					        }
					      
					      Pattern pattern=Pattern.compile(".*run_i2b2_load_annotation_deapp - Dispatching started for transformation \\[run_i2b2_load_annotation_deapp\\].*", Pattern.DOTALL);
							Matcher matcher=pattern.matcher(out);
							if(matcher.matches()){
								String connection=RetrieveData.getConnectionString();
								Connection con = DriverManager.getConnection(connection, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
								Statement stmt = con.createStatement();
								
								//remove rows for this study before adding new ones
								ResultSet rs=stmt.executeQuery("select max(JOB_ID) from CZ_JOB_AUDIT where STEP_DESC='Starting i2b2_load_annotation_deapp'");
								int jobId;
								if(rs.next()){
									jobId=rs.getInt("max(JOB_ID)");
								}
								else{
									con.close();
									loadAnnotationUI.setIsLoading(false);
									return;
								}
								
								out+="\nOracle job id:\n"+String.valueOf(jobId);
								rs=stmt.executeQuery("select job_status from cz_job_master where job_id="+String.valueOf(jobId));
								if(rs.next()){
									if(rs.getString("job_status").compareTo("Running")==0){
										loadAnnotationUI.setMessage("Kettle job timed out");
										loadAnnotationUI.setIsLoading(false);
										return;
									}
								}
								rs=stmt.executeQuery("select ERROR_MESSAGE from CZ_JOB_ERROR where JOB_ID="+String.valueOf(jobId));
								String procedureErrors="";
								if(rs.next()){
									procedureErrors=rs.getString("ERROR_MESSAGE");
								}
								con.close();
								if(procedureErrors.compareTo("")==0){
									loadAnnotationUI.setMessage("Platform annotation has been loaded");
								}
								else{
									loadAnnotationUI.setMessage("Error during procedure: "+procedureErrors);
								}
							}
							else{
								loadAnnotationUI.setMessage("Error in Kettle job: see log file");
							}
							writeLog(out);
							
					      channel.disconnect();
					      session.disconnect();
					    }
					    catch(Exception e){
					    	e.printStackTrace();
							loadAnnotationUI.setMessage("Error when executing Kettle command");
							loadAnnotationUI.setIsLoading(false);
							return;
					    }
						
						session.disconnect();
						channel.disconnect();
					
				}catch (Exception e1){
					e1.printStackTrace();
					loadAnnotationUI.setMessage("Error when transferring files");
					loadAnnotationUI.setIsLoading(false);
					return;
				}
				}
				loadAnnotationUI.setIsLoading(false);
			}
		};
		thread.start();
		this.loadAnnotationUI.waitForThread();
		WorkPart.updateSteps();
		WorkPart.updateFiles();
	}
	
	/**
	 *Write a given string corresponding to Kettle log in a log file
	 */	public void writeLog(String text)
	{
		File log=new File(dataType.getPath()+File.separator+"annotation.kettle.log");
		try
		{
			FileWriter fw = new FileWriter(log);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(text);		
			output.close();
			((GeneExpressionAnalysis)dataType).setKettleLog(log);
		}
		catch(IOException ioe){
			loadAnnotationUI.setMessage("File error: "+ioe.getLocalizedMessage());
			ioe.printStackTrace();
		}
	}
}

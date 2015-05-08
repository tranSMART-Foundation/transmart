package fr.sanofi.fcl4transmart.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.workUI.LoadAnnotationUIItf;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public abstract class LoadAnnotationListener implements Listener {
	protected LoadAnnotationUIItf loadAnnotationUI;
	protected DataTypeItf dataType;
	protected ChannelSftp c;
	protected String storedProcedureLaunched;
	protected String storedProcedreEnded;
	protected String queryStoredProcedureStarted;
	protected String queryStoredProcedureEnded;
	protected String unixDir;
	protected String checkPs;
	protected Channel channel;
	protected Session session;
	protected String pathToFile;
	protected String platformId;
	protected String annotationTitle;
	protected String newPath;
	protected String sortName;
	protected boolean success;
	public LoadAnnotationListener(LoadAnnotationUIItf loadAnnotationUI, DataTypeItf dataType){
		this.dataType=dataType;
		this.loadAnnotationUI=loadAnnotationUI;
		success=true;
	}
	@Override
	public void handleEvent(Event event) {
		this.sortName=this.dataType.getStudy().getPath().getParentFile().getAbsolutePath()+File.separator+".sort";
		this.pathToFile=this.loadAnnotationUI.getPathToFile();
		this.platformId=this.loadAnnotationUI.getPlatformId();
		this.annotationTitle=this.loadAnnotationUI.getAnnotationTitle();
		if(!validateInputs()) return;
		this.setParameters();
		
		this.loadAnnotationUI.openLoadingShell();
		Thread thread=new Thread(){
			public void run() {
				if(copyFile()){
					if(!loadAnnotationUI.getEtlServer()){
						try{
							inititateKettle();
							
							JobMeta jobMeta = new JobMeta(getJobPath(), null);
							Job job = new Job(null, jobMeta);
							setJobMetadata(job);
							
							launchKettle(job);
							Log4jBufferAppender appender = CentralLogStore.getAppender();
							String logText = appender.getBuffer(job.getLogChannelId(), false).toString();
							writeLog(logText);
							CentralLogStore.discardLines(job.getLogChannelId(), false);
						} 
						catch (Exception e1) {
							e1.printStackTrace();
							loadAnnotationUI.setMessage("Kettle exception: "+e1.getLocalizedMessage());
							loadAnnotationUI.setIsLoading(false);
							success=false;
							return;
						}
					}else{
						try{
							transferFiles();
							String command=createUnixCommand();
							launchUnixCommand(command);
						}catch (Exception e1) {
							e1.printStackTrace();
							loadAnnotationUI.setMessage("Kettle exception: "+e1.getLocalizedMessage());
							loadAnnotationUI.setIsLoading(false);
							success=false;
							return;
						}
					}

					if(success) loadAnnotationUI.setMessage("Loading process is over.");
				}
				loadAnnotationUI.setIsLoading(false);
			}
		};
		thread.start();
		this.loadAnnotationUI.waitForThread();
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		WorkPart.filesChanged(dataType);
	}
	protected abstract String getJobPath() throws Exception;
	protected abstract void setJobMetadata(Job job) throws Exception;
	protected abstract String createUnixCommand()throws Exception;
	protected abstract void setParameters();
	
	protected boolean validateInputs(){
		if(this.platformId==null){
			this.loadAnnotationUI.setMessage("Please provide the platform identifier");
		    return false;
		}if(this.annotationTitle==null){
			this.loadAnnotationUI.setMessage("Please provide the platform title");
		    return false;
		}if(this.pathToFile==null){
			this.loadAnnotationUI.setMessage("Please provide the platform file path");
		    return false;
		}
		return true;
	}
	protected boolean copyFile(){
		File copiedFile=new File(newPath);
		File file=new File(pathToFile);
		if(copiedFile.exists()){
			copiedFile.delete();
		}
		try {
			Utils.copyFile(file, copiedFile);
			((HDDData)dataType).setAnnotationFile(copiedFile);
		} catch (IOException e) {
			if(copiedFile.exists()) copiedFile.delete();
			loadAnnotationUI.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	protected void inititateKettle() throws Exception{
		GlobalMessages.setLocale(EnvUtil.createLocale("en-US"));
		URL kettleUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/lib/pentaho");
		kettleUrl = FileLocator.toFileURL(kettleUrl);  
		System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", kettleUrl.getPath());
		KettleEnvironment.init(false);
		LanguageChoice language=LanguageChoice.getInstance();
		language.setDefaultLocale(Locale.US);	
	}
	protected void launchKettle(Job job) throws Exception{
		job.start();
		
		//wait for kettle to finish
		//job.waitUntilFinished() don't always finish, so there are several verification steps to see if loading is over
		boolean kettleFinished=false;
		
		while(!job.isFinished() && !kettleFinished){
			Log4jBufferAppender appender = CentralLogStore.getAppender();
			String logText="";
			try{
				logText = appender.getBuffer(job.getLogChannelId(), false).toString();
			}catch(ConcurrentModificationException e){
				//do nothing
			}
			Pattern pattern=Pattern.compile(storedProcedureLaunched, Pattern.DOTALL);
			Matcher matcher=pattern.matcher(logText);
			if(matcher.matches()){
			 	job.waitUntilFinished(2*60*1000);
			 	kettleFinished=true;
			}
		}
		
		if(!job.isFinished()){
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			Statement stmt = con.createStatement();
			
			ResultSet rs=stmt.executeQuery(queryStoredProcedureStarted);
			int jobId;
			if(rs.next()){
				jobId=rs.getInt("max(JOB_ID)");
			}
			else{
				con.close();
				return;
			}
			boolean procedureFinished=false;
			while(!job.isFinished() && !procedureFinished){
				rs=stmt.executeQuery(queryStoredProcedureEnded+jobId);
				if(rs.next()){
					procedureFinished=true;
				}
				job.waitUntilFinished(60*1000);
			}
			con.close();
		}	
	}
	protected void launchUnixCommand(String command) throws Exception{
		channel=session.openChannel("exec");
		((ChannelExec)channel).setCommand(command);
		
		channel.setInputStream(null);
		InputStream in=channel.getInputStream();
		channel.connect();

		String out="";
		byte[] tmp=new byte[1024];
		boolean running=true;
		boolean began=false;
		while(running){
	        while(in.available()>0){
	          int i=in.read(tmp, 0, 1024);
	          if(i<0)break;
	          out+=new String(tmp, 0, i);
	          began=true;
	        }
	        if(began){
        		running=checkRunnningWithPs();
        	}else{
        		try{Thread.sleep(1000);}catch(Exception ee){}
        		if(checkRunnningWithPs()) began=true;
        		else{
        			loadAnnotationUI.setMessage("Error when launching command");
	    			loadAnnotationUI.setIsLoading(false);
	    			success=false;
	    			return;
        		}
        	}
	        try{Thread.sleep(1000);}catch(Exception ee){}
	      }
		BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
		String s;
	  while ((s = inReader.readLine()) != null) {
		  out+=s+"\n";
	  }
		
		writeLog(out);
		
		channel.disconnect();
		session.disconnect();
	}
	protected void transferFiles() throws NumberFormatException, JSchException{
		JSch jsch=new JSch();
		session=jsch.getSession(etlPreferences.getUser(), etlPreferences.getHost(), Integer.valueOf(etlPreferences.getPort()));
		session.setPassword(etlPreferences.getPass());
	 
		java.util.Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setConfig(config);
		
		session.connect();
	 
		channel=session.openChannel("sftp");
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
				success=false;
				return;
			}
		}else{
			loadAnnotationUI.setMessage("No file directory is indicated");
			loadAnnotationUI.setIsLoading(false);
			success=false;
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
				c.mkdir(unixDir);
			}catch(Exception exist2){
				//normal if directory already exists
			}
			c.cd(unixDir);
		}catch(SftpException e){
			loadAnnotationUI.setMessage("Directory can not be created");
			loadAnnotationUI.setIsLoading(false);
			success=false;
    	   	return;
		}

		try{
			c.put(pathToFile, ".", null, ChannelSftp.OVERWRITE);
			
		}catch(SftpException e){
			loadAnnotationUI.setMessage("Error when transferring files");
    	   loadAnnotationUI.setIsLoading(false);
    	   return;
		}
	}
	
	public void writeLog(String text) throws Exception
	{
		Pattern pattern=Pattern.compile(storedProcedureLaunched, Pattern.DOTALL);
		Matcher matcher=pattern.matcher(text);
		if(matcher.matches()){
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			Statement stmt = con.createStatement();
			
			//remove rows for this study before adding new ones
			ResultSet rs=stmt.executeQuery(queryStoredProcedureStarted);
			int jobId=-1;
			if(rs.next()){
				jobId=rs.getInt("max(JOB_ID)");
				text+="\nOracle job id:\n"+String.valueOf(jobId);
			}
			else{
				loadAnnotationUI.setMessage("No job id found for this loading");
				success=false;
				con.close();
				return;
			}
			rs=stmt.executeQuery("select ERROR_MESSAGE from CZ_JOB_ERROR where JOB_ID="+String.valueOf(jobId));
			String procedureErrors="";
			if(rs.next()){
				procedureErrors=rs.getString("ERROR_MESSAGE");
			}
			con.close();
			if(procedureErrors.compareTo("")!=0){
				loadAnnotationUI.setMessage("Error during procedure: "+procedureErrors);
				success=false;
			}
		}else{
			loadAnnotationUI.setMessage("Error in Kettle job: see log file");
			success=false;
		}
		
		File log=new File(dataType.getPath()+File.separator+"annotation.kettle.log");
		try
		{
			FileWriter fw = new FileWriter(log);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(text);		
			output.close();
			loadAnnotationUI.setAnnotationLogFile(log);
		}
		catch(IOException ioe){
			loadAnnotationUI.setMessage("File error: "+ioe.getLocalizedMessage());
			ioe.printStackTrace();
		}
	}
	private boolean checkRunnningWithPs() throws JSchException, IOException{
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
			 Pattern pattern=Pattern.compile(".*kitchen.*", Pattern.DOTALL);
	     Matcher matcher=pattern.matcher(out2);
	     if(!matcher.matches()){
	    	 return false;
	     }
	     return true;
	}
}

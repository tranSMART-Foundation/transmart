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
import java.sql.SQLException;
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
import fr.sanofi.fcl4transmart.model.classes.workUI.LoadDataUIItf;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public abstract class LoadDataListener implements Listener {
	protected LoadDataUIItf loadDataUI;
	protected DataTypeItf dataType;
	protected String topNode;
	protected String path;
	protected String sortName;
	protected ChannelSftp c;
	protected String storedProcedureLaunched;
	protected String storedProcedreEnded;
	protected String queryStoredProcedureStarted;
	protected String queryStoredProcedureEnded;
	protected String unixDir;
	protected String checkPs;
	protected Channel channel;
	protected Session session;
	public LoadDataListener(LoadDataUIItf loadDataUI, DataTypeItf dataType){
		this.dataType=dataType;
		this.loadDataUI=loadDataUI;
		this.setParameters();
	}
	@Override
	public void handleEvent(Event event) {
		this.topNode=this.loadDataUI.getTopNode();
		this.path=this.dataType.getPath().getAbsolutePath();
		this.sortName=this.dataType.getStudy().getPath().getParentFile().getAbsolutePath()+File.separator+".sort";
		
		if(!validateInputs()) return;
		if(!validateTopNode()) return;
		loadDataUI.openLoadingShell();
		new Thread(){
			public void run() {
				createAnalyzeTree();
				if(!loadDataUI.getEtlServer()){
					try{
						inititateKettle();
						
						JobMeta jobMeta = new JobMeta(getJobPath(), null);
						Job job = new Job(null, jobMeta);
						setJobMetadata(job);
						
						preLoading();
						launchKettle(job);
						writeLog(job);
						postLoading();
					} 
					catch (Exception e1) {
						e1.printStackTrace();
						loadDataUI.setMessage("Kettle exception: "+e1.getLocalizedMessage());
						loadDataUI.setIsLoading(false);
						return;
					}
					
				}else{//etl server is used
					try{
						preLoading();
						transferFiles();
						String command=createUnixCommand();
						launchUnixCommand(command);
						
						postLoading();
					}catch (Exception e1) {
						e1.printStackTrace();
						loadDataUI.setMessage("Kettle exception: "+e1.getLocalizedMessage());
						loadDataUI.setIsLoading(false);
						return;
					}
				}
				loadDataUI.setIsLoading(false);
				loadDataUI.setMessage("Loading process is over.\n Please check monitoring step.");
			}
		}.start();
		this.loadDataUI.waitForThread();
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		WorkPart.filesChanged(dataType);
	}
	
	protected abstract String getJobPath() throws Exception;
	protected abstract void setJobMetadata(Job job) throws Exception;
	protected abstract String createUnixCommand()throws Exception;
	protected abstract void setParameters();
	
	protected void preLoading() throws Exception{}
	protected void postLoading() throws Exception{}
	
	protected boolean validateInputs(){return true;}
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
        			loadDataUI.setMessage("Error when launching command");
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
	  
	  Pattern pattern=Pattern.compile(storedProcedureLaunched, Pattern.DOTALL);
	  Matcher matcher=pattern.matcher(out);
	  if(matcher.matches()){
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			Statement stmt = con.createStatement();
			
			//remove rows for this study before adding new ones
			ResultSet rs=stmt.executeQuery(queryStoredProcedureStarted);
			int jobId;
			if(rs.next()){
				jobId=rs.getInt("max(JOB_ID)");
			}
			else{
				con.close();
				loadDataUI.setIsLoading(false);
				return;
			}
			
			out+="\nOracle job id:\n"+String.valueOf(jobId);
			con.close();
		}
		
		writeLog(out);
		
		channel.disconnect();
		session.disconnect();
	}
	
	protected boolean validateTopNode(){
		String[] splited=loadDataUI.getTopNode().split("\\\\", -1);
		if(splited[0].compareTo("")!=0){
			loadDataUI.setMessage("A study node has to begin by the character '\\'");
			loadDataUI.setIsLoading(false);
			return false;
		}
		return true;
	}
	protected void createAnalyzeTree(){
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			String[] splited=loadDataUI.getTopNode().split("\\\\", -1);
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select * from table_access where c_name='"+splited[1].replace("_"," ")+"'");
			if(!rs.next()){//have to add a top node
				stmt.executeQuery("insert into table_access("+
						"c_table_cd,"+
						"c_table_name,"+
						"c_protected_access,"+
						"c_hlevel,"+
						"c_fullname,"+
						"c_name,"+
						"c_synonym_cd,"+
						"c_visualattributes,"+
						"c_totalnum,"+
						"c_facttablecolumn,"+
						"c_dimtablename,"+
						"c_columnname,"+
						"c_columndatatype,"+
						"c_operator,"+
						"c_dimcode,"+
						"c_tooltip,"+
						"c_status_cd) values("+
						"'"+splited[1].replace("_"," ")+"',"+
						"'i2b2',"+
						"'N',"+
						"0,"+
						"'\\"+splited[1].replace("_"," ")+"\\',"+
						"'"+splited[1].replace("_"," ")+"',"+
						"'N',"+	
						"'CAP',"+
						"0,"+
						"'concept_cd',"+
						"'concept_dimension',"+
						"'concept_path',"+
						"'T',"+
						"'LIKE',"+
						"'\\"+splited[1].replace("_"," ")+"\\',"+
						"'\\"+splited[1].replace("_"," ")+"\\',"+
						"'A')"
					);
				
				//get folder id for program
				int id=RetrieveFm.getProgramId();
				String uid="FOL:"+String.valueOf(id);
				
				stmt.executeQuery("insert into i2b2 values(0, '\\"+splited[1].replace("_"," ")+"\\', '"+splited[1].replace("_"," ")+"','N','CAP',0,null, null, 'CONCEPT_CD','CONCEPT_DIMENSION','CONCEPT_PATH', 'T', 'LIKE','\\"+splited[1].replace("_"," ")+"\\', null, '\\"+splited[1].replace("_"," ")+"\\', sysdate, null, null, '"+uid+"', null, null, '@', null, null, null)");
			}
			
			
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
			loadDataUI.setMessage("SQL exception: "+e.getLocalizedMessage());
			loadDataUI.setIsLoading(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			loadDataUI.setMessage("Class Not Found exception");
			loadDataUI.setIsLoading(false);
			return;
		}
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
				loadDataUI.setMessage("The file directory does not exist in this server");
				loadDataUI.setIsLoading(false);
				return;
			}
		}else{
			loadDataUI.setMessage("No file directory is indicated");
			loadDataUI.setIsLoading(false);
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
			if(((HDDData)dataType).isIncremental()){
				String subFolder=((HDDData)dataType).getPath().getName();
				try{
					c.mkdir(subFolder);
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd(subFolder);
			}
		}catch(SftpException e){
			loadDataUI.setMessage("Directory can not be created");
			loadDataUI.setIsLoading(false);
    	   	return;
		}

		try{
			for(File f: dataType.getFiles()){
				c.put(f.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
			}
			
		}catch(SftpException e){
			loadDataUI.setMessage("Error when transferring files");
    	   loadDataUI.setIsLoading(false);
    	   return;
		}
	}
	
	protected void writeLog(Job job) throws SQLException
	{
		Log4jBufferAppender appender = CentralLogStore.getAppender();
		String logText = appender.getBuffer(job.getLogChannelId(), false).toString();
		
		Pattern pattern=Pattern.compile(storedProcedureLaunched, Pattern.DOTALL);
		Matcher matcher=pattern.matcher(logText);
		if(matcher.matches()){
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			Statement stmt = con.createStatement();
			
			//remove rows for this study before adding new ones
			ResultSet rs=stmt.executeQuery(queryStoredProcedureStarted);
			int jobId;
			if(rs.next()){
				jobId=rs.getInt("max(JOB_ID)");
				logText+="\nOracle job id:\n"+String.valueOf(jobId);
				con.close();
			}
			else{
				logText+="no job id found";
				con.close();
			}
		}
		writeLog(logText);
		
		CentralLogStore.discardLines(job.getLogChannelId(), false);
	}
	public void writeLog(String text)
	{
		File log=new File(dataType.getPath()+File.separator+"kettle.log");
		try
		{
			FileWriter fw = new FileWriter(log);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(text);		
			output.close();
			loadDataUI.setLogFile(log);
		}
		catch(IOException ioe){
			loadDataUI.setMessage("File error: "+ioe.getLocalizedMessage());
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

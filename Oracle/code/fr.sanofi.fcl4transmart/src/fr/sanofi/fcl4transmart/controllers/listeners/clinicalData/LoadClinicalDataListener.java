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
package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.ClinicalLoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

import org.eclipse.e4.core.di.extensions.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 *This class controls the clinical data loading
 */	
public class LoadClinicalDataListener implements Listener{
	private DataTypeItf dataType;
	private ClinicalLoadDataUI loadDataUI;
	private ChannelSftp c;
	Session  session;
	private boolean success;
	@SuppressWarnings("restriction")
	@Inject @Preference(nodePath="fr.sanofi.fcl4transmart") IEclipsePreferences preferences;

	public LoadClinicalDataListener(ClinicalLoadDataUI loadDataUI, DataTypeItf dataType){
		this.dataType=dataType;
		this.loadDataUI=loadDataUI;
		this.success=true;
	}
	@Override
	public void handleEvent(Event event) {
		loadDataUI.openLoadingShell();
		new Thread(){
			public void run() {
				String[] splited=loadDataUI.getTopNode().split("\\\\");
				if(splited.length!=3){
					loadDataUI.setMessage("The study top node is not valid");
					loadDataUI.setIsLoading(false);
					success=false;
					return;
				}
				//check if top node exists, add it if not
				createTopNode();
	
				if(!loadDataUI.getEtlServer()){//do not use ETL server
					loadFromLocal();
				}else{
					loadFromServer();
				}
			}
		}.start();
		this.loadDataUI.waitForThread();
		if(success) this.loadDataUI.displayMessage("Loading process is over.\n Please check monitoring step.");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
	}
	/**
	 *Write the given string in the log file
	 */	
	public void writeLog(String text)
	{
		File log=new File(this.dataType.getPath()+File.separator+"kettle.log");
		try
		{
			FileWriter fw = new FileWriter(log);
			BufferedWriter output = new BufferedWriter(fw);
			output.write(text);		
			output.close();
			((ClinicalData)this.dataType).setLogFile(log);
		}
		catch(IOException ioe){
			this.loadDataUI.displayMessage("File error: "+ioe.getLocalizedMessage());
			ioe.printStackTrace();
			success=false;
		}
	}
	public static void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	
	public void loadFromLocal(){
		try{ 
			String[] splited=loadDataUI.getTopNode().split("\\\\");
			//check there are no ":" in program or study name (not supported in Windows folder names)
			String programFolder=splited[1];
			String studyFolder=splited[2];
			if(splited[1].contains(":")){
				programFolder=splited[1].replace(":", "-");
			}
			if(splited[2].contains(":")){
				studyFolder=splited[2].replace(":", "-");
			}
			
			//create directories for ETL
			String dataLocation=dataType.getPath().getAbsolutePath();
			File etlDir=new File(dataLocation+File.separator+"etl_dir");
			FileUtils.forceMkdir(etlDir);
			
			FileUtils.forceMkdir(new File(etlDir.getAbsolutePath()+File.separator+programFolder));
			FileUtils.forceMkdir(new File(etlDir.getAbsolutePath()+File.separator+programFolder+File.separator+studyFolder));
			File clinicalDataDir=new File(etlDir.getAbsolutePath()+File.separator+programFolder+File.separator+studyFolder+File.separator+"ClinicalData");
			FileUtils.forceMkdir(clinicalDataDir);
			FileUtils.forceMkdir(new File(etlDir.getAbsolutePath()+File.separator+"DisplayMapping"));
						
			//create Config.groovy
			File configFile=new File(etlDir+File.separator+"Config.groovy");
			BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
			out.write("db.hostname = '"+PreferencesHandler.getDbServer()+"'\n");
			out.write("db.port = "+PreferencesHandler.getDbPort()+"\n");
			out.write("db.sid = '"+PreferencesHandler.getDbName()+"'\n");
			out.write("db.username = '"+PreferencesHandler.getTm_czUser()+"'\n");
			out.write("db.password = '"+PreferencesHandler.getTm_czPwd()+"'\n");
			out.write("log.fileName = \""+(dataLocation+File.separator+"kettle.log").replace("\\", "\\\\")+"\"\n");
			out.write("db.jdbcConnectionString = \""+RetrieveData.getConnectionString()+"\"\n");
			out.write("db.jdbcDriver = '"+RetrieveData.getDriverString()+"'\n");
			out.write("dataDir = '"+etlDir.getAbsolutePath().replace("\\", "\\\\")+"'");
			out.close();
			//copy clinical data files
			for(File f: ((ClinicalData)dataType).getRawFiles()){
				Utils.copyFile(f, new File(clinicalDataDir+File.separator+f.getName()));
			}
			for(String k: ((ClinicalData)dataType).getMappingFiles().keySet()){
				Utils.copyFile(((ClinicalData)dataType).getMappingFiles().get(k), new File(clinicalDataDir+File.separator+((ClinicalData)dataType).getMappingFiles().get(k).getName()));
			}
			if(((ClinicalData)dataType).getWMF()!=null) Utils.copyFile(((ClinicalData)dataType).getWMF(), new File(clinicalDataDir+File.separator+((ClinicalData)dataType).getWMF().getName()));
			Utils.copyFile(((ClinicalData)dataType).getCMF(), new File(clinicalDataDir+File.separator+dataType.getStudy().toString()+"_Mapping_File.txt"));
			if(((ClinicalData)dataType).getDimFile()!=null) Utils.copyFile(((ClinicalData)dataType).getDimFile(), new File(etlDir.getAbsolutePath()+File.separator+"DisplayMapping"+File.separator+((ClinicalData)dataType).getDimFile().getName()));
			
			//launch ETL
			URL jarUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/tm_etl.jar");
			jarUrl = FileLocator.toFileURL(jarUrl);  
			String jarPath = jarUrl.getPath();
			jarPath = URLDecoder.decode(jarPath, "utf-8");
			jarPath = new File(jarPath).getPath();
			List<String> cmdList=new ArrayList<String>();
			cmdList.addAll(Arrays.asList( "java", "-jar", jarPath, 
					"-c", etlDir+File.separator+"Config.groovy",
					"-study", dataType.getStudy().toString(),
					"-node", splited[1]+"\\\\"+splited[2]));

			if(((ClinicalData)dataType).isIncremental()) cmdList.add("-incremental"); 
			if(loadDataUI.getSecurity()) cmdList.add("-secure-study");
			
	        Process p = Runtime.getRuntime().exec(cmdList.toArray(new String[cmdList.size()]));
	        BufferedReader stdInput = new BufferedReader(new 
	                InputStreamReader(p.getInputStream()));
	        BufferedReader stdError = new BufferedReader(new 
	                InputStreamReader(p.getErrorStream()));
	        // read the output from the command
	        String s;
	        while ((s = stdInput.readLine()) != null) {
	            System.out.println(s);
	        }
	        // read any errors from the attempted command
	        while ((s = stdError.readLine()) != null) {
	            System.out.println(s);
	        }    
	        p.waitFor();
			
			//delete etl directory
			deleteDir(etlDir);
			
			((ClinicalData)dataType).setLogFile(new File(dataLocation+File.separator+"kettle.log"));
		}catch (Exception e1) {
			e1.printStackTrace();
			loadDataUI.setMessage("Kettle exception: "+e1.getLocalizedMessage());
			loadDataUI.setIsLoading(false);
			success=false;
			return;
		}
		loadDataUI.setIsLoading(false);
	}
	public void loadFromServer(){
		try{
			String dataLocation=dataType.getPath().getAbsolutePath();
			//create directories for ETL
			
			JSch jsch=new JSch();
			session=jsch.getSession(etlPreferences.getUser(), etlPreferences.getHost(), Integer.valueOf(etlPreferences.getPort()));
			session.setPassword(etlPreferences.getPass());
		 
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
			session.setConfig(config);
			
			session.connect();
			Channel channel=session.openChannel("sftp");
			c=(ChannelSftp)channel;
			c.connect();
							
			//try to go to the right directory for file transfer
			String dir=etlPreferences.getFilesDirectory();
			String[] splited=loadDataUI.getTopNode().split("\\\\");
			if(dir.compareTo("")!=0){
				try{
					c.cd(dir);
				}catch(SftpException e){
					loadDataUI.setMessage("The file directory does not exist in this server");
					loadDataUI.setIsLoading(false);
					success=false;
					return;
				}
			}else{
				loadDataUI.setMessage("No file directory is indicated");
				loadDataUI.setIsLoading(false);
				success=false;
				return;
			}
			String etlDir="";
			try{
				try{
					c.mkdir(dataType.getStudy().toString());
				}catch(Exception exist){
					//normal if directory already exists
				}
				c.cd(dataType.getStudy().toString());
				try{
					c.mkdir("clinical");
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd("clinical");
				if(((ClinicalData)dataType).isIncremental()){
					String subFolder=((ClinicalData)dataType).getPath().getName();
					try{
						c.mkdir(subFolder);
					}catch(Exception exist2){
						//normal if directory already exists
					}
					c.cd(subFolder);
				}
				
				try{
					c.mkdir("etl_dir");
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd("etl_dir");
				etlDir=c.pwd();
				try{
					c.mkdir("DisplayMapping");
				}catch(Exception exist2){
					//normal if directory already exists
				}
				if (((ClinicalData)dataType).getDimFile()!=null) c.put(((ClinicalData)dataType).getDimFile().getAbsolutePath(), "DisplayMapping", null, ChannelSftp.OVERWRITE);
				File configFile=new File(dataLocation+File.separator+"Config.groovy");
				BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
				out.write("db.hostname = '"+PreferencesHandler.getDbServer()+"'\n");
				out.write("db.port = "+PreferencesHandler.getDbPort()+"\n");
				out.write("db.sid = '"+PreferencesHandler.getDbName()+"'\n");
				out.write("db.username = '"+PreferencesHandler.getTm_czUser()+"'\n");
				out.write("db.password = '"+PreferencesHandler.getTm_czPwd()+"'\n");
				out.write("log.fileName = \""+(c.pwd()+"/"+splited[1]+"/"+splited[2]+"/"+"kettle.log").replace("\\", "\\\\")+"\"\n");
				out.write("db.jdbcConnectionString = \""+RetrieveData.getConnectionString()+"\"\n");
				out.write("db.jdbcDriver = '"+RetrieveData.getDriverString()+"'\n");
				out.write("dataDir = '"+etlDir.replace("\\", "\\\\")+"'");
				out.close();
				try{
					c.put(configFile.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
				}catch(SftpException e){
					loadDataUI.setMessage("Error when transferring files");
					loadDataUI.setIsLoading(false);
					return;
				}
				configFile.delete();
				try{
					c.mkdir(splited[1]);
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd(splited[1]);
				try{
					c.mkdir(splited[2]);
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd(splited[2]);
				try{
					c.mkdir("ClinicalData");
				}catch(Exception exist2){
					//normal if directory already exists
				}
				c.cd("ClinicalData");
			}catch(SftpException e){
				loadDataUI.setMessage("Directory can not be created");
				loadDataUI.setIsLoading(false);
				success=false;
        	   	return;
			}
			
			try{
				for(File f: ((ClinicalData)dataType).getRawFiles()){
					c.put(f.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
				}
				for(File f: ((ClinicalData)dataType).getMappingFiles().values()){
					c.put(f.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
				}
				if(((ClinicalData)dataType).getWMF()!=null) c.put(((ClinicalData)dataType).getWMF().getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
				c.put(((ClinicalData)dataType).getCMF().getAbsolutePath(), "./"+dataType.getStudy().toString()+"_Mapping_File.txt", null, ChannelSftp.OVERWRITE);
			}catch(SftpException e){
				loadDataUI.setMessage("Error when transferring files");
				loadDataUI.setIsLoading(false);
				success=false;
				return;
			}
						
			String command="java -jar "+etlPreferences.getJobsDirectory()+"/tm_etl.jar ";
			command+="-c "+ etlDir+"/Config.groovy ";
			command+="-study '"+dataType.getStudy().toString()+"'";
			command+=" -node '"+splited[1]+"\\\\"+splited[2]+"'";
			if(loadDataUI.getSecurity()) command+=" -secure-study";
			if(((ClinicalData)dataType).isIncremental()) command+=" -incremental";
			
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
		    			loadDataUI.setIsLoading(false);
		    			success=false;
		    			return;
	        		}
	        	}
		        try{Thread.sleep(1000);}catch(Exception ee){}
		      }
			System.out.println(out);
			
			c.get(etlDir+"/"+splited[1]+"/"+splited[2]+"/"+"kettle.log", dataType.getPath().getAbsolutePath());
			((ClinicalData)dataType).setLogFile(new File(dataType.getPath().getAbsolutePath()+File.separator+"kettle.log"));
			channel.disconnect();
			session.disconnect();
			
	    }catch(Exception e){
	    	e.printStackTrace();
			loadDataUI.setMessage("Error when loading data");
			loadDataUI.setIsLoading(false);
			success=false;
			return;
	    }
		loadDataUI.setIsLoading(false);
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
			 Pattern pattern=Pattern.compile(".*tm_etl.*", Pattern.DOTALL);
	     Matcher matcher=pattern.matcher(out2);
	     if(!matcher.matches()){
	    	 return false;
	     }
	     return true;
	}
	public void createTopNode(){
		try{
			String[] splited=loadDataUI.getTopNode().split("\\\\", -1);
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
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
	private void deleteDir(File dir) throws IOException {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles())
				deleteDir(f);
		}
		if (!dir.delete()) throw new FileNotFoundException("Failed to delete file: " + dir);
	}
}

package fr.sanofi.fcl4transmart.controllers;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class HDDMonitoringController{
	private DataTypeItf dataType;
	private File logFile;
	private int jobId;
	public HDDMonitoringController(DataTypeItf dataType){
		this.dataType=dataType;
	}

	public boolean checkLogFileExists() {
		File logFile=((HDDData)this.dataType).getLogFile();
		if(logFile.exists()){
			this.logFile=logFile;
			this.jobId=getJobId();
			return true;
		}
		return false;
	}

	public boolean kettleSucceed() {
		if(this.jobId!=-1) return true;
		return false;
	}

	public String proceduresError() {
		String procedureErrors="";
		try{
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			Statement stmt = con.createStatement();

			if(jobId==-1) return "";
			ResultSet rs=stmt.executeQuery("(select STEP_DESC from CZ_JOB_AUDIT where STEP_STATUS='ERROR' and JOB_ID="+String.valueOf(jobId)+") UNION "+
					"(select STEP_DESC from CZ_JOB_AUDIT where STEP_STATUS='FAIL' and JOB_ID="+String.valueOf(jobId)+")");
			if(rs.next()){
				procedureErrors=rs.getString("STEP_DESC");
			}
	
			rs=stmt.executeQuery("select ERROR_MESSAGE from CZ_JOB_ERROR where JOB_ID="+String.valueOf(jobId));
			if(rs.next()){
				procedureErrors=rs.getString("ERROR_MESSAGE");
			}
			if(procedureErrors.compareTo("User-Defined Exception")==0){
				rs=stmt.executeQuery("select STEP_DESC from CZ_JOB_AUDIT where JOB_ID="+String.valueOf(jobId)+" and SEQ_ID in(select max(SEQ_ID)-1 from CZ_JOB_AUDIT where JOB_ID="+String.valueOf(jobId)+")");
				if(rs.next()){
					procedureErrors=rs.getString("STEP_DESC");
				}
			}
			con.close();
	    	
			}catch(SQLException sqle){
				sqle.printStackTrace();
			}
		return procedureErrors;
	}
	private int getJobId(){
		int jobId=-1;
		if(this.logFile!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(this.logFile));
				String line;
				while ((line=br.readLine())!=null){
					if(line.compareTo("Oracle job id:")==0){
						try{
							jobId=Integer.parseInt(br.readLine());
						}
						catch(Exception e){
							br.close();
							return -1;
						}
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return -1;
			}
		}
		return jobId;
	}
}

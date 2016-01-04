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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
/**
 *This class controls the gene expression data quality control
 */	
public class GeneQCController {
	private DataTypeItf dataType;
	public GeneQCController(DataTypeItf dataType){
		this.dataType=dataType;
	}
	/**
	 *Returns a hash map with as key a probe and as value the intensity value, from the data files
	 */	
	public HashMap<String, Double> getFileValues(String probeId){
		HashMap<String, Double> filesValues=new HashMap<String, Double>();
		Vector<File> rawFiles=((GeneExpressionData)this.dataType).getRawFiles();
		for(File file: rawFiles){
                	filesValues.putAll(FileHandler.getIntensity(file, probeId));
		}
		return filesValues;
	}
	/**
	 *Returns a hash map with as key a probe and as value the intensity value, from the database
	 */
	public HashMap<String, Double> getDbValues(String probeId){
		HashMap<String, Double> dbValues=new HashMap<String, Double>();
		try{
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select ssm.sample_cd, smd.log_intensity from de_subject_microarray_data smd, de_subject_sample_mapping ssm where probeset_id in (select probeset_id from de_mrna_annotation where probe_id='"+probeId+"') and ssm.trial_name='"+this.dataType.getStudy().toString().toUpperCase()+"' and ssm.assay_id=smd.assay_id");
			while(rs.next()){
				dbValues.put(rs.getString(1), rs.getDouble(2));
			}
			con.close();
		}catch(SQLException sqle){
			sqle.printStackTrace();
			return null;
		}
		catch(ClassNotFoundException cnfe){
			cnfe.printStackTrace();
			return null;
		}
		return dbValues;
	}
	public HashMap<String, HashMap<String, Double>> getDbValuesAllProbes(){
		HashMap<String, HashMap<String, Double>> dbValues=new HashMap<String, HashMap<String, Double>>();
		try{
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select ssm.sample_cd, smd.log_intensity, probe_id from de_subject_microarray_data smd, de_subject_sample_mapping ssm, de_mrna_annotation ma where smd.probeset_id=ma.probeset_id and ssm.trial_name='"+this.dataType.getStudy().toString().toUpperCase()+"' and ssm.assay_id=smd.assay_id");
			while(rs.next()){
				String probe=rs.getString(3);
				if(dbValues.get(probe)==null) dbValues.put(probe, new HashMap<String, Double>()); 	
				dbValues.get(probe).put(rs.getString(1), rs.getDouble(2));
			}
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return dbValues;
	}
	public boolean writeLog(){
		FileWriter fw;
		try {
			HashMap<String, HashMap<String, Double>> fileValues=FileHandler.getIntensitiesAllProbes(((GeneExpressionData)this.dataType).getRawFiles());
			HashMap<String, HashMap<String, Double>> dbValues=this.getDbValuesAllProbes();
			
			String logPath=this.dataType.getPath().getAbsolutePath()+File.separator+"QClog.txt";
			fw = new FileWriter(logPath);
			BufferedWriter out = new BufferedWriter(fw);
			int cnt=0;
			int n=0;
			for(String probe: fileValues.keySet()){
				for(String sample: fileValues.get(probe).keySet()){
					if(dbValues.get(probe)!=null && dbValues.get(probe).get(sample)!=null){
						if((dbValues.get(probe).get(sample)-fileValues.get(probe).get(sample))<=0.001 && (dbValues.get(probe).get(sample)-fileValues.get(probe).get(sample))>=-0.001){
							n++;
						}
						else{
							if(cnt==0){
								out.write("There are differences between the files and database:\n");
								out.write("Probe\tSample\tFiles intensity\tDatabase intensity\n");
							}
							cnt++;
							out.write(probe+"\t"+sample+"\t"+fileValues.get(probe).get(sample)+"\t"+dbValues.get(probe).get(sample)+"\n");
						}	
					}
					else{
						//no value
						if(cnt==0){
							out.write("There are differences between the files and database:\n");
							out.write("Probe\tSample\tFiles intensity\tDatabase intensity\n");
						}
						cnt++;
						out.write(probe+"\t"+sample+"\t"+fileValues.get(probe).get(sample)+"\tNo value\n");
					}
				}
			}
			out.write("Number of identical values: "+n+"\n");
			out.write("Number of different values: "+cnt);
			out.close();
			((GeneExpressionData)this.dataType).setQClog(new File(logPath));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean getIfRawData(){
		File logFile=((GeneExpressionData)this.dataType).getLogFile();
		if(logFile!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(logFile));
				String line;
				Pattern pattern=Pattern.compile(".*data_type = R");
				while ((line=br.readLine())!=null){
					Matcher matcher=pattern.matcher(line);
					if(matcher.matches()){
						br.close();
						return true;
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
}

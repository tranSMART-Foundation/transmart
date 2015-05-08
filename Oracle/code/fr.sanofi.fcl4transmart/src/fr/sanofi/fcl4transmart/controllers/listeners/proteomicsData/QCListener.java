package fr.sanofi.fcl4transmart.controllers.listeners.proteomicsData;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.ProteomicsData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;

public class QCListener {
	private DataTypeItf dataType;
	public QCListener(DataTypeItf dataType){
		this.dataType=dataType;
	}
	public HashMap<String, Double> getFileValues(String probeId){
		HashMap<String, Double> filesValues=new HashMap<String, Double>();
		Vector<File> rawFiles=((ProteomicsData)this.dataType).getRawFiles();
		for(File file: rawFiles){
			filesValues.putAll(FileHandler.getProteomicsValue(file, ((ProteomicsData)this.dataType).getColumnMappingFile(), probeId));
		}
		return filesValues;
	}
	public HashMap<String, Double> getDbValues(String probeId){
		HashMap<String, Double> dbValues=new HashMap<String, Double>();
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select dm.sample_cd, srd.intensity from deapp.de_subject_protein_data srd INNER JOIN de_subject_sample_mapping dm ON dm.assay_id= srd.assay_id where dm.trial_name='"+this.dataType.getStudy().toString().toUpperCase()+"' and srd.protein_annotation_id in (select p.id from de_protein_annotation p where peptide='"+probeId+"')");
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
	public HashMap<String, HashMap<String, Double>> getDbValuesAllTranscripts(){
		HashMap<String, HashMap<String, Double>> dbValues=new HashMap<String, HashMap<String, Double>>();
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
			Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select dm.sample_cd, srd.intensity, p.peptide from deapp.de_subject_protein_data srd INNER JOIN deapp.de_subject_sample_mapping dm ON dm.assay_id= srd.assay_id INNER JOIN deapp.de_protein_annotation p ON p.id=srd.protein_annotation_id where dm.trial_name='"+this.dataType.getStudy().toString().toUpperCase()+"'");
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
			HashMap<String, HashMap<String, Double>> fileValues=FileHandler.getProteomicsValuesProbes(((ProteomicsData)this.dataType).getRawFiles(), ((ProteomicsData)this.dataType).getColumnMappingFile());
			HashMap<String, HashMap<String, Double>> dbValues=this.getDbValuesAllTranscripts();
			
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
								out.write("Probe\tSample\tFiles value\tDatabase value\n");
							}
							cnt++;
							out.write(probe+"\t"+sample+"\t"+fileValues.get(probe).get(sample)+"\t"+dbValues.get(probe).get(sample)+"\n");
						}	
					}
					else{
						//if raw value is 0, it's normal to have no value in DB
						if(fileValues.get(probe).get(sample)==0){
							n++;
						}else{
							//no value
							if(cnt==0){
								out.write("There are differences between the files and database:\n");
								out.write("Probe\tSample\tFiles value\tDatabase value\n");
							}
							cnt++;
							out.write(probe+"\t"+sample+"\t"+fileValues.get(probe).get(sample)+"\tNo value\n");
						}
					}
				}
			}
			out.write("Number of identical values: "+n+"\n");
			out.write("Number of different values: "+cnt);
			out.close();
			((ProteomicsData)this.dataType).setQClog(new File(logPath));
			UsedFilesPart.sendFilesChanged(this.dataType);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

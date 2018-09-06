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
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
/**
 * This class controls the clinical data quality control step
 */
public class ClinicalQCController {
	private DataTypeItf dataType;
	public ClinicalQCController(DataTypeItf dataType){
		this.dataType=dataType;
	}
	/**
	 * Returns a Hash map containing properties and values for a given subject from data files
	 */
	public HashMap<String, Vector<String>> getFileValues(String subject){
		HashMap<String, Vector<String>> filesValues=new HashMap<String, Vector<String>>();
		File cmf=((ClinicalData)this.dataType).getCMF();
		for(File rawFile: ((ClinicalData)this.dataType).getRawFiles()){
			HashMap<String, Vector<String>> values=FileHandler.getValueForSubjectForQC(cmf, rawFile, subject, ((ClinicalData)this.dataType).getWMF());
			for(String k: values.keySet()){
				Vector<String> v=new Vector<String>();
				for(String s: values.get(k)){
					v.add(this.replaceValue(s));
				}
				filesValues.put(this.replaceLabel(k), v);
			}
		}
		return filesValues;
	}
	/**
	 * Returns a Hash map containing properties and values for a given subject from database
	 */
	public HashMap<String, Vector<String>> getDbValues(String subject){
		HashMap<String, Vector<String>> dbValues=new HashMap<String, Vector<String>>();
		try{
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			String topNode=((ClinicalData)this.dataType).getStudy().getTopNode();
			
			ResultSet rs=stmt.executeQuery("select CONCEPT_DIMENSION.CONCEPT_PATH, NVAL_NUM from CONCEPT_DIMENSION, OBSERVATION_FACT where OBSERVATION_FACT.VALTYPE_CD='N' and OBSERVATION_FACT.patient_num in (select patient_num from I2B2DEMODATA.PATIENT_DIMENSION where REGEXP_LIKE(sourcesystem_cd,'"+this.dataType.getStudy().toString().toUpperCase()+":(.*:)*"+subject+"$')) and OBSERVATION_FACT.CONCEPT_CD=CONCEPT_DIMENSION.CONCEPT_CD");

                        while(rs.next()){
                        	String path=rs.getString("CONCEPT_PATH");
		    		String value=String.valueOf(rs.getDouble("NVAL_NUM"));
    			if(path.split(Pattern.quote(topNode),2).length>1){
    				path=path.split(Pattern.quote(topNode),2)[1];
	    			if(path.lastIndexOf("\\")==path.length()-1) path=path.substring(0, path.length()-1); //remove the last back slash character
	    			Vector<String> v=new Vector<String>();
					if(dbValues.get(path)!=null) v=dbValues.get(path);
					else dbValues.put(path, v);
					v.add(value);
    			}
    		}
    		
    		rs=stmt.executeQuery("select CONCEPT_DIMENSION.CONCEPT_PATH, TVAL_CHAR from CONCEPT_DIMENSION, OBSERVATION_FACT where OBSERVATION_FACT.VALTYPE_CD='T' and OBSERVATION_FACT.patient_num in (select patient_num from I2B2DEMODATA.PATIENT_DIMENSION where REGEXP_LIKE(sourcesystem_cd, '"+this.dataType.getStudy().toString().toUpperCase()+":(.*:)*"+subject+"$')) and OBSERVATION_FACT.CONCEPT_CD=CONCEPT_DIMENSION.CONCEPT_CD");
    		while(rs.next()){
    			String path=rs.getString("CONCEPT_PATH");
    			String value=rs.getString("TVAL_CHAR");
    			if(value.compareTo("EXP:PUBLIC")!=0 && value.compareTo("E")!=0){ //remove security lines and gene expression lines
	    			if(path.split(Pattern.quote(topNode),2).length>1) path=path.split(Pattern.quote(topNode),2)[1];//remove the part of the path containing the top node
	    			if(path.lastIndexOf("\\")==path.length()-1) path=path.substring(0, path.length()-1); //remove the last back slash character
	    			if(path.substring(path.lastIndexOf("\\")+1, path.length()).compareTo(value)==0){//remove the last part (containing the value)
	    				path=path.substring(0, path.lastIndexOf("\\"));
	    			}else{//if there is a visit_name, there is the value then the visit name
	    				String visit=path.substring(path.lastIndexOf("\\"), path.length());
	    				path=path.substring(0, path.lastIndexOf("\\"));//remove visit name
	    				path=path.substring(0, path.lastIndexOf("\\"));//remove value
	    				path=path+visit;//read visit name
	    			}
	    			Vector<String> v=new Vector<String>();
					if(dbValues.get(path)!=null) v=dbValues.get(path);
					else dbValues.put(path, v);
					v.add(value);
    			}
    		}
			for(String k: dbValues.keySet()){
				System.out.println(k+" --> "+dbValues.get(k));
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
	/**
	 * Write in a log file the differences between files and database values, for all subjects
	 */
	public boolean writeLog(){
		FileWriter fw;
		try {
			HashMap<String, HashMap<String, Vector<String>>> fileValues=FileHandler.getValueForQC(((ClinicalData)this.dataType).getCMF(), ((ClinicalData)this.dataType).getRawFiles(), ((ClinicalData)this.dataType).getWMF());
			HashMap<String, HashMap<String, Vector<String>>> dbValues=this.getDbValuesAllSubjects(fileValues.keySet());
			
			String logPath=this.dataType.getPath().getAbsolutePath()+File.separator+"QClog.txt";
			fw = new FileWriter(logPath);
			BufferedWriter out = new BufferedWriter(fw);
	
			int cnt=0;
			int n=0;
			for(String subject: fileValues.keySet()){
				for(String label: fileValues.get(subject).keySet()){
					String correctedLabel=this.replaceLabel(label);
					for(int i=0; i<fileValues.get(subject).get(label).size(); i++){
						fileValues.get(subject).get(label).set(i, this.replaceValue(fileValues.get(subject).get(label).get(i)));
					}
					Vector<String> file=this.sort(fileValues.get(subject).get(label));       
					Vector<String> db=new Vector<String>();
					if(dbValues.get(subject)!=null && dbValues.get(subject).get(correctedLabel)!=null){
						db=this.sort(dbValues.get(subject).get(correctedLabel));
					}
					for(int i=0; i<file.size(); i++){
						String fileValue=file.get(i);
						if(!(fileValue.compareTo("")==0 || fileValue.compareTo(".")==0)){
							if(db==null || i>=db.size()){
								if(cnt==0){
									out.write("There are differences between the files and database:\n");
									out.write("Subject\tProperty\tFiles value\tDatabase value\n");
								}
							cnt++;
							out.write(subject+"\t"+label+"\t"+file.get(i)+"\tNo value\n");
							}else{
								try{
									if(Double.valueOf(fileValue)-Double.valueOf(db.get(i))<0.001 && Double.valueOf(fileValue)-Double.valueOf(db.get(i))>-0.001){
										//OK
										n++;
									}
									else{
										if(cnt==0){
											out.write("There are differences between the files and database:\n");
											out.write("Subject\tProperty\tFiles value\tDatabase value\n");
										}
										cnt++;
										out.write(subject+"\t"+label+"\t"+fileValue+"\t"+db.get(i)+"\n");
									}
								}
								catch(Exception e){
									if(fileValue.compareTo(db.get(i))==0){
										n++;
									}
									else{
										if(cnt==0){
											out.write("There are differences between the files and database:\n");
											out.write("Subject\tProperty\tFiles value\tDatabase value\n");
										}
										cnt++;
										out.write(subject+"\t"+label+"\t"+fileValue+"\t"+db.get(i)+"\n");
									}
								}
							}
						}
					}
				}
			}
			out.write("Number of identical values: "+n+"\n");
			out.write("Number of different values: "+cnt);
			out.close();
			((ClinicalData)this.dataType).setQClog(new File(logPath));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a Hash map containing for each subject a hashmap with properties and values from database
	 */
	public HashMap<String, HashMap<String, Vector<String>>> getDbValuesAllSubjects(Set<String> subjects){
		HashMap<String, HashMap<String, Vector<String>>> dbValues=new HashMap<String, HashMap<String, Vector<String>>>();
		try{
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			String topNode=((ClinicalData)this.dataType).getStudy().getTopNode();
			ResultSet rs=stmt.executeQuery("select CONCEPT_DIMENSION.CONCEPT_PATH, NVAL_NUM, patient_dimension.sourcesystem_cd from CONCEPT_DIMENSION, OBSERVATION_FACT, patient_dimension where OBSERVATION_FACT.VALTYPE_CD='N' and OBSERVATION_FACT.patient_num=patient_dimension.patient_num and OBSERVATION_FACT.CONCEPT_CD=CONCEPT_DIMENSION.CONCEPT_CD and concept_dimension.sourcesystem_cd='"+this.dataType.getStudy().toString().toUpperCase()+"'");	
			while(rs.next()){
    			String path=rs.getString(1);
    			String value=String.valueOf(rs.getDouble(2));
    			
    			if(path.split(Pattern.quote(topNode),2).length>1){
	    			path=path.split(Pattern.quote(topNode),2)[1];//remove the part of the path containing the top node
	    			if(path.lastIndexOf("\\")==path.length()-1) path=path.substring(0, path.length()-1); //remove the last back slash character
	    			String source=rs.getString(3);
	    			String subject=source.split(":", -1)[source.split(":", -1).length-1];
	    			if(dbValues.get(subject)==null) dbValues.put(subject, new HashMap<String, Vector<String>>());
					if(dbValues.get(subject).get(path)==null) dbValues.get(subject).put(path, new Vector<String>());
					dbValues.get(subject).get(path).add(value);
    			}
    		}
			
			rs=stmt.executeQuery("select CONCEPT_DIMENSION.CONCEPT_PATH, TVAL_CHAR, patient_dimension.sourcesystem_cd from CONCEPT_DIMENSION, OBSERVATION_FACT, patient_dimension  where OBSERVATION_FACT.VALTYPE_CD='T' and OBSERVATION_FACT.patient_num=patient_dimension.patient_num and OBSERVATION_FACT.CONCEPT_CD=CONCEPT_DIMENSION.CONCEPT_CD and concept_dimension.sourcesystem_cd='"+this.dataType.getStudy().toString().toUpperCase()+"'");
			while(rs.next()){
    			String path=rs.getString(1);
    			String value=rs.getString(2);
    			if(value.compareTo("EXP:PUBLIC")!=0 && value.compareTo("E")!=0){
	    			if(path.split(Pattern.quote(topNode),2).length>1) path=path.split(Pattern.quote(topNode),2)[1];//remove the part of the path containing the top node
	    			if(path.lastIndexOf("\\")==path.length()-1) path=path.substring(0, path.length()-1); //remove the last back slash character
	    			if(path.substring(path.lastIndexOf("\\")+1, path.length()).compareTo(value)==0){//remove the last part (containing the value)
	    				path=path.substring(0, path.lastIndexOf("\\"));
	    			}else{//if there is a visit_name, there is the value then the visit name
	    				String visit=path.substring(path.lastIndexOf("\\"), path.length());
	    				path=path.substring(0, path.lastIndexOf("\\"));//remove visit name
	    				path=path.substring(0, path.lastIndexOf("\\"));//remove value
	    				path=path+visit;//read visit name
	    			}
	    			String source=rs.getString(3);
	    			String subject=source.split(":", -1)[source.split(":", -1).length-1];
	    			if(dbValues.get(subject)==null) dbValues.put(subject, new HashMap<String, Vector<String>>());
					if(dbValues.get(subject).get(path)==null) dbValues.get(subject).put(path, new Vector<String>());
					dbValues.get(subject).get(path).add(value);
    			}
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
	public String replaceValue(String value){
		Pattern p1=Pattern.compile(".*\\(( )*\\).*");
		Pattern p2=Pattern.compile(".*\\(.*");
		Pattern p3=Pattern.compile(".*\\).*");
		Matcher m1=p1.matcher(value);
		Matcher m2=p2.matcher(value);
		Matcher m3=p3.matcher(value);
		if(m1.matches() || (m2.matches() && !m3.matches())){
			value=value.replaceAll("(", "");
		}
		if(m1.matches() || m3.matches() && ! m2.matches()){
			value=value.replaceAll(")", "");
		}
		value=value.replaceAll("\\|$", "").replaceAll("^\\|", "");
		value=value.replaceAll("\\|", "-");
		value=value.replaceAll("%", "Pct");
		value=value.replaceAll("&", " and ");
		value=value.trim();
		value=value.replaceAll("  ", " ");
		value=value.replaceAll(" ,", ",");
		value=value.replaceAll("\\+", " and");
		value=value.replaceAll("\\\"", "");
		
		return value;
	}
	public String replaceLabel(String label){
		if(label!=null){
                	label=label.replaceAll("\\(%", "\\( Pct");
			label=label.replaceAll("%", "Pct");
			label=label.replaceAll("&", " and ");
			label=label.replaceAll("\\|", ",");
			label=label.trim();
			label=label.replaceAll("  ", " ");
			label=label.replaceAll(" ,", ",");
			label=label.replaceAll("_", " ");
                }
                
		return label;
	}
	
	public Vector<String> sort(Vector<String> sort) {
        Vector<String> v = new Vector<String>();
        for(int count = 0; count < sort.size(); count++) {
            String s = sort.elementAt(count).toString();
            int i = 0;
            for (i = 0; i < v.size(); i++) {
                int c = s.compareTo((String) v.elementAt(i));
                if (c < 0) {
                    v.insertElementAt(s, i);
                    break;
                } else if (c == 0) {
                    break;
                }
            }
            if (i >= v.size()) {
                v.addElement(s);
            }
        }

        return v;
    }
}

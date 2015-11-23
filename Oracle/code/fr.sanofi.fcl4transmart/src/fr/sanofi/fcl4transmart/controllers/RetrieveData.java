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
package fr.sanofi.fcl4transmart.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
/**
 *This class allows data retrieving from database
 */
public class RetrieveData {
	public static String getConnectionString(){
		return "jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
	}
	public static String getDriverString(){
		return "oracle.jdbc.driver.OracleDriver";
	}
	public static boolean isPostgres(){
		return false;   // not Postgres
	}
	public static boolean isOracle(){
		return true;    // is Oracle
	}
	/**
	 *Returns a vector with organism names
	 */
	public static Vector<String> getTaxononomy(){
	    Vector<String> taxons=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT distinct taxon_name from bio_taxonomy");

		    while(rs.next()){
		    	taxons.add(rs.getString("taxon_name"));
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return taxons;
		}
		catch(ClassNotFoundException cnfe){
			return taxons;
		}
		return taxons;
	}
	/**
	 *Retrieves study title from its identifier
	 */
	public static String retrieveTitle(String study){
		String title="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT title from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	title=rs.getString("title");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(title!=null){
				return title;
		}
		else{
			return "";
		}
	}
	
	/**
	 *Retrieves study description from its identifier
	 */
	public static String retrieveDescription(String study){
		String description="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT description from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	description=rs.getString("description");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(description!=null){
			return description;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study design from its identifier
	 */
	public static String retrieveDesign(String study){
		String design="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT design from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	design=rs.getString("design");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(design!=null){
			return design;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study owner from its identifier
	 */
	public static String retrieveOwner(String study){
		String owner="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT study_owner from bio_clinical_trial where trial_number='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	owner=rs.getString("study_owner");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(owner!=null){
			return owner;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study institution from its identifier
	 */
	public static String retrieveInstitution(String study){
		String institution="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT institution from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	institution=rs.getString("institution");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(institution!=null){
			return institution;
		}
		else{
			return "";
		}
	}	
	/**
	 *Retrieves study country from its identifier
	 */
	public static String retrieveCountry(String study){
		String country="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT country from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	country=rs.getString("country");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(country!=null){
			return country;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study access type from its identifier
	 */
	public static String retrieveAccessType(String study){
		String access_type="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT access_type from bio_experiment where accession='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	access_type=rs.getString("access_type");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(access_type!=null){
			return access_type;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study phase from its identifier
	 */
	public static String retrievePhase(String study){
		String phase="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT study_phase from bio_clinical_trial where trial_number='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	phase=rs.getString("study_phase");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(phase!=null){
			return phase;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study number from its identifier
	 */
	public static String retrieveNumber(String study){
		String number="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT number_of_patients from bio_clinical_trial where trial_number='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	number=rs.getString("number_of_patients");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(number!=null){
			return number;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study organism from its identifier
	 */
	public static String retrieveOrganism(String study){
		String organism="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT taxon_name from bio_taxonomy where bio_taxonomy_id in(select bio_taxonomy_id from bio_data_taxonomy where etl_source='"+study.toUpperCase()+"')");

		    if(rs.next()){
		    	organism=rs.getString("taxon_name");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(organism!=null){
			return organism;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study pubmed from its identifier
	 */
	public static String retrievePubmed(String study){
		String pubmed="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT location from biomart.bio_content where study_name='"+study.toUpperCase()+"' and repository_id in (select bio_content_repo_id from biomart.bio_content_repository where repository_type='PubMed')");

		    if(rs.next()){
		    	pubmed=rs.getString("location");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(pubmed!=null){
			return pubmed;
		}
		else{
			return "";
		}
	}
	/**
	 *Retrieves study top node from its identifier
	 */
	public static String retrieveTopNode(String study){
		String topNode="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT path from i2b2_tags where tag='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	topNode=rs.getString("path");
		    }
		    con.close();
				
		}catch(SQLException sqle){
			return "";
		}
		catch(ClassNotFoundException cnfe){
			return "";
		}
		if(topNode!=null){
			return topNode;
		}
		else{
			return "";
		}
	}
	/**
	 *Checks if a study is loaded
	 */
	public static boolean isLoaded(String study){
		boolean isLoaded=false;
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT * from i2b2_tags where tag='"+study.toUpperCase()+"'");

		    if(rs.next()){
		    	isLoaded=true;
		    }
		    else{
		    	rs = stmt.executeQuery("SELECT * from i2b2 where sourcesystem_cd='"+study.toUpperCase()+"'");
		    	if(rs.next()){
		    		isLoaded=true;
		    	}
		    }
		    
		    con.close();
				
		}catch(SQLException sqle){
			return false;
		}
		catch(ClassNotFoundException cnfe){
			return false;
		}
		return isLoaded;
	}
	/**
	 *Checks that the connection to biomart database is available with given parameters
	 */
	public static boolean testBiomartConnection(String dbServer, String dbName, String dbPort, String biomartUser, String biomartPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, biomartUser, biomartPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testFmappConnection(String dbServer, String dbName, String dbPort, String fmappUser, String fmappPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, fmappUser, fmappPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testMetadataConnection(String dbServer, String dbName, String dbPort, String metadataUser, String metadataPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, metadataUser, metadataPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testDemodataConnection(String dbServer, String dbName, String dbPort, String demodataUser, String demodataPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, demodataUser, demodataPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testDeappConnection(String dbServer, String dbName, String dbPort, String deappUser, String deappPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
		
			Connection con = DriverManager.getConnection(connection, deappUser, deappPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean testTm_lzConnection(String dbServer, String dbName, String dbPort, String tm_lzUser, String tm_lzPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, tm_lzUser, tm_lzPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testTm_czConnection(String dbServer, String dbName, String dbPort, String tm_czUser, String tm_czPwd){
		String connection=RetrieveData.getConnectionString();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(connection, tm_czUser, tm_czPwd);
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	/**
	 *Checks that the connection to biomart database is available with parameters from preferences
	 */	
	public static boolean testBiomartConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testFmappConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getFmappUser(), PreferencesHandler.getFmappPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testMetadataConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testDemodataConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testDeappConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
		
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean testTm_lzConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getTm_lzUser(), PreferencesHandler.getTm_lzPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	public static boolean testTm_czConnection(){
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e2) {
			return false;
		}
		return true;
	}
	/**
	 *Returns a vector containing studies identifiers for all loaded studies
	 */	
	public static Vector<String> getStudiesIds(){
		Vector<String> ids=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			String connection=RetrieveData.getConnectionString();
			Connection con = DriverManager.getConnection(connection, PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select distinct sourcesystem_cd from i2b2");
			while(rs.next()){
				String id=rs.getString("sourcesystem_cd");
				if(id!=null) ids.add(id);
			}
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
			return  null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return ids;
	}
	/**
	 *Returns a vector containing studies paths for all loaded studies
	 */	
	public static Vector<String> getStudies(){
		Vector<String> studies=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select c_fullname from i2b2 where c_hlevel=1 and c_comment like 'trial%'");
			while(rs.next()){
				String study=rs.getString("c_fullname");
				studies.add(study);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return studies;
	}
	/**
	 *Returns the study path for a given identifier
	 */	
	public static String getIdFromPath(String path){
		String id="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select distinct sourcesystem_cd from concept_dimension where concept_path='"+path+"'");
			if(rs.next()){
				id=rs.getString("sourcesystem_cd");
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return id;
	}
	/**
	 *Returns the patient count for a clinical study
	 */	
	public static int getClinicalPatientNumber(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct patient_num) from patient_trial where trial='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the patient count for a gene expression study
	 */	
	public static int getGenePatientNumber(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct subject_id) from de_subject_sample_mapping where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the probe count for a gene expression study
	 */	
	public static int getGeneProbeNumber(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct probeset_id) from de_subject_microarray_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the RNAseq transcript count for a study
	 */	
	public static int getRnaSeqTranscriptNumber(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct probeset_id) from de_subject_rna_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the miRNA qPCR probe count for a study
	 */	
	public static int getMiRnaProbeNumber(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct probeset_id) from de_subject_mirna_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the number of lines in DEAPP.DE_GPL_INFO for a given platform id
	 */	
	public static int getGplInfo(String gpl){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_gpl_info where platform='"+gpl+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the platform name from  DEAPP.DE_GPL_INFO for a given platform id
	 */	
	public static String getGplName(String gpl){
		String name="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select title from de_gpl_info where platform='"+gpl+"'");
			if(rs.next()){
				name=rs.getString(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return name;
	}
	/**
	 *Returns the number of lines in I2B2METADATA.I2B2 for a given study
	 */	
	public static int getI2b2Lines(String study, HashSet<String> paths){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from i2b2 where sourcesystem_cd='"+study+"' and c_fullname in "+getVectorForSQL(paths));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	 *Returns the number of lines in I2B2METADATA.I2B2_SECURE for a given study
	 */	
	public static int getI2b2SecLines(String study, HashSet<String> paths){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from i2b2_secure where sourcesystem_cd='"+study+"' and c_fullname in "+getVectorForSQL(paths));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of patients in I2B2DEMODATA.PATIENT_DIMENSION corresponding to a vector and for a given study 
	*/	
	public static int getPatientLines(String study, HashSet<String> subjects){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from patient_dimension where sourcesystem_cd like '"+study+":%' and sourcesystem_cd in "+getVectorForSQL(subjects));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in I2B2DEMODATA.CONCEPT_DIMENSION corresponding to a vector and for a given study 
	*/	
	public static int getConceptsLines(String study, HashSet<String> paths){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from concept_dimension where sourcesystem_cd = '"+study+"' and concept_path in "+getVectorForSQL(paths));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in I2B2DEMODATA.CONCEPT_COUNTS corresponding to a vector of paths 
	*/	
	public static int getConceptsCountLines(HashSet<String> paths){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from concept_counts where concept_path in "+getVectorForSQL(paths));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in I2B2DEMODATA.OBSERVATION_FACT corresponding to a vector and for a given study 
	*/	
	public static int getObservationLines(String study, HashSet<String> paths, HashSet<String> subjects){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from observation_fact where sourcesystem_cd = '"+study+"' and patient_num in (select patient_num from patient_dimension where sourcesystem_cd in "+getVectorForSQL(subjects)+") and concept_cd in (select concept_cd from concept_dimension where concept_path in "+getVectorForSQL(paths)+")");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in DEAPP.DE_SUBJECT_SAMPLE_MAPPING corresponding to a hashet of sample ids and a given study 
	*/	
	public static int getSamplesLines(String study, HashSet<String> samples){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_subject_sample_mapping where omic_source_study= '"+study+"' and sample_cd in "+getVectorForSQL(samples));
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Retrieve the title of a platform from its id 
	*/	
	public static String getPlatformName(String id){
		String platform="";
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select title from de_gpl_info where platform='"+id+"'");
			if(rs.next()){
				platform=rs.getString(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return platform;
	}
	/**
	*Utility to transform a vector to a string usable as SQL list 
	*/	
	public static String getVectorForSQL(HashSet<String> h){
		String s="(";
		Iterator<String> it=h.iterator();
		for(int i=0; i<h.size()-1; i++){
			if(it.hasNext()){
				s+="'"+it.next()+"',";
			}
		}
		if(it.hasNext()){
			s+="'"+it.next()+"'";
		}
		s+=")";
		return s;
	}
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_DATASET_LOC for a study 
	*/	
	public static int getDatasetLoc(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_data_dataset_loc where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	/**
	*Retrieve the number of lines of DEAPP.DE_SUBJECT_SNP_DATASET for a study 
	*/	
	public static int getSubjectSnp(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_subject_snp_dataset where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_PROBE_SORTED_DEF for a study 
	*/	
	public static int getProbeSortSnp(String platform){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_probe_sorted_def where platform_name='"+platform+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_SUBJECT_SORTED_DEF for a study 
	*/	
	public static int getSubjectSortSnp(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_subject_sorted_def where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_CALLS_BY_GSM for a study 
	*/	
	public static int getCallsSnp(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_calls_by_gsm where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_DATA_BY_PATIENT for a study 
	*/	
	public static int getDataByPatientSnp(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_data_by_patient where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Retrieve the number of lines of DEAPP.DE_SNP_DATA_BY_PROBEfor a study 
	*/	
	public static int getDataByProbeSnp(String study){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from de_snp_data_by_probe where trial_name='"+study+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in DEAPP.DE_SNP_INFO corresponding to a hashet of probes 
	*/	
	public static int getSnpInfo(HashSet<String> probes){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			Iterator<String> it=probes.iterator();
			for(int i=0; i<probes.size(); i+=999){
				HashSet<String> probesSubset=new HashSet<String>();
				for(int j=0; j<999; j++){
					if(it.hasNext()){
						probesSubset.add(it.next());
					}else{
						break;
					}
				}
				ResultSet rs=stmt.executeQuery("select count(*) from de_snp_info where name in "+getVectorForSQL(probesSubset));
				if(rs.next()){
					n+=rs.getInt(1);
				}
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in DEAPP.DE_SNP_Probe corresponding to a hashet of probes 
	*/	
	public static int getSnpProbe(HashSet<String> probes){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			Iterator<String> it=probes.iterator();
			for(int i=0; i<probes.size(); i+=999){
				HashSet<String> probesSubset=new HashSet<String>();
				for(int j=0; j<999; j++){
					if(it.hasNext()){
						probesSubset.add(it.next());
					}else{
						break;
					}
				}
				ResultSet rs=stmt.executeQuery("select count(*) from de_snp_probe where probe_name in "+getVectorForSQL(probesSubset));
				if(rs.next()){
					n+=rs.getInt(1);
				}
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns the number of lines in DEAPP.DE_SNP_GENE_MAP corresponding to a hashet of probes 
	*/	
	public static int getGeneMap(HashSet<String> snp){
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			Iterator<String> it=snp.iterator();
			for(int i=0; i<snp.size(); i+=999){
				HashSet<String> snpSubset=new HashSet<String>();
				for(int j=0; j<999; j++){
					if(it.hasNext()){
						snpSubset.add(it.next());
					}else{
						break;
					}
				}
				ResultSet rs=stmt.executeQuery("select count(*) from de_snp_gene_map where snp_name in "+getVectorForSQL(snpSubset));
				if(rs.next()){
					n+=rs.getInt(1);
				}
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	public static int getRbmProbeNumber(String study) {
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct antigen_name) from de_subject_rbm_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	public static int getProteomicsProbeNumber(String study) {
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct protein_annotation_id) from de_subject_protein_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	
	public static int getMetabolomicsProbeNumber(String study) {
		int n=0;
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(distinct metabolite_annotation_id) from de_subject_metabolomics_data where trial_name='"+study.toUpperCase()+"'");
			if(rs.next()){
				n=rs.getInt(1);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return n;
	}
	/**
	*Returns a list of patients for a particular concept path 
	*/
	public static Vector<String> getPatientsForConceptPath(String conceptPath){
		Vector<String> patients=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select o.sourcesystem_cd from concept_dimension c, observation_fact o "+
					"where o.concept_cd= c.concept_cd "+
					"and ((c.concept_path='"+conceptPath+"' and o.valtype_cd='N') "+
					"or('"+conceptPath+"'|| c.name_char||'\\'= c.concept_path and o.valtype_cd='T'))");
			while(rs.next()){
				String sourceSystemCd=rs.getString(1);
				patients.add(sourceSystemCd.split(":")[sourceSystemCd.split(":").length-1]);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return patients;
	}
	public static Vector<String> getpatientsForStudy(String study){
		Vector<String> patients=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select sourcesystem_cd from patient_dimension where sourcesystem_cd like '"+study+":%'");
 
			while(rs.next()){
				String sourceSystemCd=rs.getString(1);
				patients.add(sourceSystemCd.split(":")[sourceSystemCd.split(":").length-1]);
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return patients;
	}
	public static Vector<String> getSamplesForCategory(String category){
		Vector<String> samples=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select sample_cd from de_subject_sample_mapping where concept_code in( select concept_cd from i2b2demodata.concept_dimension where concept_path = '"+category+"')");
			while(rs.next()){
				samples.add(rs.getString(1));
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return samples;
	}
	public static Vector<String> getSamplesForStudy(String study){
		Vector<String> samples=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select sample_cd from de_subject_sample_mapping WHERE trial_name='"+study+"'");
			while(rs.next()){
				samples.add(rs.getString(1));
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return samples;
	}
	public static Vector<String> getConceptsForStudy(String study){
		Vector<String> concepts=new Vector<String>();
		try{
			Class.forName(RetrieveData.getDriverString());
			Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
			Statement stmt = con.createStatement();
			ResultSet rs=stmt.executeQuery("select concept_path from concept_dimension where sourcesystem_cd='"+study+"'");
			while(rs.next()){
				concepts.add(rs.getString(1));
			}
			con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		return concepts;
	}
}

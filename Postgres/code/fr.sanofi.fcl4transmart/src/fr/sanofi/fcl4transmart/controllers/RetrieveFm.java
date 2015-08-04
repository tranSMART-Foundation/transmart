package fr.sanofi.fcl4transmart.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.FolderNode;
import fr.sanofi.fcl4transmart.model.classes.FoldersTree;

public class RetrieveFm {
		private static String message="";
		private static int programId;
		private static String programUid;
		private static int studyId;
		private static boolean foundId;
		public static Vector<String> getPrograms(){
			Vector<String> programs=new Vector<String>();
			if(!RetrieveData.testFmappConnection()){
				message="Can not connect to database";
				return null;
			}
			try{
				Class.forName(RetrieveData.getDriverString());
				Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getFmappUser(), PreferencesHandler.getFmappPwd());
				Statement stmt = con.createStatement();
			    ResultSet rs = stmt.executeQuery("select folder_name from fm_folder where folder_type='PROGRAM'");

			    while(rs.next()){
			    	programs.add(rs.getString("folder_name"));
			    }
			    con.close();
					
			}catch(SQLException sqle){
				return programs;
			}
			catch(ClassNotFoundException cnfe){
				return programs;
			}
			return programs;
		}
		
		//build the tree corresponding to the tranSMART program explorer. A boolean allows indicating if the tree has to contain levels below studies
		public static boolean buildTree(FoldersTree tree, boolean afterStudy){
			if(!RetrieveData.testFmappConnection()){
				message="Can not connect to database";
				return false;
			}
			try{
				HashMap<Integer, FolderNode> nodes=new HashMap<Integer, FolderNode>();
				Class.forName(RetrieveData.getDriverString());
				Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getFmappUser(), PreferencesHandler.getFmappPwd());
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("select folder_id, folder_name, folder_full_name, folder_type, folder_level, parent_id from fm_folder where active_ind=1 order by folder_level");
				while(rs.next()){
					if(rs.getString("folder_type").compareTo("PROGRAM")==0){
						FolderNode program=new FolderNode(null);
						program.setType("PROGRAM");      
						program.setId(Integer.valueOf(rs.getString("folder_id")));	
						program.setName(rs.getString("folder_name"));	
						program.setFullName(rs.getString("folder_full_name"));
						program.setLevel(0);		
						tree.addRoot(program);
						nodes.put(program.getId(), program);
					}else{
						if(afterStudy || rs.getString("folder_type").compareTo("STUDY")==0){
							FolderNode parent=nodes.get(rs.getInt("parent_id"));
							if(parent!=null){
								FolderNode child=new FolderNode(parent);    
								child.setId(rs.getInt("folder_id"));
								child.setName(rs.getString("folder_name"));
								child.setFullName(rs.getString("folder_full_name"));		
								child.setType(rs.getString("folder_type"));
								child.setLevel(rs.getInt("folder_level"));		
								parent.addChild(child);
								nodes.put(child.getId(), child);
							}
						}
					}
				}
			    con.close();
					
			}catch(SQLException sqle){
				return false;
			}
			catch(ClassNotFoundException cnfe){
				return false;
			}
			return true;
		}
				
		public static String getMessage(){
			return message;
		}
		
		//searh for all experiments, return a hashmap with as key the experiment title and as value its accession number
		public static HashMap<String, String> getExperiments(){
			if(!RetrieveData.testBiomartConnection()){
				message="Can not connect to database";
				return null;
			}
			HashMap<String, String> experiments=new HashMap<String, String>();
			
			try{
				Class.forName(RetrieveData.getDriverString());
				Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
				Statement stmt = con.createStatement();
			    ResultSet rs = stmt.executeQuery("select title, accession from bio_experiment where bio_experiment_id in(select bio_data_id from biomart.bio_data_uid where unique_id in (select object_uid from fmapp.fm_folder_association where object_type='bio.Experiment'))");

			    while(rs.next()){
			    	experiments.put(rs.getString("title"), rs.getString("accession"));
			    }
			    con.close();
					
			}catch(SQLException sqle){
				message="Error in database";
				return experiments;
			}
			catch(ClassNotFoundException cnfe){
				message="Error in database connection";
				return experiments;
			}
			message="";
			return experiments;
		}
		
		//get the folder id corresponding to a given study accession number
		public static int getIdByAccession(String access){
			if(!RetrieveData.testBiomartConnection()){
				message="Can not connect to database";
				return -1;
			}
			try{
				Class.forName(RetrieveData.getDriverString());
				Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
				Statement stmt = con.createStatement();
			    ResultSet rs = stmt.executeQuery("select folder_id from fmapp.fm_folder_association where object_uid in(select unique_id from biomart.bio_data_uid where bio_data_id in (select bio_experiment_id from BIOMART.bio_experiment where accession ='"+access+"'))");

			    if(rs.next()){
					message="";
			    	return Integer.valueOf(rs.getString("folder_id"));
			    }
			    con.close();
					
			}catch(SQLException sqle){
				message="Error in database";
				return -1;
			}
			catch(ClassNotFoundException cnfe){
				message="Can not connect to database";
				return -1;
			}catch(NumberFormatException nfe){
				message="Study not found in database";
				return -1;
			}
			message="Study not found in database";
			return -1;
		}
		
		//search the top node for a given study accession
		public static String searchTopNode(FoldersTree tree, String study){
			int id=getIdByAccession(study);
			if(id==-1) return "";
			studyId=id;
			String path="";
			foundId=false;
			Vector<FolderNode> roots=tree.getRoots();
			for(FolderNode root: roots){
				path=searchTopNodeR(root, id, "");
				if(foundId){
					programId=root.getId();
					programUid=root.getObjectUid();
					return path;
				}
			}
			return path;
		}
		//function to recursively search top node (called by teh function searchTopNode)
		private static String searchTopNodeR(FolderNode node, int id, String path){
			String parentPath="";
			if(path.compareTo("")==0){
				path+="\\"+node.toString()+"\\";
			}else{
				parentPath=path;
				path+=node.toString()+"\\";
			}
			if(node.getId()==id){
				foundId=true;
				return path;
			}
			for(FolderNode child: node.getChildren()){
				if(!foundId){
					path=searchTopNodeR(child, id, path);
					//if(path.compareTo("")!=0) break;
				}
			}
			if(foundId){
				return path;
			}else{
				return parentPath;
			}
		}
		public static int getProgramId(){
			return programId;
		}
		public static String getProgramUid(){
			return programUid;
		}
		public static int getStudyId(){
			return studyId;
		}
		
		//search for assays of a given study accession number, return an hashmap with as key the assay title and as value the assay id 
		public static HashMap<String, String> getAssays(String access){
			
			if(!RetrieveData.testBiomartConnection()){
				message="Can not connect to database";
				return null;
			}
			HashMap<String, String> assays=new HashMap<String, String>();
			int studyId=getIdByAccession(access);
			if(studyId==-1) return null;
			try{
				Class.forName(RetrieveData.getDriverString());
				Connection con = DriverManager.getConnection(RetrieveData.getConnectionString(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd());
				Statement stmt = con.createStatement();
			    ResultSet rs = stmt.executeQuery("select analysis_name, bio_assay_analysis_id  from BIOMART.bio_assay_analysis where bio_assay_analysis_id in (select bio_data_id from biomart.bio_data_uid where unique_id in(select object_uid from fmapp.fm_folder_association where folder_id in( select folder_id from fmapp.fm_folder where folder_full_name like'%"+studyId+"%' and folder_type='ANALYSIS' and active_ind=1)))");

			    while(rs.next()){
			    	assays.put(rs.getString("analysis_name"), rs.getString("bio_assay_analysis_id"));
			    }
			    con.close();
					
			}catch(SQLException sqle){
				sqle.printStackTrace();
				message="Error in database";
				return null;
			}
			catch(ClassNotFoundException cnfe){
				message="Error in database connection";
				return null;
			}
			message="";
			return assays;
		}

		public static String getStudyTopNode(String accession){
			FoldersTree tree=new FoldersTree();
			buildTree(tree, false);
			return searchTopNode(tree, accession);
		}
}

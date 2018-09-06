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
package fr.sanofi.fcl4transmart.model.classes;

import java.io.File;
import java.util.Vector;

import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionData;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.dataType.MiRnaSeqData;
import fr.sanofi.fcl4transmart.model.classes.dataType.ProteomicsData;
import fr.sanofi.fcl4transmart.model.classes.dataType.QPcrMiRnaData;
import fr.sanofi.fcl4transmart.model.classes.dataType.RbmData;
import fr.sanofi.fcl4transmart.model.classes.dataType.RnaSeqData;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.dataType.StudyDescription;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
/**
 *This class represents a study, and implements the StudyItf interface
 */	
public class Study implements StudyItf{
	private String name;
	private Vector<DataTypeItf> dataTypes;
	private File path;
	private boolean[] areFoldersPresent;
	public Study(String name, File path){
		this.dataTypes=new Vector<DataTypeItf>();
		this.name=name;
		this.path=path;
		this.areFoldersPresent=new boolean[10];
		for(int i=0; i<this.areFoldersPresent.length; i++){
			this.areFoldersPresent[i]=false;
		}
		this.dataTypes.add(new StudyDescription(this));
		this.dataTypes.add(new ClinicalData(this));
		this.dataTypes.add(new GeneExpressionData(this));
		this.dataTypes.add(new GeneExpressionAnalysis(this));
		this.dataTypes.add(new SnpData(this));
		this.dataTypes.add(new RnaSeqData(this));
		this.dataTypes.add(new QPcrMiRnaData(this));
		this.dataTypes.add(new MiRnaSeqData(this));
		this.dataTypes.add(new ProteomicsData(this));
		this.dataTypes.add(new RbmData(this));
		this.dataTypes.add(new MetabolomicsData(this));
		this.setDataTypesPaths();

	}
	@Override
	public String toString(){
		return this.name;
	}
	public Vector<DataTypeItf> getDataTypes(){
		return this.dataTypes;
	}
	/**
	 *Checks that all required folders are present. This presence is set in method setDataTypesPath()
	 */	
	public Vector<String> getMissingFolders(){
		Vector<String> missingFolders=new Vector<String>();
		if(!this.areFoldersPresent[0]){
			missingFolders.add("clinical");
		}
		if(!this.areFoldersPresent[1]){
			missingFolders.add("gene");
		}
		if(!this.areFoldersPresent[2]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"analysis");
			dir.mkdir();
			this.dataTypes.get(3).setFiles(dir);
		}
		if(!this.areFoldersPresent[3]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"snp");
			dir.mkdir();
			this.dataTypes.get(4).setFiles(dir);
		}
		if(!this.areFoldersPresent[4]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"rnaSeq");
			dir.mkdir();
			this.dataTypes.get(5).setFiles(dir);
		}
		if(!this.areFoldersPresent[5]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"qPCR_MiRNA");
			dir.mkdir();
			this.dataTypes.get(6).setFiles(dir);
		}
		if(!this.areFoldersPresent[6]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"miRNA_seq");
			dir.mkdir();
			this.dataTypes.get(7).setFiles(dir);
		}
		if(!this.areFoldersPresent[7]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"proteomics");
			dir.mkdir();
			this.dataTypes.get(8).setFiles(dir);
		}
		if(!this.areFoldersPresent[8]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"rbm");
			dir.mkdir();
			this.dataTypes.get(9).setFiles(dir);
		}
		if(!this.areFoldersPresent[9]){
			File dir=new File(path.getAbsoluteFile()+File.separator+"metabolomics");
			dir.mkdir();
			this.dataTypes.get(10).setFiles(dir);
		}
		return missingFolders;
	}
	public void setName(String name){
		this.name=name;
	}
	public File getPath(){
		return this.path;
	}
	public void setPath(File newPath){
		this.path=newPath;
		this.setDataTypesPaths();
	}
	public String getTopNode(){
		FoldersTree folders=new FoldersTree();
		RetrieveFm.buildTree(folders, false);
		return RetrieveFm.searchTopNode(folders, this.name);
	}
	/**
	 *Check the folder names, set the paths to the data types and set the folder presence
	 */	
	public void setDataTypesPaths(){
		File[] children=this.path.listFiles();
		for(int i=0; i<children.length; i++){
			if(children[i].isDirectory()){
				if(children[i].getName().compareTo("clinical")==0){
					this.dataTypes.get(1).setFiles(children[i]);
					this.areFoldersPresent[0]=true;
				}else if(children[i].getName().compareTo("gene")==0){
					this.dataTypes.get(2).setFiles(children[i]);
					this.areFoldersPresent[1]=true;
				}else if(children[i].getName().compareTo("analysis")==0){
					this.dataTypes.get(3).setFiles(children[i]);
					this.areFoldersPresent[2]=true;
				}else if(children[i].getName().compareTo("snp")==0){
					this.dataTypes.get(4).setFiles(children[i]);
					this.areFoldersPresent[3]=true;
				}else if(children[i].getName().compareTo("rnaSeq")==0){
					this.dataTypes.get(5).setFiles(children[i]);
					this.areFoldersPresent[4]=true;
				}else if(children[i].getName().compareTo("qPCR_MiRNA")==0){
					this.dataTypes.get(6).setFiles(children[i]);
					this.areFoldersPresent[5]=true;
				}else if(children[i].getName().compareTo("miRNA_seq")==0){
					this.dataTypes.get(7).setFiles(children[i]);
					this.areFoldersPresent[6]=true;
				}else if(children[i].getName().compareTo("proteomics")==0){
					this.dataTypes.get(8).setFiles(children[i]);
					this.areFoldersPresent[7]=true;
				}else if(children[i].getName().compareTo("rbm")==0){
					this.dataTypes.get(9).setFiles(children[i]);
					this.areFoldersPresent[8]=true;
				}else if(children[i].getName().compareTo("metabolomics")==0){
					this.dataTypes.get(10).setFiles(children[i]);
					this.areFoldersPresent[9]=true;
				}
			}
		}
	}
}

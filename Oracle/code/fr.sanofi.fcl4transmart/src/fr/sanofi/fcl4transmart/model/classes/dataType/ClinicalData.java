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
package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import fr.sanofi.fcl4transmart.model.classes.steps.SetLoadingType;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.CheckIncrementalLoading;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.Monitoring;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.QualityControl;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SelectCMF;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SelectRawFiles;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SelectSampleMapping;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SelectWMF;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetLabelsOntology;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetOtherIds;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetSerialNode;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetStudyTree;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetSubjectsId;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetTerms;
import fr.sanofi.fcl4transmart.model.classes.steps.clinicalData.SetVisitDates;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
/**
 *This class handles the clinical data loading. It contains the paths to the folder representing clinical data for a study, and paths to:
 *-raw data files
 *-column mapping file
 *-word mapping file
 *-Kettle log file
 * The list of steps required for clinical data loading are set in this class
 */	
public class ClinicalData implements DataTypeItf, IncrementalLoadingPossibleItf{
	private Vector<StepItf> steps;
	private Vector<File> rawFiles;
	private HashMap<String, File> mappingFiles;
	private File cmf;//column mapping file
	private File wmf;//word mapping file
	private File logFile; //kettle job log file
	private StudyItf study;
	private File path;
	private File QCLog;
	private File dimFile;
	private File rootPath;
	private Vector<File> subFolders;
	private boolean setFilesForFirstTime;
	public ClinicalData(StudyItf study){
		this.study=study;
		this.steps=new Vector<StepItf>();
		this.subFolders=new Vector<File>();
		this.setFilesForFirstTime=true;
	
		//add the different steps here
		this.steps.add(new SetLoadingType(this));
		this.steps.add(new SelectRawFiles(this));
		this.steps.add(new SelectCMF(this));
		this.steps.add(new SelectWMF(this));
		this.steps.add(new SetSubjectsId(this));
		this.steps.add(new SetOtherIds(this));
		this.steps.add(new SetVisitDates(this));
		this.steps.add(new SetTerms(this));
		this.steps.add(new SetStudyTree(this));
		this.steps.add(new SetLabelsOntology(this));
		this.steps.add(new SelectSampleMapping(this));
		this.steps.add(new SetSerialNode(this));
		this.steps.add(new CheckIncrementalLoading(this));
		this.steps.add(new LoadData(this));
		this.steps.add(new Monitoring(this));
		this.steps.add(new QualityControl(this));
	}
	@Override
	public Vector<StepItf> getSteps() {
		return steps;
	}
	public String toString(){
		return "Clinical data";
	}
	/**
	 *Check for the presence of the different files, set the pats to these files if they are present 
	 */	
	public void setFiles(File path){
		this.path=path;
		this.rawFiles=new Vector<File>();
		this.mappingFiles=new HashMap<String, File>();
		this.cmf=null;
		this.wmf=null;
		this.logFile=null;
		this.dimFile=null;
		this.QCLog=null;
		File[] children=this.path.listFiles();
		this.rawFiles=new Vector<File>();
		Pattern patternCMF=Pattern.compile(".*\\.columns");
		Pattern patternWMF=Pattern.compile(".*\\.words");
		Pattern patternMapping=Pattern.compile(".*_Sample_Mapping.txt");
		Pattern patternDim=Pattern.compile(".*_Display_Mapping_File.txt");
		for(int i=0; i<children.length;i++){
			if(children[i].isFile()){
				Matcher matcherCMF=patternCMF.matcher(children[i].getName());
				Matcher matcherWMF=patternWMF.matcher(children[i].getName());
				Matcher matcherMapping=patternMapping.matcher(children[i].getName());
				Matcher matcherDim=patternDim.matcher(children[i].getName());
				if(matcherCMF.matches()){
					this.cmf=children[i];
				}else if(matcherWMF.matches()){
					this.wmf=children[i];
				}else if(matcherMapping.matches()){
					this.mappingFiles.put(children[i].getName().replace("_Sample_Mapping.txt", ".txt"), children[i]);
				}else if(matcherDim.matches()){
					this.dimFile=children[i];
				}else if(children[i].getName().compareTo("kettle.log")==0){
					this.logFile=children[i];
				}else if(children[i].getName().compareTo("QClog.txt")==0){
					this.QCLog=children[i];
				}
				else{
					this.rawFiles.add(children[i]);
				}
			}else if(this.setFilesForFirstTime){
				this.subFolders.add(children[i]);
			}
		}
		if(this.setFilesForFirstTime){
			this.rootPath=path;
			this.setFilesForFirstTime=false;
		}
	}
	/**
	 *Returns a list of files to display in the file viewer list
	 */	
	public Vector<File> getFiles(){
		Vector<File> v=new Vector<File>();
		for(File f:this.rawFiles){
			v.add(f);
		}
		if(this.cmf!=null) v.add(this.cmf);
		if(this.wmf!=null) v.add(this.wmf);
		for(String k: this.mappingFiles.keySet()){
			v.add(this.mappingFiles.get(k));
		}
		if(this.dimFile!=null) v.add(this.dimFile);
		if(this.logFile!=null) v.add(this.logFile);
		if(this.QCLog!=null) v.add(this.QCLog);
		return v;
	}
	public StudyItf getStudy(){
		return this.study;
	}
	public File getCMF(){
		return this.cmf;
	}
	public File getWMF(){
		return this.wmf;
	}
	public Vector<File> getRawFiles(){
		return this.rawFiles;
	}
	public Vector<String> getRawFilesNames(){
		Vector<String> rawFilesNames=new Vector<String>();
		for(File f: this.rawFiles){
			rawFilesNames.add(f.getName());
		}
		return rawFilesNames;
	}
	public File getPath(){
		return this.path;
	}
	public void addRawFile(File rawFile){
		this.rawFiles.add(rawFile);
	}
	public void setCMF(File cmf){
		this.cmf=cmf;
	}
	public void setWMF(File wmf){
		this.wmf=wmf;
	}
	public void setDimFile(File file){
		this.dimFile=file;
	}
	public File getDimFile(){
		return this.dimFile;
	}
	public File getLogFile(){
		return this.logFile;
	}
	public void setLogFile(File logFile){
		this.logFile=logFile;
		WorkPart.filesChanged(this);
	}
	public void setQClog(File log){
		this.QCLog=log;
		WorkPart.filesChanged(this);
	}
	public void setMappingFile(String rawFileName, File mappingFile){
		this.mappingFiles.put(rawFileName, mappingFile);
	}
	public HashMap<String, File> getMappingFiles(){
		return this.mappingFiles;
	}
	public boolean isIncremental(){
		return (this.rootPath.getAbsolutePath().compareTo(this.path.getAbsolutePath())!=0);
	}
	public void setSubFolder(String name) throws IOException{
		File folder=null;
		if(name.compareTo("")==0){
			folder=rootPath;
		}else{
			for(File f: this.subFolders){
				if(f.getName().compareTo(name)==0){
					folder=f;
				}
			}
			if(folder==null){
				folder=new File(this.rootPath+File.separator+name);
				FileUtils.forceMkdir(folder);
				this.subFolders.add(folder);
			}
		}
		this.setFiles(folder);
	}
	public Vector<File> getSubFolders(){
		return this.subFolders;
	}
}

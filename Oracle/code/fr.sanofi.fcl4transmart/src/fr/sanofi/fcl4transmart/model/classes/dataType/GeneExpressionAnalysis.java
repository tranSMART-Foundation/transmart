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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis.CheckAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis.ChooseAssay;
import fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis.Monitoring;
import fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis.SelectRawFile;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class handles the study description loading. It contains the paths to the folder representing study description data.
 * The list of steps required for study description loading are set in this class
 */	
public class GeneExpressionAnalysis implements DataTypeItf{
	private Vector<StepItf> steps;
	private StudyItf study;
	private File path;
	private String analysisId;
	private File rawFile;
	private File annotFile;
	private File analysisPath;
	private File kettleLog;
	public GeneExpressionAnalysis(StudyItf study){
		this.study=study;
		this.steps=new Vector<StepItf>();
		
		//add the different steps here
		this.steps.add(new ChooseAssay(this));
		this.steps.add(new SelectRawFile(this));
		this.steps.add(new CheckAnnotation(this));
		this.steps.add(new LoadData(this));
		this.steps.add(new Monitoring(this));
	}
	@Override
	public Vector<StepItf> getSteps() {
		return this.steps;
	}
	public String toString(){
		return "Gene expression analysis";
	}
	public void setFiles(File path){
		this.path=path;
	}
	public void setAnalysisPath(){
		this.analysisPath=new File(this.path.getAbsolutePath()+File.separator+this.analysisId);
		if(!this.analysisPath.exists()) this.analysisPath.mkdir();
		File[] children=this.analysisPath.listFiles();
		Pattern patternResults=Pattern.compile(".*\\.results");
		Pattern patternAnnot=Pattern.compile(".*\\.annot");
		Pattern patternLog=Pattern.compile(".*\\.log");
		for(int i=0; i<children.length;i++){
			if(children[i].isFile()){
				Matcher matcherResults=patternResults.matcher(children[i].getName());
				Matcher matcherAnnot=patternAnnot.matcher(children[i].getName());
				Matcher matcherLog=patternLog.matcher(children[i].getName());
				if(matcherResults.matches()){
					this.rawFile=children[i];
				}else if(matcherAnnot.matches()){
					this.annotFile=children[i];
				}else if(matcherLog.matches()){
					this.kettleLog=children[i];
				}
			}
		}
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(this);
	}
	public Vector<File> getFiles(){
		Vector<File> files=new Vector<File>();
		if(this.rawFile!=null) files.add(rawFile);
		if(this.annotFile!=null) files.add(annotFile);
		if(this.kettleLog!=null) files.add(kettleLog);
		return files;
	}
	public StudyItf getStudy(){
		return this.study;
	}
	public File getPath(){
		return this.path;
	}
	public void setAnalysisId(String analysisId){
		this.analysisId=analysisId;
		this.setAnalysisPath();
	}
	public String getAnalysisId(){
		return this.analysisId;
	}
	public File getResultsFile(){
		return this.rawFile;
	}
	public File getAnnotFile(){
		return this.annotFile;
	}
	public void setResultsFile(File resultsFile){
		this.rawFile=resultsFile;
	}
	public void setAnnotFile(File annotFile){
		this.annotFile=annotFile;
	}
	public File getAnalysisPath(){
		return this.analysisPath;
	}
	public void setKettleLog(File log){
		this.kettleLog=log;
	}
	public File getKettleLog(){
		return this.kettleLog;
	}
}

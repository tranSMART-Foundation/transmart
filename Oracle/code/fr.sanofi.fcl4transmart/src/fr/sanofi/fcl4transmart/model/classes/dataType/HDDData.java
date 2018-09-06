package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public abstract class HDDData implements DataTypeItf, HDDataItf, IncrementalLoadingPossibleItf {
	protected Vector<StepItf> steps;
	protected Vector<File> rawFiles;
	protected File stsmf;//subject to sample mapping file
	protected File logFile;
	protected File annotationLogFile;
	protected StudyItf study; 
	protected File path;
	protected File QCLog;
	protected File annotationFile;
	protected File dimFile;
	protected File rootPath;
	protected Vector<File> subFolders;
	protected boolean setFilesForFirstTime;
	
	public HDDData(StudyItf study){
		this.study=study;
		this.setFilesForFirstTime=true;
		this.subFolders=new Vector<File>();
		this.steps=new Vector<StepItf>();
		this.addSteps();
	}
	public File getAnnotationFile() {
		return annotationFile;
	}
	public void setAnnotationFile(File annotationFile) {
		this.annotationFile = annotationFile;
	}
	protected abstract void addSteps();
	@Override
	public Vector<StepItf> getSteps() {
		return this.steps;
	}
	public void setFiles(File path){
		this.path=path;
		File[] children=this.path.listFiles();
		initFiles();
		Pattern patternSTSMF=Pattern.compile(".*\\.subject_mapping");
		Pattern patternDimFile=Pattern.compile(".*\\.dimension_mapping");
		Pattern patternAnnotation=Pattern.compile(".*_annotation.txt");
		Pattern patternRaw=Pattern.compile("raw\\..*");
		for(int i=0; i<children.length;i++){
			if(children[i].isFile()){
				Matcher matcherSTSMF=patternSTSMF.matcher(children[i].getName());
				Matcher matcherDimFile=patternDimFile.matcher(children[i].getName());
				Matcher matcherAnnotation=patternAnnotation.matcher(children[i].getName());
				if(matcherSTSMF.matches()) this.stsmf=children[i];
				else if(matcherAnnotation.matches())this.annotationFile=children[i];
				else if(matcherDimFile.matches())this.dimFile=children[i];
				else if(children[i].getName().compareTo("kettle.log")==0) this.logFile=children[i];
				else if(children[i].getName().compareTo("annotation.kettle.log")==0) this.annotationLogFile=children[i];
				else if(children[i].getName().compareTo("QClog.txt")==0) this.QCLog=children[i];
				else{
					Matcher matcherRaw=patternRaw.matcher(children[i].getName());
					if(!matcherRaw.matches()){
						children[i].renameTo(new File(this.path+File.separator+"raw."+children[i].getName()));
					}
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
	public Vector<File> getFiles(){
		Vector<File> v=new Vector<File>();
		if(this.rawFiles!=null) v.addAll(this.rawFiles);
		if(this.stsmf!=null) v.add(this.stsmf);
		if(this.dimFile!=null) v.add(this.dimFile);
		if(this.logFile!=null) v.add(this.logFile);
		if(this.annotationLogFile!=null) v.add(this.annotationLogFile);
		if(this.QCLog!=null) v.add(this.QCLog);
		return v;
	}	
	public StudyItf getStudy(){
		return this.study;
	}
	public File getPath(){
		return this.path;
	}
	public Vector<File> getRawFiles(){
		return this.rawFiles;
	}
	public File getMappingFile(){
		return this.stsmf;
	}
	public Vector<String> getRawFilesNames(){
		Vector<String> rawFilesNames=new Vector<String>();
		for(File f: this.rawFiles){
			rawFilesNames.add(f.getName());
		}
		return rawFilesNames;
	}
	public void addRawFile(File rawFile){
		this.rawFiles.add(rawFile);
	}
	public void setMappingFile(File file){
		this.stsmf=file;
	}
	public File getLogFile(){
		return this.logFile;
	}
	public void setLogFile(File logFile){
		this.logFile=logFile;
	}
	public void setQClog(File log){
		this.QCLog=log;
		WorkPart.filesChanged(this);
	}
	public void setDimFile(File file){
		this.dimFile=file;
	}
	public File getDimFile(){
		return this.dimFile;
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
	protected void initFiles(){
		this.rawFiles=new Vector<File>();
		this.stsmf=null;
		this.logFile=null;
		this.annotationLogFile=null;
		this.QCLog=null;
		this.annotationFile=null;
		this.dimFile=null;
	}
}

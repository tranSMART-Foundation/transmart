package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.sanofi.fcl4transmart.model.classes.steps.SetLoadingType;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.CheckAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.Monitoring;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.QualityControl;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SelectMappingFile;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SelectRawFiles;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetAttribute1;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetAttribute2;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetColumnMapping;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetPlatforms;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetSiteId;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.CheckIncremental;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetSerialNode;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetStudyTree;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetSubjectsId;
import fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData.SetTissueType;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;

public class ProteomicsData extends HDDData {
	private File columnMappingFile;
	
	public ProteomicsData(StudyItf study){
		super(study);
	}
	@Override
	protected void addSteps(){
		this.steps=new Vector<StepItf>();
		this.steps.add(new SetLoadingType(this));
		this.steps.add(new SelectRawFiles(this));
		this.steps.add(new SetColumnMapping(this));
		this.steps.add(new SelectMappingFile(this));
		this.steps.add(new SetSubjectsId(this));
		this.steps.add(new SetPlatforms(this));
		this.steps.add(new SetTissueType(this));
		this.steps.add(new SetSiteId(this));
		this.steps.add(new SetAttribute1(this));
		this.steps.add(new SetAttribute2(this));
		this.steps.add(new SetStudyTree(this));
		this.steps.add(new CheckAnnotation(this));
		this.steps.add(new SetSerialNode(this));
		this.steps.add(new CheckIncremental(this));
		this.steps.add(new LoadData(this));
		this.steps.add(new Monitoring(this));
		this.steps.add(new QualityControl(this));
	}
	@Override
	public void setFiles(File path){
		this.path=path;
		File[] children=this.path.listFiles();
		initFiles();
		this.rawFiles=new Vector<File>();
		Pattern patternSTSMF=Pattern.compile(".*\\.subject_mapping");
		Pattern patternColumnMapping=Pattern.compile(".*\\.column_mapping");
		Pattern patternDimFile=Pattern.compile(".*\\.dimension_mapping");
		Pattern patternAnnotation=Pattern.compile(".*_annotation.txt");
		Pattern patternRaw=Pattern.compile("raw\\..*");
		for(int i=0; i<children.length;i++){
			if(children[i].isFile()){
				Matcher matcherSTSMF=patternSTSMF.matcher(children[i].getName());
				Matcher matcherColumnMapping=patternColumnMapping.matcher(children[i].getName());
				Matcher matcherAnnotation=patternAnnotation.matcher(children[i].getName());
				Matcher matcherDimFile=patternDimFile.matcher(children[i].getName());
				if(matcherSTSMF.matches()) this.stsmf=children[i];
				else if(matcherAnnotation.matches()) this.annotationFile=children[i];
				else if(matcherColumnMapping.matches()) this.columnMappingFile=children[i];
				else if(children[i].getName().compareTo("kettle.log")==0) this.logFile=children[i];
				else if(matcherDimFile.matches())this.dimFile=children[i];
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
	@Override
	public Vector<File> getFiles(){
		Vector<File> v=new Vector<File>();
		if(this.rawFiles!=null) v.addAll(this.rawFiles);
		if(this.stsmf!=null) v.add(this.stsmf);
		if(this.columnMappingFile!=null) v.add(this.columnMappingFile);
		if(this.dimFile!=null) v.add(this.dimFile);
		if(this.logFile!=null) v.add(this.logFile);
		if(this.annotationLogFile!=null) v.add(this.annotationLogFile);
		if(this.QCLog!=null) v.add(this.QCLog);
		return v;
	}	
	public String toString(){
		return "Proteomics (Mass spectrometry) data";
	}
	public File getColumnMappingFile() {
		return columnMappingFile;
	}
	public void setColumnMappingFile(File columnMappingFile) {
		this.columnMappingFile = columnMappingFile;
	}
	@Override
	protected void initFiles(){
		super.initFiles();
		this.columnMappingFile=null;
	}
}

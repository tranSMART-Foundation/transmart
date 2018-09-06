package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.sanofi.fcl4transmart.model.classes.steps.snpData.CheckAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.CheckData;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.CheckMeta;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.Convert;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.LoadAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.LoadMeta;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.SelectAnnotationFile;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.SelectRawFile;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.SetPlatforms;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.SetSubjectId;
import fr.sanofi.fcl4transmart.model.classes.steps.snpData.SetTissue;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;

public class SnpData implements DataTypeItf {
	private Vector<StepItf> steps;
	private StudyItf study;
	private File path;
	private File mappingFile;
	private File rawFile;
	private File annotationFile;
	private File annotationProps;
	private File metaTablesProps;
	private File conversionProps;
	private File dataProps;
	private File logProps;  
	private File loaderProps;
	private Vector<File> outputFiles;
	
	public SnpData(StudyItf study){
		this.study=study;
		this.steps=new Vector<StepItf>();
		this.outputFiles=new Vector<File>();
		
		//add the different steps here
		this.steps.add(new SelectRawFile(this));
		this.steps.add(new SelectAnnotationFile(this));
		this.steps.add(new SetSubjectId(this));
		this.steps.add(new SetPlatforms(this));
		this.steps.add(new SetTissue(this));
		this.steps.add(new LoadAnnotation(this));
		this.steps.add(new CheckAnnotation(this));
		this.steps.add(new LoadMeta(this));
		this.steps.add(new CheckMeta(this));
		this.steps.add(new Convert(this));
		this.steps.add(new LoadData(this));
		this.steps.add(new CheckData(this));
	}
	
	@Override
	public Vector<StepItf> getSteps() {
		return this.steps;
	}

	@Override
	public void setFiles(File path) {
		this.path=path;
		this.outputFiles=new Vector<File>();
		File[] children=this.path.listFiles();
		Pattern patternRaw=Pattern.compile(".*\\.raw");
		Pattern patternAnnotationProps=Pattern.compile("annotation\\.properties");
		Pattern patternMeta=Pattern.compile("metaTables\\.properties");
		Pattern patternConvert=Pattern.compile("convert\\.properties");
		Pattern patternData=Pattern.compile("data\\.properties");
		Pattern patternAnnotation=Pattern.compile(".*\\.annotation");
		Pattern patternMapping=Pattern.compile(".*\\.subject_mapping");
		Pattern patternLog=Pattern.compile("log4j\\.properties");
		Pattern patternChr=Pattern.compile("chr.*");
		Pattern patternAll=Pattern.compile("all.*");
		for(int i=0; i<children.length;i++){
			if(children[i].isFile()){
				Matcher matcherRaw=patternRaw.matcher(children[i].getName());
				Matcher matcherAnnotationProps=patternAnnotationProps.matcher(children[i].getName());
				Matcher matcherAnnotation=patternAnnotation.matcher(children[i].getName());
				Matcher matcherMapping=patternMapping.matcher(children[i].getName());
				Matcher matcherLog=patternLog.matcher(children[i].getName());
				Matcher matcherMeta=patternMeta.matcher(children[i].getName());
				Matcher matcherConvert=patternConvert.matcher(children[i].getName());
				Matcher matcherData=patternData.matcher(children[i].getName());
				Matcher matcherChr=patternChr.matcher(children[i].getName());
				Matcher matcherAll=patternAll.matcher(children[i].getName());
				if(matcherRaw.matches()){
					this.rawFile=children[i];
				}else if(matcherAnnotationProps.matches()){
					this.annotationProps=children[i];
				}else if(matcherMeta.matches()){
					this.metaTablesProps=children[i];
				}else if(matcherConvert.matches()){
					this.conversionProps=children[i];
				}else if(matcherData.matches()){
					this.dataProps=children[i];
				}else if(matcherAnnotation.matches()){
					this.annotationFile=children[i];
				}else if(matcherMapping.matches()){
					this.mappingFile=children[i];
				}else if(matcherLog.matches()){
					this.logProps=children[i];
				}else if(!matcherChr.matches() && !matcherAll.matches()){
					this.outputFiles.add(children[i]);
				}
			}
		}
	}

	@Override
	public Vector<File> getFiles() {
		Vector<File> files=new Vector<File>();
		if(this.rawFile!=null) files.add(this.rawFile);
		if(this.annotationFile!=null) files.add(this.annotationFile);
		if(this.mappingFile!=null) files.add(this.mappingFile);
		if(this.annotationProps!=null) files.add(this.annotationProps);
		if(this.metaTablesProps!=null) files.add(this.metaTablesProps);
		if(this.conversionProps!=null) files.add(this.conversionProps);
		if(this.dataProps!=null) files.add(this.dataProps);
		if(this.logProps!=null) files.add(this.logProps);
		files.addAll(outputFiles);
		return files;
	}

	@Override
	public StudyItf getStudy() {
		return this.study;
	}

	@Override
	public File getPath() {
		return this.path;
	}

	public File getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(File mappingFile) {
		this.mappingFile = mappingFile;
	}

	public File getRawFile() {
		return rawFile;
	}

	public void setRawFile(File rawFile) {
		this.rawFile = rawFile;
	}

	public File getAnnotationFile() {
		return annotationFile;
	}
	public void setAnnotationFile(File annotationFile) {
		this.annotationFile = annotationFile;
	}
	public File getAnnotationProps() {
		return annotationProps;
	}
	public void setAnnotationProps(File file){
		this.annotationProps=file;
	}
	public File getLogProps() {
		return logProps;
	}
	public void setLogProps(File file){
		this.logProps=file;
	}

	public File getMetaTablesProps() {
		return metaTablesProps;
	}

	public void setMetaTablesProps(File metaTablesProps) {
		this.metaTablesProps = metaTablesProps;
	}

	public File getConversionProps() {
		return conversionProps;
	}

	public void setConversionProps(File conversionProps) {
		this.conversionProps = conversionProps;
	}

	public File getLoaderProps() {
		return loaderProps;
	}

	public void setLoaderProps(File loaderProps) {
		this.loaderProps = loaderProps;
	}
	public String toString(){
		return "SNP data";
	}
	public File getDataProp(){
		return this.dataProps;
	}
	public void setDataProp(File file){
		this.dataProps=file;
	}
	public boolean checkMappingFileComplete(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(this.mappingFile));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			if(s[0].compareTo("")!=0 && s[2].compareTo("")!=0 && s[3].compareTo("")!=0 && s[4].compareTo("")!=0 && s[5].compareTo("")!=0){
				br.close();
				return true;
			}
			br.close();
		}catch (Exception e){
			return false;
		}
		return false;
	}
	public void addOutputFile(File file){
		if(!this.outputFiles.contains(file)){
			this.outputFiles.add(file);
		}
	}
	public boolean isAnnotationLoaded(){
		File file=new File(this.path+File.separator+"load_annotation.log");
		if(this.outputFiles.contains(file)) return true;
		return false;
	}
	public boolean isMetaLoaded(){
		File file=new File(this.path+File.separator+"load_meta_tables.log");
		if(this.outputFiles.contains(file)) return true;
		return false;
	}
	public boolean isFileConverted(){
		File file=new File(this.path+File.separator+"convert.log");
		if(this.outputFiles.contains(file)) return true;
		return false;
	}
	public boolean isDataLoaded(){
		File file=new File(this.path+File.separator+"load_data.log");
		if(this.outputFiles.contains(file)) return true;
		return false;
	}
}

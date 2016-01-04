package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.LoadAnnotationUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class LoadAnnotation implements StepItf {
	private DataTypeItf dataType;
	private WorkItf workUI;
	public LoadAnnotation(DataTypeItf dataType){
		this.workUI=new LoadAnnotationUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows loading the annotation for a platform.\n"+
				"It has to be done for each study, even if the same annotation has already been loaded, because a study-specific file is created, which is required by following steps.\n"+
				"Before loading annotation, information on platform has to be provided: platform identifier and title, platform marker type (generally \"SNP\") and platform organism.\n"+
				"Then the file containing annotation has to be described, by choosing the columns of the file corresponding to SNP identifiers, probes identifiers, chromosome number, position of the SNP on the chromosome, and optionally the gene identifier corresponding to the SNP.\n"+
				"The steps of the loading can be selected. This can be useful if a part of the loading has succeeded and another failed, to run only the loading part which failed.\n"+
				"Finally, you can choose to use an ETL server to perform the loading.";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null && ((SnpData)this.dataType).getMappingFile()!=null && ((SnpData)this.dataType).checkMappingFileComplete()) return true;
		return false;
	}
	public String toString(){
		return "Load platform Annotation";
	}
}

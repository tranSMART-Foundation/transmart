package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SelectAnnotationFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectAnnotationFile implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SelectAnnotationFile(DataTypeItf dataType){
		this.workUI=new SelectAnnotationFileUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows selecting a platform annotation file for this study. This file can be for instance found in the GEO website\n"
				+"This file has to contain a header line. The header line can be preceded by any number of comments lines beginning with the character \"#\"\n"
				+"The file has to contain four or five columns, corresponding to the rs identifier, the SNP identifier, the chromosome number, the position of the SNP on this chromosome, and optionally the gene corresponding to this SNP\n"
				+"The format is checked when the file is added";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null) return true;
		return false;
	}
	public String toString(){
		return "Select annotation file";
	}
}

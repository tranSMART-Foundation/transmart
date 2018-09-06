package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SelectRawFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectRawFile implements StepItf {
	private WorkItf workUI;
	
	public SelectRawFile(DataTypeItf dataType){
		this.workUI=new SelectRawFileUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows selecting a raw data file containing genotyping data for this study.\n"
				+"This file has to contain a header line beginning by the header \"ID_REF\", and followed by the different sample identifiers. Then each line has to begin with the snp identifier, followed by the genotype for each sample.\n"
				+"The genotype has to be composed of two non-separated letters, for instance \"AB\". Unknown values can be set as \"00\" or \"NC\"\n"
				+"The header line can be preceded by any number of comments lines beginning with the character \"!\"\nThe format is checked when the file is added";
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
	public String toString(){
		return "Select raw data file";
	}
}

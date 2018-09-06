package fr.sanofi.fcl4transmart.model.classes.steps.clinicalData;

import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SelectSampleMappingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectSampleMapping implements StepItf {
	private DataTypeItf dataType;
	private WorkItf ui;
	public SelectSampleMapping(DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=new SelectSampleMappingUI(this.dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows selecting sample mapping files. A sample mapping file corresponds to a given raw data file, and contain the same number and order of rows and columns. Then each cell can contain a sample code corresponding to the given patient and variable.\n"+
				"The format of the files will be checked before adding them to workspace.";
	}

	@Override
	public boolean isAvailable() {
		if(((ClinicalData)this.dataType).getRawFiles().size()<1) return false;
		if(((ClinicalData)this.dataType).getCMF()==null) return false;
		return true;
	}
	public String toString(){
		return "Select sample mapping file (optional)";
	}
}

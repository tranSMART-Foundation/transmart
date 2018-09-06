package fr.sanofi.fcl4transmart.model.classes.steps.metabolomicsData;

import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.MetabolomicsSetColumnMappingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetColumnMapping implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SetColumnMapping(DataTypeItf dataType){
		this.dataType=dataType;
		this.workUI=new MetabolomicsSetColumnMappingUI(this.dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This steps allow defining the columns from the raw data files that will be loaded.\n"+
				"For each file, choose a column header corresponding to the peptide identifier, the first intensity value and the last intensity value\n"+
				"When you click on the 'OK' button, a column mapping file is created, and the raw data file format is checked give the indicated columns";
	}

	@Override
	public boolean isAvailable() {
		if(((MetabolomicsData)this.dataType).getRawFiles()==null || ((MetabolomicsData)this.dataType).getRawFiles().size()==0){
			return false;
		}
		return true;
	}
	public String toString(){
		return "Set column mapping";
	}
}

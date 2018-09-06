package fr.sanofi.fcl4transmart.model.classes.steps.clinicalData;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.CheckIncrementalLoadingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class CheckIncrementalLoading implements StepItf {
	private DataTypeItf dataType;
	private CheckIncrementalLoadingUI ui;
	public CheckIncrementalLoading(DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=new CheckIncrementalLoadingUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows, in case of incremental loading, to check the modifications that will be brought to the study by the loading.\n"+
					"The modifications can belong to four categories: new variables, new patients, new values for existing variables/patients and overwritten values";
	}

	@Override
	public boolean isAvailable() {
		try{
			if(((ClinicalData)this.dataType).getRawFiles().size()<1) return false;
			if(((ClinicalData)this.dataType).getCMF()==null) return false;
			if(!FileHandler.checkTreeSet(((ClinicalData)this.dataType).getCMF())) return false;
			if(!((ClinicalData)this.dataType).isIncremental()) return false;
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	public String toString(){
		return "Check incremental loading";
	}
}

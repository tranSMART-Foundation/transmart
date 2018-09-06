package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SetSubjectIdUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetSubjectId implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType; 
	public SetSubjectId(DataTypeItf dataType){
		this.workUI=new SetSubjectIdUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows setting the subject identifiers corresponding to samples, and initiates the subject to sample mapping file creation\n"+
				"For each sample identifier in first column, indicate the corresponding subject identifier.";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null) return true;
		return false;
	}
	public String toString(){
		return "Set subject identifiers";
	}
}

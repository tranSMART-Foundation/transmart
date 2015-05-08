package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SetTissueUi;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetTissue implements StepItf {
	private DataTypeItf dataType;
	private WorkItf workUI;
	public SetTissue(DataTypeItf dataType){
		this.workUI=new SetTissueUi(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows setting the tissue type corresponding to samples, and updates the subject to sample mapping file creation\n"+
				"For each sample identifier in first column, indicate the corresponding tissue type.";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null && ((SnpData)this.dataType).getMappingFile()!=null) return true;
		return false;
	}
	public String toString(){
		return "Set tissue type";
	}
}

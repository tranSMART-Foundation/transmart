package fr.sanofi.fcl4transmart.model.classes.steps;

import fr.sanofi.fcl4transmart.model.classes.workUI.SetLoadingTypeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetLoadingType implements StepItf {
	private SetLoadingTypeUI ui;
	public SetLoadingType(DataTypeItf dataType){
		this.ui=new SetLoadingTypeUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows to choose if the loading will be incremental or not.\n"+
				"If incremental loading is chosen, a sub-folder will be needed to store partial data. Select an existing sub-folder in the list, or create a new one by indicating a not-existing name";
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
	public String toString(){
		return "Set loading type";
	}
}

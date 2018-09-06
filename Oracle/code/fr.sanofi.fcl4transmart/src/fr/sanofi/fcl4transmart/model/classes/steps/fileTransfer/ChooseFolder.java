package fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer;

import fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer.ChooseFolderUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class ChooseFolder implements StepItf {
	private WorkItf workUI;
	public ChooseFolder(DataTypeItf dataType){
		this.workUI=new ChooseFolderUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows choosing a folder in tranSMART Program Explorer tree to load files into it.\n"+
				"Click on a folder in the tree, then click on the \"Choose selected folder\" button.\n"+
				"When a folder is selected, it is displayed in orange in the tree.";
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
	public String toString(){
		return "Choose remote folder";
	}
}

package fr.sanofi.fcl4transmart.model.classes.steps.rbmData;

import fr.sanofi.fcl4transmart.model.classes.workUI.rbmData.RbmSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectRawFiles implements StepItf {
	private WorkItf workUI;
	public SelectRawFiles(DataTypeItf dataType){
		this.workUI=new RbmSelectRawFilesUI(dataType);
	}

	@Override
	public String getDescription() {
		return "This step allows choosing raw files, by indicating the paths or by choosing them with the 'browse' button.\n"+
				"When the button 'Add file' is clicked, the format of the file is checked, and then the file is copied in the workspace.\n"+
				"The raw data files have to contain a line for each sample and analyte, with the following columns:\n\t\t id\trid\tsampid\tplate\tvisit_code\tAnalyte (ana_unit)\tLDD\tavalue\tanalval\tbelowLDD\tread_low\tread_hi	logtrans\toutlier";
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Select raw data files";
	}
}

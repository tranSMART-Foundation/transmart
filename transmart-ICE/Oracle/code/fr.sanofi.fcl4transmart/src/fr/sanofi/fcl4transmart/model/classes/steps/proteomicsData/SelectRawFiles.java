package fr.sanofi.fcl4transmart.model.classes.steps.proteomicsData;


import fr.sanofi.fcl4transmart.model.classes.workUI.proteomicsData.ProteomicsSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectRawFiles implements StepItf {
	private WorkItf workUI;
	public SelectRawFiles(DataTypeItf dataType){
		this.workUI=new ProteomicsSelectRawFilesUI(dataType);
	}

	@Override
	public String getDescription() {
		return "This step allows choosing raw files, by indicating the paths or by choosing them with the 'browse' button.\n"+
				"When the button 'Add file' is clicked, the format of the file is checked, and then the file is copied in the workspace.\n"+
				"The raw data files have to contain a line for each transcript, with the first column being the transcript identifiers, and a column for each sample, with the first line being the sample identifiers.";
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

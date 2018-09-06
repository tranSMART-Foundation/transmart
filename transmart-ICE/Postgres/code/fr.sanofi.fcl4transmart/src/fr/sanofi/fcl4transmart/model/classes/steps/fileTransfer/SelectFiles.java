package fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer;

import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer.SelectFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectFiles implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SelectFiles(DataTypeItf dataType){
		this.dataType=dataType;
		this.workUI=new SelectFilesUI(this.dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows selecting local files to load them into tranSMART Browse part.";
	}

	@Override
	public boolean isAvailable() {
		if(((FilesTransfer)this.dataType).getRemotePath()==null || ((FilesTransfer)this.dataType).getRemotePath().compareTo("")==0){
			return false;
		}
		return true;
	}
	public String toString(){
		return "Select files";
	}
}

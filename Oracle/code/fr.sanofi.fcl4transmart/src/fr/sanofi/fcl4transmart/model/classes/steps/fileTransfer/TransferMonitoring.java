package fr.sanofi.fcl4transmart.model.classes.steps.fileTransfer;

import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer.TransferMonitoringUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class TransferMonitoring implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public TransferMonitoring(DataTypeItf dataType){
		this.workUI=new TransferMonitoringUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows monitoring the file transfer progress. A transfer can be cancelled by clicking on the 'Cancel' button.";
	}

	@Override
	public boolean isAvailable() {
		if(((FilesTransfer)this.dataType).getRemotePath()==null || ((FilesTransfer)this.dataType).getRemotePath().compareTo("")==0){
			return false;
		}
		return true;
	}
	public String toString(){
		return "Transfer monitoring";
	}

}

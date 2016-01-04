package fr.sanofi.fcl4transmart.model.classes.workUI.rbmData;

import fr.sanofi.fcl4transmart.controllers.listeners.HDData.RemoveRawFileListener;
import fr.sanofi.fcl4transmart.controllers.listeners.rbmData.RbmSelectRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class RbmSelectRawFilesUI extends SelectRawFilesUI implements WorkItf {
	public RbmSelectRawFilesUI(DataTypeItf dataType){
		super(dataType);
	}
	@Override
	protected void setListeners(){
		this.removeFileListener=new RemoveRawFileListener(this.dataType, this);
		this.addFileListener=new RbmSelectRawFileListener(this, this.dataType);
	}
}

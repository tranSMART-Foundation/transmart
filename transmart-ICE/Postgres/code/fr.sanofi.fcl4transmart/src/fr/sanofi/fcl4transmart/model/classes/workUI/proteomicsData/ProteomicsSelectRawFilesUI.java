package fr.sanofi.fcl4transmart.model.classes.workUI.proteomicsData;


import fr.sanofi.fcl4transmart.controllers.listeners.proteomicsData.ProteomicsRemoveRawFileListener;
import fr.sanofi.fcl4transmart.controllers.listeners.proteomicsData.ProteomicsSelectRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class ProteomicsSelectRawFilesUI extends SelectRawFilesUI implements WorkItf {
	public ProteomicsSelectRawFilesUI(DataTypeItf dataType){
		super(dataType);
	}
	@Override
	protected void setListeners(){
		this.removeFileListener=new ProteomicsRemoveRawFileListener(this.dataType, this);
		this.addFileListener=new ProteomicsSelectRawFileListener(this, this.dataType);
	}
}

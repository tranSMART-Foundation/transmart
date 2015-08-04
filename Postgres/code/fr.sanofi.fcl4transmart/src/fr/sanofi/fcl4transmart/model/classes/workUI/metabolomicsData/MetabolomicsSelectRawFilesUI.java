package fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData;


import fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData.MetabolomicsRemoveRawFileListener;
import fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData.MetabolomicsSelectRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class MetabolomicsSelectRawFilesUI extends SelectRawFilesUI implements WorkItf {
	public MetabolomicsSelectRawFilesUI(DataTypeItf dataType){
		super(dataType);
	}
	@Override
	protected void setListeners(){
		this.removeFileListener=new MetabolomicsRemoveRawFileListener(this.dataType, this);
		this.addFileListener=new MetabolomicsSelectRawFileListener(this, this.dataType);
	}
}

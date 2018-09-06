package fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData;

import java.io.File;

import org.eclipse.swt.widgets.Event;

import fr.sanofi.fcl4transmart.controllers.listeners.HDData.RemoveRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.MetabolomicsSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class MetabolomicsRemoveRawFileListener extends RemoveRawFileListener {
	public MetabolomicsRemoveRawFileListener(DataTypeItf dataType,
			MetabolomicsSelectRawFilesUI ui) {
		super(dataType, ui);
	}
	@Override
	public void handleEvent(Event event) {
		if(!confirmDelete()) return;
		this.deleteMapping();
		this.deleteColumnMapping();
		this.deleteRawFiles();
		this.updateUi();
	}
	public void deleteColumnMapping(){
		File mapping=((MetabolomicsData)this.dataType).getColumnMappingFile();
		if(mapping!=null){
			((MetabolomicsData)this.dataType).setColumnMappingFile(null);
			mapping.delete();
		}
	}
}

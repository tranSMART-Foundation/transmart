package fr.sanofi.fcl4transmart.controllers.listeners.proteomicsData;

import java.io.File;

import org.eclipse.swt.widgets.Event;

import fr.sanofi.fcl4transmart.controllers.listeners.HDData.RemoveRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ProteomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.proteomicsData.ProteomicsSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class ProteomicsRemoveRawFileListener extends RemoveRawFileListener {
	public ProteomicsRemoveRawFileListener(DataTypeItf dataType,
			ProteomicsSelectRawFilesUI ui) {
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
		File mapping=((ProteomicsData)this.dataType).getColumnMappingFile();
		if(mapping!=null){
			((ProteomicsData)this.dataType).setColumnMappingFile(null);
			mapping.delete();
		}
	}
}

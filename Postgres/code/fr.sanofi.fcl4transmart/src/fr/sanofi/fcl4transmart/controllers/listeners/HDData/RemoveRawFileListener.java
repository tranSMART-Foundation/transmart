package fr.sanofi.fcl4transmart.controllers.listeners.HDData;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.model.classes.dataType.HDDataItf;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class RemoveRawFileListener implements Listener {
	protected SelectRawFilesUI ui;
	protected DataTypeItf dataType;
	protected Vector<File> files;
	public RemoveRawFileListener(DataTypeItf dataType, SelectRawFilesUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		if(!confirmDelete()) return;
		this.deleteMapping();
		this.deleteRawFiles();
		this.updateUi();
	}
	public boolean confirmDelete(){
		files=this.ui.getSelectedRemovedFile();
		if(files.size()<1){
			this.ui.displayMessage("No file selected");
			return false;
		}
		
		return this.ui.confirm("The subject to sample mapping file will be removed consequently.\nAre you sure to remove these files?");
	}
	public void deleteMapping(){
		File mapping=((HDDataItf)this.dataType).getMappingFile();
		if(mapping!=null){
			((HDDataItf)this.dataType).setMappingFile(null);
			mapping.delete();
		}
	}
	public void deleteRawFiles(){
		for(File file: files){
			if(file!=null){
				((HDDataItf)this.dataType).getRawFiles().remove(file);
				file.delete();
			}
		}
	}
	public void updateUi(){
		this.ui.updateViewer();
		WorkPart.updateAll();
		UsedFilesPart.sendFilesChanged(this.dataType);
	}
}

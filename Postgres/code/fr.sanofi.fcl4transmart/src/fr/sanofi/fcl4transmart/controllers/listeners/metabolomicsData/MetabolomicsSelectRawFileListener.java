package fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.MetabolomicsSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class MetabolomicsSelectRawFileListener implements Listener {
	private MetabolomicsSelectRawFilesUI ui;
	private DataTypeItf dataType;
	public MetabolomicsSelectRawFileListener(MetabolomicsSelectRawFilesUI ui, DataTypeItf dataType){
		this.ui=ui;
		this.dataType=dataType;
	}
	@Override
	public void handleEvent(Event event) {
		String[] paths=ui.getPath().split("\\?", -1);
		for(int i=0; i<paths.length; i++){
			String path=paths[i];
			if(path==null) return;
			if(path.contains("%")){
				this.ui.displayMessage("File name can not contain percent ('%') symbol.");
				return;
			}
			File rawFile=new File(path);
			if(rawFile.exists()){
				if(rawFile.isFile()){

					Pattern patternRaw=Pattern.compile("raw\\..*");
					Matcher matcherRaw=patternRaw.matcher(rawFile.getName());
					String newPath;
					if(!matcherRaw.matches()){
						newPath=this.dataType.getPath().getAbsolutePath()+File.separator+"raw."+rawFile.getName();
					}else{
						newPath=this.dataType.getPath().getAbsolutePath()+File.separator+rawFile.getName();
					}
					
					File copiedRawFile=new File(newPath);
					if(!copiedRawFile.exists()){
						try {
							Utils.copyFile(rawFile, copiedRawFile);
							((MetabolomicsData)this.dataType).addRawFile(copiedRawFile);
							
							this.ui.displayMessage("File has been added");
							WorkPart.updateSteps();
							UsedFilesPart.sendFilesChanged(dataType);
						} catch (IOException e) {
							if(copiedRawFile.exists()) copiedRawFile.delete();
							ui.displayMessage("File error: "+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
					else{
						this.ui.displayMessage("This file has already been added");
					}
				}
				else{
					this.ui.displayMessage("This is a directory");
				}
			}
			else{
				this.ui.displayMessage("This path does no exist");
			}
		}
		ui.updateViewer();
		WorkPart.updateSteps();
		UsedFilesPart.sendFilesChanged(dataType);

	}
}

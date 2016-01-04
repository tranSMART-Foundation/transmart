package fr.sanofi.fcl4transmart.controllers.listeners.rbmData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.RbmData;
import fr.sanofi.fcl4transmart.model.classes.workUI.rbmData.RbmSelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class RbmSelectRawFileListener implements Listener {
	private RbmSelectRawFilesUI ui;
	private DataTypeItf dataType;
	public RbmSelectRawFileListener(RbmSelectRawFilesUI ui, DataTypeItf dataType){
		this.ui=ui;
		this.dataType=dataType;
	}
	@Override
	public void handleEvent(Event event) {
		String[] paths=ui.getPath().split("\\?", -1);
		for(int i=0; i<paths.length; i++){
			String path=paths[i];
			if(path==null) return;
			if(((RbmData)this.dataType).getRawFiles().size()>0 || paths.length>1){
				this.ui.displayMessage("For RBM data, you can only select one raw data file");
				return;
			}
			if(path.contains("%")){
				this.ui.displayMessage("File name can not contain percent ('%') symbol.");
				return;
			}
			File rawFile=new File(path);
			if(rawFile.exists()){
				if(rawFile.isFile()){
					if(!this.checkFormat(rawFile)) return;

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
							((RbmData)this.dataType).addRawFile(copiedRawFile);
							
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
	public boolean checkFormat(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			line=br.readLine();
			//split must has a limit to take into account empty strings
			int columnsNbr=line.split("\t", -1).length;
			if(columnsNbr<2){
				this.ui.displayMessage("Error:\nAt least two columns are required");
				br.close();
				return false;
			}
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					String[] fields=line.split("\t", -1);
					if(fields.length!=columnsNbr){
						this.ui.displayMessage("Error:\nLines have no the same number of columns");
						br.close();
						return false;
					}
					if(fields[7].compareTo("")!=0){
						try{
							Double.valueOf(fields[7]);
						}
						catch(NumberFormatException e){
							this.ui.displayMessage("Error:\nIntensity values have to be numbers");
							br.close();
							return false;
						}
					}
				}
			}
			br.close();
		}catch (Exception e){
			ui.displayMessage("Error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

}

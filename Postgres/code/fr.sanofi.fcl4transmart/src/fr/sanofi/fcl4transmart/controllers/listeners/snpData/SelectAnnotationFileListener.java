package fr.sanofi.fcl4transmart.controllers.listeners.snpData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SelectAnnotationFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectAnnotationFileListener implements Listener {
	private DataTypeItf dataType;
	private SelectAnnotationFileUI ui;
	public SelectAnnotationFileListener(DataTypeItf dataType, SelectAnnotationFileUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		String path=ui.getPath();
		if(path==null || path.compareTo("")==0){
			this.ui.displayMessage("File path is empty");
			return;
		}
		File file=new File(path);
		if(file.exists() && file.isFile()){
			if(!this.checkFormat(file)) return;

			Pattern patternAnnot=Pattern.compile(".*\\.annotation");
			Matcher matcherAnnot=patternAnnot.matcher(file.getName());
			String newPath;
			if(!matcherAnnot.matches()){
				newPath=this.dataType.getPath().getAbsolutePath()+File.separator+file.getName()+".annotation";
			}else{
				newPath=this.dataType.getPath().getAbsolutePath()+File.separator+file.getName();
			}
			
			File copiedRawFile=new File(newPath);
			if(!copiedRawFile.exists()){
				try {
					Utils.copyFile(file, copiedRawFile);
					((SnpData)this.dataType).setAnnotationFile(copiedRawFile);
					
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
		}else{
			this.ui.displayMessage("File does not exist");
		}
	}
	
	/**
	 *Checks the format of the platform annotation file
	 */	
	public boolean checkFormat(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			int columnsNbr=-1;
			String line;
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					String[] fields=line.split("\t", -1);
					if(columnsNbr==-1){
						//first line, containing headers
						columnsNbr=fields.length;
						if(columnsNbr!=4 && columnsNbr!=5){
							this.ui.displayMessage("Error:\nThe file has to contain 4 or 5 columns");
							br.close();
							return false;
						}
					}else{
						if(fields.length!=columnsNbr){
							this.ui.displayMessage("Error:\nAll lines have not the same number of columns");
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

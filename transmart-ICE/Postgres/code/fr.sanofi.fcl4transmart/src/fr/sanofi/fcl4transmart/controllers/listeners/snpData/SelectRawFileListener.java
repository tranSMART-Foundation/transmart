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
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SelectRawFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectRawFileListener implements Listener {
	private SelectRawFileUI ui;
	private DataTypeItf dataType;
	public SelectRawFileListener(DataTypeItf dataType, SelectRawFileUI ui){
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
		File rawFile=new File(path);
		if(rawFile.exists() && rawFile.isFile()){
			if(!this.checkFormat(rawFile)) return;

			Pattern patternRaw=Pattern.compile(".*\\.raw");
			Matcher matcherRaw=patternRaw.matcher(rawFile.getName());
			String newPath;
			if(!matcherRaw.matches()){
				newPath=this.dataType.getPath().getAbsolutePath()+File.separator+rawFile.getName()+".raw";
			}else{
				newPath=this.dataType.getPath().getAbsolutePath()+File.separator+rawFile.getName();
			}
			
			File copiedRawFile=new File(newPath);
			if(!copiedRawFile.exists()){
				try {
					Utils.copyFile(rawFile, copiedRawFile);
					((SnpData)this.dataType).setRawFile(copiedRawFile);
					
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
	 *Checks the format of the SNP raw data file
	 */	
	public boolean checkFormat(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			int columnsNbr=-1;
			String line;
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("!")!=0){
					String[] fields=line.split("\t", -1);
					if(columnsNbr==-1){
						//first line, containing headers
						columnsNbr=fields.length;
						if(columnsNbr<2){
							this.ui.displayMessage("Error:\nAt least two columns are required");
							br.close();
							return false;
						}
						if(fields[0].replace("\"", "").compareTo("ID_REF")!=0){
							this.ui.displayMessage("Error:\nFirst header is not \"ID_REF\"");
							br.close();
							return false;
						}
					}else{
						if(fields.length!=columnsNbr){
							this.ui.displayMessage("Error:\nAll lines have not the same number of columns");
							br.close();
							return false;
						}
						/*for(int i=1; i<fields.length; i++){
							String[] authorized={"AA", "BB", "AB", "BA",  "00", "NC"};
							boolean ok=false;
							for(int j=0; j<authorized.length; j++){
								if(fields[i].compareTo(authorized[j])==0){
									ok=true;
									break;
								}
							}
							if(!ok){
								this.ui.displayMessage("Error:\nThere are not authorized values in the file: "+fields[i]);
								br.close();
								return false;
							}
						}*/
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

package fr.sanofi.fcl4transmart.controllers.listeners.geneanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.SelectRawFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectGeneAnalysisFileController implements Listener{
	private DataTypeItf dataType;
	private SelectRawFileUI selectRawFileUI;
	public SelectGeneAnalysisFileController(SelectRawFileUI selectRawFileUI, DataTypeItf dataType){
		this.dataType=dataType;
		this.selectRawFileUI=selectRawFileUI;
	}
	@Override
	public void handleEvent(Event event) {
		String path=this.selectRawFileUI.getPath();
		if(path==null) return;
		File file=new File(path);
		if(file.exists()){
			if(file.isFile()){
				if(!this.checkFormat(file)) return;
				String  newPath;
				if(file.getName().endsWith(".results")){
					newPath=((GeneExpressionAnalysis)this.dataType).getAnalysisPath().getAbsolutePath()+File.separator+file.getName();
				}
				else{
					newPath=((GeneExpressionAnalysis)this.dataType).getAnalysisPath().getAbsolutePath()+File.separator+file.getName()+".results";
				}
	
				File copiedFile=new File(newPath);
				try {
					Utils.copyFile(file, copiedFile);
					((GeneExpressionAnalysis)this.dataType).setResultsFile(copiedFile);
					
					this.selectRawFileUI.displayMessage("File has been added");
					WorkPart.updateSteps();
					WorkPart.updateFiles();
					UsedFilesPart.sendFilesChanged(dataType);
				} catch (IOException e) {
					if(copiedFile.exists()) copiedFile.delete();
					this.selectRawFileUI.displayMessage("File error: "+e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
			else{
				this.selectRawFileUI.displayMessage("This is a directory");
			}
		}
		else{
			this.selectRawFileUI.displayMessage("This path does no exist");
		}
	}
	/**
	 *Checks the format of a column mapping file
	 */	
	public boolean checkFormat(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			//header
			String line=br.readLine();
			String[] fields=line.split("\t", -1);
			//check columns number
			if(fields.length!=4){
				this.selectRawFileUI.displayMessage("Error:\nLines have not the right number of columns");
				br.close();
				return false;
			}
			//check headers
			HashMap<String, Integer> headers=new HashMap<String, Integer>();
			headers.put("Probe ID", 0);
			headers.put("Raw P Value", 0);
			headers.put("Adjusted P Value", 0);
			headers.put("Fold Change", 0);
			for(String h: headers.keySet()){
				boolean found=false;
				for(int i=0; i<fields.length; i++){
					if(fields[i].compareTo(h)==0){
						found=true;
						headers.put(h, i);
						break;
					}
				}
				if(!found){
					this.selectRawFileUI.displayMessage("Header "+h+" has not been found");
					br.close();
					return false;
				}
			}
			
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					fields=line.split("\t", -1);
					//check columns number
					if(fields.length!=4){
						this.selectRawFileUI.displayMessage("Error:\nLines have not the right number of columns");
						br.close();
						return false;
					}
					//check mandatory columns (Probe ID, Raw P value, Fold change
					if(fields[headers.get("Probe ID")].compareTo("")==0){
						this.selectRawFileUI.displayMessage("Probe identifiers are required");
						br.close();
						return false;
					}
					if(fields[headers.get("Raw P Value")].compareTo("")==0){
						this.selectRawFileUI.displayMessage("Raw P Value are required");
						br.close();
						return false;
					}
					if(fields[headers.get("Fold Change")].compareTo("")==0){
						this.selectRawFileUI.displayMessage("Fold Change are required");
						br.close();
						return false;
					}
				}
			}
			br.close();
		}catch (Exception e){
			this.selectRawFileUI.displayMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		WorkPart.updateSteps();
		return true;
	}
	
}

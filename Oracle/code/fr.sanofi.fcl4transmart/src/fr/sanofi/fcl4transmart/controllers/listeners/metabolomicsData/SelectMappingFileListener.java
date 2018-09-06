package fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDataItf;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.SelectMappingFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectMappingFileListener implements Listener {
	private DataTypeItf dataType;
	private SelectMappingFileUI ui;
	public SelectMappingFileListener(SelectMappingFileUI ui, DataTypeItf dataType){
		this.ui=ui;
		this.dataType=dataType;		
	}
	@Override
	public void handleEvent(Event event) {
		String path=this.ui.getPath();
		if(path==null) return;
		if(path.contains("%")){
			this.ui.displayMessage("File name can not contain percent ('%') symbol.");
			return;
		}
		File file=new File(path);
		if(file.exists()){
			if(file.isFile()){
				if(!this.checkFormat(file)) return;
				String newPath;
				if(file.getName().endsWith(".subject_mapping")){
					newPath=this.dataType.getPath().getAbsolutePath()+File.separator+file.getName();
				}
				else{
					newPath=this.dataType.getPath().getAbsolutePath()+File.separator+file.getName()+".subject_mapping";
				}
				
				File copiedFile=new File(newPath);
				try {
					Utils.copyFile(file, copiedFile);
					((HDDataItf)this.dataType).setMappingFile(copiedFile);
					
					this.ui.displayMessage("File has been loaded");
					WorkPart.updateSteps();
					UsedFilesPart.sendFilesChanged(this.dataType);
				} catch (IOException e) {
					if(copiedFile.exists()) copiedFile.delete();
					ui.displayMessage("File error: "+e.getLocalizedMessage());
					e.printStackTrace();
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
	/**
	 *Checks the subject to sample mapping file format
	 */	
	public boolean checkFormat(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			Vector<String> samples=new Vector<String>();
			for(File rawFile: ((HDDataItf)this.dataType).getRawFiles()){
				samples.addAll(FileHandler.getProteomicsSamplesId(rawFile, ((MetabolomicsData)this.dataType).getColumnMappingFile()));
			}
			Vector<String> samplesSTSMF=new Vector<String>();
			String category="";
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					String[] fields=line.split("\t", -1);
					//check columns number
					if(fields.length!=9){
						this.ui.displayMessage("Error:\nLines have not the right number of columns");
						br.close();
						return false;
					}
					//check that study id is set
					if(fields[0].compareTo("")==0){
						this.ui.displayMessage("Error:\nStudy identifiers have to be set");
						br.close();
						return false;
					}
					//check that study id is the right one
					if(fields[0].compareTo(this.dataType.getStudy().toString())!=0){
						this.ui.displayMessage("Error:\nStudy identifiers are wrong");
						br.close();
						return false;
					}
					//check that subject id is set
					if(fields[2].compareTo("")==0){
						this.ui.displayMessage("Error:\nSubjects identifiers have to be set");
						br.close();
						return false;
					}	
					//check that samples id is set
					if(fields[3].compareTo("")==0){
						this.ui.displayMessage("Error:\nSamples identifiers have to be set");
						br.close();
						return false;
					}	
					//check that platform is set
					if(fields[4].compareTo("")==0){
						this.ui.displayMessage("Error:\nPlatform has to be set");
						br.close();
						return false;
					}
					//check that tissue type is set
					if(fields[5].compareTo("")==0){
						this.ui.displayMessage("Error:\nTissue type has to be set");
						br.close();
						return false;
					}	
					//check that category codes are set
					if(fields[8].compareTo("")==0){
						this.ui.displayMessage("Error:\nCategory codes have to be set");
						br.close();
						return false;
					}	
					if(category.compareTo("")==0){
						category=fields[8];
					}
					else{
						if(fields[8].compareTo(category)!=0){
							this.ui.displayMessage("Category code has to be always the same");
							br.close();
							return false;
						}
					}
					if(!samplesSTSMF.contains(fields[3])){
						if(samples.contains(fields[3])){
							samplesSTSMF.add(fields[3]);
						}
					}
				}
			}
			if(samplesSTSMF.size()!=samples.size()){
				this.ui.displayMessage("Error:\nSample identifiers are not the same than in raw data file");
				br.close();
				return false;
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

package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SelectSampleMappingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectSampleMappingListener implements Listener {
	private SelectSampleMappingUI ui;
	private Vector<File> rawFiles;
	private Vector<String> pathsToMapping;
	private DataTypeItf dataType;
	public SelectSampleMappingListener(DataTypeItf dataType, SelectSampleMappingUI ui){
		this.ui=ui;
		this.dataType=dataType;
	}
	@Override
	public void handleEvent(Event event) {
		this.rawFiles=((ClinicalData)this.dataType).getRawFiles();
		this.pathsToMapping=ui.getPathsToMapping();
		
		ui.openLoadingShell();
		new Thread(){
			public void run() {
				for(int i=0; i<rawFiles.size(); i++){
					if(pathsToMapping.get(i).compareTo("")!=0){
						File rawFile=rawFiles.get(i);
						File mapFile=new File(pathsToMapping.get(i));
						if(!rawFile.exists()){
							ui.setMessage("Raw file "+rawFile.getName()+" does not exist in workspace");
							ui.setIsLoading(false);
							return;
						}
						if(!mapFile.exists()){
							ui.setMessage("Mapping file "+mapFile.getName()+" does not exist");
							ui.setIsLoading(false);
							return;
						}
						if(!checkFormat(mapFile, rawFile, ((ClinicalData)dataType).getCMF())) return;
						
						File copiedMappingFile=new File(rawFile.getAbsolutePath().replace(".txt", "_Sample_Mapping.txt"));
						
						try {
							Utils.copyFile(mapFile, copiedMappingFile);
						} catch (IOException e) {
							ui.setMessage("Error when copying file: "+e.getMessage());
							ui.setIsLoading(false);
							e.printStackTrace();
							return;
						}
						((ClinicalData)dataType).setMappingFile(rawFile.getName(), copiedMappingFile);
					}
				}
				ui.setMessage("Mapping file(s) added to workspace");
				ui.setIsLoading(false);
			}
		}.start();
		this.ui.waitForThread();
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		WorkPart.filesChanged(dataType);
	}

	public boolean checkFormat(File mappingFile, File rawFile, File columnMappingFile){
		if(columnMappingFile==null){
			ui.setMessage("Column mapping file cannot be found");
			ui.setIsLoading(false);
			return false;
		}
		int rawColumNumber=0;
		HashMap<String, Integer> rawSubjectMap=new HashMap<String, Integer>();
		int mappingColumNumber=0;
		HashMap<String, Integer> mappingSubjectMap=new HashMap<String, Integer>();
			
		int subjectColumn=FileHandler.getNumberForLabel(columnMappingFile, "SUBJ_ID", rawFile)-1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			rawColumNumber=line.split("\t", -1).length;
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					String[] fields=line.split("\t", -1);
					int count = rawSubjectMap.containsKey(fields[subjectColumn]) ? rawSubjectMap.get(fields[subjectColumn]) : 0;
					rawSubjectMap.put(fields[subjectColumn], count + 1);
				}
			}
			br.close();
		}catch (Exception e){
			ui.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			ui.setIsLoading(false);
			return false;
		}
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line=br.readLine();
			if(line==null){
				ui.setMessage("Unable to read file "+mappingFile);
				br.close();
				ui.setIsLoading(false);
				return false;
			}
			mappingColumNumber=line.split("\t", -1).length;
			if(mappingColumNumber!=rawColumNumber){
				ui.setMessage("Mapping file "+mappingFile.getName()+" has not the same number of columns than raw file");
				br.close();
				ui.setIsLoading(false);
				return false;
			}
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					String[] fields=line.split("\t", -1);
					if(fields[subjectColumn].compareTo("")!=0){
						int count = mappingSubjectMap.containsKey(fields[subjectColumn]) ? mappingSubjectMap.get(fields[subjectColumn]) : 0;
						mappingSubjectMap.put(fields[subjectColumn], count + 1);
					}
				}
			}
			br.close();
		}catch (Exception e){
			ui.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			ui.setIsLoading(false);
			return false;
		}
		
		ArrayList<String> keys=new ArrayList<String>();
		keys.addAll(rawSubjectMap.keySet());
		for(String s: mappingSubjectMap.keySet()){
			if(!keys.contains(s)){
				keys.add(s);
			}
		}
		for(String s: keys){
			if(!rawSubjectMap.containsKey(s)){
				ui.setMessage("Format error: patient "+s+" is in mapping file "+mappingFile.getName()+" but not in raw file "+rawFile.getName());
				ui.setIsLoading(false);
				return false;
			}
			if(!mappingSubjectMap.containsKey(s)){
				ui.setMessage("Format error: patient "+s+" is in raw file "+rawFile.getName()+" but not in mapping file "+mappingFile.getName());
				ui.setIsLoading(false);
				return false;
			}
			if(rawSubjectMap.get(s)!=mappingSubjectMap.get(s)){
				ui.setMessage("Format error: patient "+s+" has "+rawSubjectMap.get(s)+"  occurences in raw file "+rawFile.getName()+" but "+mappingSubjectMap.get(s)+" occurences in mapping file "+mappingFile.getName());
				ui.setIsLoading(false);
				return false;
			}
		}
		return true;
	}
}

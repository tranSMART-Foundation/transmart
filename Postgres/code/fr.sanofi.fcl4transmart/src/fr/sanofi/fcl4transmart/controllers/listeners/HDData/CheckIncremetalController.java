package fr.sanofi.fcl4transmart.controllers.listeners.HDData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class CheckIncremetalController {
	private DataTypeItf dataType;
	public CheckIncremetalController(DataTypeItf dataType){
		this.dataType=dataType;
	}
	public String getIncrementalChanges(){
		String text="";
		
		Vector<String> newConcepts=new Vector<String>();
		Vector<String> newSamples=new Vector<String>();
		HashMap<String, Vector<String>> samplesForConcept=new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> addedData=new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> overwrittenData=new HashMap<String, Vector<String>>();
		
		File mappingFile=((HDDData)this.dataType).getMappingFile();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line=br.readLine();
			String topNode=((HDDData)this.dataType).getStudy().getTopNode();
			while ((line=br.readLine())!=null){
				String[] fields=line.split("\t", -1);
				String category=FileHandler.getFullCategoryCode(line);
				Pattern pattern = Pattern.compile(".*Platform not found: (.*)");
				Matcher matcher = pattern.matcher(category);
				if (matcher.find()){
					br.close();
					return "Platform not found: "+matcher.group(1); 
				}
						
				category=topNode+category.replace("+", "\\").replace("_", " ")+"\\";
				Vector<String> v;
				if(!samplesForConcept.containsKey(category)) v=new Vector<String>();
				else v=samplesForConcept.get(category);
				v.add(fields[3]);
				samplesForConcept.put(category, v);
				
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		Vector<String> currentSamples=new Vector<String>();
		for(String category: samplesForConcept.keySet()){
			Vector<String> samples=RetrieveData.getSamplesForCategory(category); 
			if(samples.size()==0){
				newConcepts.add(category);
			}else{
				Vector<String> vOverwritten=new Vector<String>();
				Vector<String> vNew=new Vector<String>();
				for(String sample: samplesForConcept.get(category)){
					if(samples.contains(sample)){
						vOverwritten.add(sample);
					}else{
						vNew.add(sample);
					}
				}
				if(vOverwritten.size()>0) overwrittenData.put(category, vOverwritten);
				if(vNew.size()>0) addedData.put(category, vNew);
			}
		}
		Vector<String> oldSamples=RetrieveData.getSamplesForStudy(dataType.getStudy().toString());
		for(String s: currentSamples){
			if(!oldSamples.contains(s) && !newSamples.contains(s)) newSamples.add(s);
		}
		
		if(newSamples.size()>0){
			text+="The following samples will be added to your study:\n";
			text+=StringUtils.join(newSamples.toArray(), ", ")+"\n";
			text+="\n";
		}
		if(newConcepts.size()>0){
			text+="The following nodes will be added to your study:\n";
			for(String c: newConcepts){
				text+="-"+c+"\n";
			}
			text+="\n";
		}
		if(addedData.size()>0){
			text+="Data will be added for the following nodes for the indicated samples:\n";
			for(String c: addedData.keySet()){
				text+=c+": "+StringUtils.join(addedData.get(c).toArray(), ", ")+"\n";
			}
			text+="\n";
		}
		if(overwrittenData.size()>0){
			text+="Data will be overwritten for the following nodes for the indicated samples:\n";
			for(String c: overwrittenData.keySet()){
				text+=c+": "+StringUtils.join(overwrittenData.get(c).toArray(), ", ")+"\n";
			}
			text+="\n";
		}
		
		if(text.compareTo("")==0) text += "The study will not be modified";
		
		
		return text;
	}

}

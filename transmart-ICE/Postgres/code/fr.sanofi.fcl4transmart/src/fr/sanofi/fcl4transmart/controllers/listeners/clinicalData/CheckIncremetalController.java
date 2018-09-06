package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class CheckIncremetalController {
	private DataTypeItf dataType;
	public CheckIncremetalController(DataTypeItf dataType){
		this.dataType=dataType;
	}
	public String getIncrementalChanges(){
		String text="";
		Vector<String> newConcepts=new Vector<String>();
		Vector<String> newPatients=new Vector<String>();
		HashMap<String, Vector<String>> patientsForConcept=new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> addedData=new HashMap<String, Vector<String>>();
		HashMap<String, Vector<String>> overwrittenData=new HashMap<String, Vector<String>>();
		
		File mappingFile=((ClinicalData)this.dataType).getCMF();
		HashMap<String, Integer> visits=new HashMap<String, Integer>();
		HashMap<String, Vector<String>> dataLabels=new HashMap<String, Vector<String>>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] fields=line.split("\t", -1);
				if(fields[3].compareTo("VISIT_NAME")==0){
					visits.put(fields[0], Integer.valueOf(fields[2]));
				}
				else if(fields[3].compareTo("\\")==0){
					Vector<String> v=new Vector<String>();
					if(dataLabels.containsKey(fields[0])) v=dataLabels.get(fields[0]);
					v.add(fields[2]+";"+fields[4]);
					dataLabels.put(fields[0], v);
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		Vector<String> concepts=new Vector<String>();
		String topNode=((ClinicalData)dataType).getStudy().getTopNode();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] fields=line.split("\t", -1);
				File rawFile=null;
				for(File f: ((ClinicalData)dataType).getRawFiles()){
					if(f.getName().compareTo(fields[0])==0){
						rawFile=f;
						break;
					}
				}
				if(fields[3].compareTo("VISIT_NAME")!=0 && fields[3].compareTo("SUBJ_ID")!=0 && fields[3].compareTo("DATA_LABEL")!=0 &&  
				   fields[3].compareTo("VISIT_DATE")!=0 && fields[3].compareTo("OMIT")!=0 && fields[3].compareTo("ENROLL_DATE")!=0){
					String concept="";
						if(visits.containsKey(fields[0])){
							if(rawFile!=null){
								BufferedReader br2 = new BufferedReader(new FileReader(rawFile));
								String line2=br2.readLine();
								while ((line2=br2.readLine())!=null){
									String[] fields2=line2.split("\t", -1);
									concept=topNode+visits.get(fields[0])+"\\"+fields[1].replace("+", "\\").replace("_", " ")+"\\";
									if(fields[3].compareTo("")==0){
										concept+=FileHandler.getHeaders(rawFile).get(Integer.valueOf(fields[2]))+"\\";
									}else{
										concept+=fields[3]+"\\";
									}
									concept+=fields2[visits.get(fields[0])-1];
								}
								br2.close();
							}					
						}else{
							if(fields[3].compareTo("\\")==0){
								for(String s: dataLabels.get(fields[0])){
									if(s.split(";")[0].compareTo(fields[2])==0){
										int dataLabel=Integer.valueOf(s.split(";")[1].substring(0, 1));
										if(rawFile!=null){
											BufferedReader br2 = new BufferedReader(new FileReader(rawFile));
											String line2=br2.readLine();
											while ((line2=br2.readLine())!=null){
												String[] fields2=line2.split("\t", -1);
												if(fields2[dataLabel-1].compareTo("")!=0){
													concept=topNode+fields[1].replace("+", "\\").replace("_", " ")+"\\"+fields2[dataLabel-1]+"\\";	
												}
											}
											br2.close();
										}
										
									}
								}
							}else{
								concept=topNode+fields[1].replace("+", "\\").replace("_", " ")+"\\";
								if(fields[3].compareTo("")==0){
									concept+=FileHandler.getHeaders(rawFile).get(Integer.valueOf(fields[2]))+"\\";
								}else{
									concept+=fields[3]+"\\";
								}
							}
							
						}
						if(!concepts.contains(concept)) concepts.add(concept);
						
						if(rawFile!=null){
							Vector<String> subjectsId=new Vector<String>();
							int columnNumber=FileHandler.getNumberForLabel(mappingFile, "SUBJ_ID", rawFile);
							for(String s:FileHandler.getTermsByNumber(rawFile, columnNumber)){
								if(!subjectsId.contains(s)){
									subjectsId.add(s);
								}
							}
							patientsForConcept.put(concept, subjectsId);
						}
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		Vector<String> currentPatients=new Vector<String>();
		Vector<String> oldPatients=RetrieveData.getpatientsForStudy(dataType.getStudy().toString());
		for(String s: currentPatients){
			if(!oldPatients.contains(s) && !newPatients.contains(s)) newPatients.add(s);
		}
		Vector<String> oldConcepts=RetrieveData.getConceptsForStudy(dataType.getStudy().toString());
		for(String s: oldConcepts) System.out.println(s);
		for(String concept: concepts){
			Vector<String> patients=RetrieveData.getPatientsForConceptPath(concept); 
			if(!oldConcepts.contains(concept) && !newConcepts.contains(concept)){
				newConcepts.add(concept);
			}else{
				Vector<String> vOverwritten=new Vector<String>();
				Vector<String> vNew=new Vector<String>();
				for(String patient: patientsForConcept.get(concept)){
					if(patients.contains(patient)){
						vOverwritten.add(patient);
					}else{
						vNew.add(patient);
					}
				}
				if(vOverwritten.size()>0) overwrittenData.put(concept, vOverwritten);
				if(vNew.size()>0) addedData.put(concept, vNew);
			}
		}
		
		if(newPatients.size()>0){
			text+="The following subjects will be added to your study:\n";
			text+=StringUtils.join(newPatients.toArray(), ", ")+"\n";
			text+="\n";
		}
		if(newConcepts.size()>0){
			text+="The following concepts will be added to your study:\n";
			for(String c: newConcepts){
				text+="-"+c+"\n";
			}
			text+="\n";
		}
		if(addedData.size()>0){
			text+="Data will be added for the following concepts for the indicated patients:\n";
			for(String c: addedData.keySet()){
				text+=c+": "+StringUtils.join(addedData.get(c).toArray(), ", ")+"\n";
			}
			text+="\n";
		}
		if(overwrittenData.size()>0){
			text+="Data will be overwritten for the following concepts for the indicated patients:\n";
			for(String c: overwrittenData.keySet()){
				text+=c+": "+StringUtils.join(overwrittenData.get(c).toArray(), ", ")+"\n";
			}
			text+="\n";
		}
		
		if(text.compareTo("")==0) text += "The study will not be modified";
		
		return text;
	}
}

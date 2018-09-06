package fr.sanofi.fcl4transmart.controllers.listeners.snpData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SetSubjectIdUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetSubjectIdListener implements Listener {
	private DataTypeItf dataType;
	private SetSubjectIdUI ui;
	public SetSubjectIdListener(DataTypeItf dataType, SetSubjectIdUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		Vector<String> values=this.ui.getValues();
		Vector<String> samples=this.ui.getSamples();
		for(String v: values){
			if(v.compareTo("")==0){
				this.ui.displayMessage("All identifiers have to be set");
				return;
			}
		}
		
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".subject_mapping.tmp");
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			
			File mappingFile=((SnpData)this.dataType).getMappingFile();
			if(mappingFile==null){
				for(int i=0; i<samples.size(); i++){
					out.write(this.dataType.getStudy().toString()+"\t\t"+values.elementAt(i)+"\t"+samples.elementAt(i)+"\t"+"\t"+"\t"+"\t"+"\t"+"\n");
				}
			}
			else{
				try{
					BufferedReader br = new BufferedReader(new FileReader(mappingFile));
					String line;
					while ((line=br.readLine())!=null){
						String[] fields=line.split("\t", -1);
						String sample=fields[3];
						String subject;
						int idx=samples.indexOf(sample);
						if(idx!=-1){
							subject=values.get(idx);
						}
						else{
							br.close();
							return;
						}
						out.write(fields[0]+"\t"+fields[1]+"\t"+subject+"\t"+sample+"\t"+fields[4]+"\t"+fields[5]+"\t"+fields[6]+"\t"+fields[7]+"\t"+fields[8]+"\n");
					}
					br.close();
				}catch (Exception e){
					this.ui.displayMessage("File error: "+e.getLocalizedMessage());
					out.close();
					e.printStackTrace();
				}		
			}
			out.close();
			try{
				File fileDest;
				if(mappingFile!=null){
					String fileName=mappingFile.getName();
					mappingFile.delete();
					fileDest=new File(this.dataType.getPath()+File.separator+fileName);
				}
				else{
					fileDest=new File(this.dataType.getPath()+File.separator+this.dataType.getStudy().toString()+".subject_mapping");
				}			
				FileUtils.moveFile(file, fileDest);
				((SnpData)this.dataType).setMappingFile(fileDest);
			}
			catch(IOException ioe){
				this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
				return;
			}		
		 }catch (Exception e){
			 this.ui.displayMessage("Error: "+e.getLocalizedMessage());
			  e.printStackTrace();
		 }
		this.ui.displayMessage("Subject to sample mapping file updated");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(dataType);
	}

}

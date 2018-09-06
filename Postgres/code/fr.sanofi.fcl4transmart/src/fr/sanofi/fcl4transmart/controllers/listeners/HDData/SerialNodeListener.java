package fr.sanofi.fcl4transmart.controllers.listeners.HDData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDataItf;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SetSerialNodeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SerialNodeListener implements Listener {
	private SetSerialNodeUI ui;
	private DataTypeItf dataType;
	private Vector<String> categoryCodes;
	private Vector<String> values;
	private Vector<String> units;
	public SerialNodeListener(SetSerialNodeUI ui, DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=ui;
	}
	
	@Override
	public void handleEvent(Event event) {
		this.categoryCodes=ui.getCategoriesCode();
		this.values=ui.getValues();
		this.units=ui.getUnits();
		
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".dimension_mapping");
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("Category_CD\tValue\tDisplay Label\tUnit\n");
			
			for(int i=0; i<this.categoryCodes.size(); i++){
				String displayLabel=this.categoryCodes.get(i).split("\\+")[this.categoryCodes.get(i).split("\\+").length-1];
				out.write(this.categoryCodes.get(i)+ "\t"+this.values.get(i)+ "\t"+displayLabel+"\t"+this.units.get(i)+"\n");
			}
			
			out.close();
			((HDDData)dataType).setDimFile(file);
		}catch(IOException ioe){
			this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
			return;
		}		
		
		//update sample mapping file
		File stsmf=((HDDataItf)this.dataType).getMappingFile();
		file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".mapping.tmp");
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("study_id\tsite_id\tsubject_id\tSAMPLE_ID\tPLATFORM\tTISSUETYPE\tATTR1\tATTR2\tcategory_cd\n");
			
			try{
				BufferedReader br = new BufferedReader(new FileReader(stsmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					out.write(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+fields[3]+"\t"+fields[4]+"\t"+fields[5]+"\t"+fields[6]+"\t"+fields[7]+"\t"+FileHandler.getFullCategoryCode(line)+"\n");
				}
				br.close();
			}catch (Exception e){
				this.ui.displayMessage("File error: "+e.getLocalizedMessage());
				out.close();
				e.printStackTrace();
			}	
			out.close();
			try{
				File fileDest;
				if(stsmf!=null){
					fileDest=new File(this.dataType.getPath()+File.separator+stsmf.getName());
				}
				else{
					fileDest=new File(this.dataType.getPath()+File.separator+this.dataType.getStudy().toString()+".subject_mapping");
				}			
				Utils.copyFile(file, fileDest);
				file.delete();
				((HDDataItf)this.dataType).setMappingFile(fileDest);
			}
			catch(IOException ioe){
				this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
				return;
			}		
	  }catch (Exception e){
			this.ui.displayMessage("Error: "+e.getLocalizedMessage());
		  e.printStackTrace();
	  }
		this.ui.displayMessage("Dimension mapping file created");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(dataType);
	}

}

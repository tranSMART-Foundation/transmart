package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SetSerialNodeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SerialNodeListener implements Listener {
	private DataTypeItf dataType;
	private SetSerialNodeUI ui;
	public SerialNodeListener(SetSerialNodeUI ui, DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		Vector<String> categories=this.ui.getCategoriesCode();
		Vector<String> labels=this.ui.getLabels();
		Vector<String> values=this.ui.getValues();
		Vector<String>  units=this.ui.getUnits();
		
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+"_Display_Mapping_File.txt");
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("Category_CD\tValue\tDisplay Label\tUnit\n");
			for(int i=0; i<categories.size(); i++){
				if(units.get(i).compareTo("")!=0 && values.get(i).compareTo("")!=0){
					out.write(categories.get(i)+ "\t"+values.get(i)+ "\t"+labels.get(i)+"\t"+units.get(i)+"\n");
				}
			}
			
			out.close();
			((ClinicalData)dataType).setDimFile(file);
		}catch(IOException ioe){
			this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
			return;
		}		
		
		this.ui.displayMessage("Dimension mapping file created");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(dataType);
	}

}

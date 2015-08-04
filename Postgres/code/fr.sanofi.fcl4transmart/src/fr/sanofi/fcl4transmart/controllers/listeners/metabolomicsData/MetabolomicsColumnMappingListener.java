package fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData;

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
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.MetabolomicsSetColumnMappingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class MetabolomicsColumnMappingListener implements Listener {
	private DataTypeItf dataType;
	private MetabolomicsSetColumnMappingUI ui;
	private Vector<Integer> peptideIdNum;
	private Vector<Integer> valueStartNum;
	private Vector<Integer> valueEndNum;
	public MetabolomicsColumnMappingListener (DataTypeItf dataType, MetabolomicsSetColumnMappingUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		Vector<String> peptideID=this.ui.getPeptideId();
		Vector<String> valuesStart=this.ui.getValuesStart();
		Vector<String> valuesEnd=this.ui.getValuesEnd();
		int size=peptideID.size();
		for(int i=0; i<size; i++){
			if(peptideID.get(i).compareTo("")==0){
				this.ui.displayMessage("All peptide identifiers have to be set");
				return;
			}if(valuesStart.get(i).compareTo("")==0){
				this.ui.displayMessage("All start values have to be set");
				return;
			}if(valuesEnd.get(i).compareTo("")==0){
				this.ui.displayMessage("All end values have to be set");
				return;
			}
		}
		peptideIdNum=new Vector<Integer>();
		valueStartNum=new Vector<Integer>();
		valueEndNum=new Vector<Integer>();
		Vector<File> files=((MetabolomicsData)this.dataType).getRawFiles();
		
		for(int i=0; i<size; i++){
			peptideIdNum.add(FileHandler.getHeaderNumber(files.get(i), peptideID.get(i)));
			valueStartNum.add(FileHandler.getHeaderNumber(files.get(i), valuesStart.get(i))-1);
			valueEndNum.add(FileHandler.getHeaderNumber(files.get(i), valuesEnd.get(i))-1);
		}
		if(!checkFormat(files)) return;
		
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".column_mapping.tmp");
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("Filename\tPeptide Identifier\tIntensity Values Start\tIntensity Values End\n");
			
			File columnMapping=((MetabolomicsData)this.dataType).getColumnMappingFile();
			for(int i=0; i<size; i++){
				out.write(files.get(i).getName()+"\t"+String.valueOf(peptideIdNum.get(i))+"\t"+String.valueOf(valueStartNum.get(i))+"\t"+String.valueOf(valueEndNum.get(i))+"\n");
			}
			
			out.close();
			try{
				File fileDest;
				if(columnMapping!=null){
					String fileName=columnMapping.getName();
					fileDest=new File(this.dataType.getPath()+File.separator+fileName);
				}
				else{
					fileDest=new File(this.dataType.getPath()+File.separator+this.dataType.getStudy().toString()+".column_mapping");
				}			
				Utils.copyFile(file, fileDest);
				file.delete();
				((MetabolomicsData)this.dataType).setColumnMappingFile(fileDest);
			}
			catch(IOException ioe){
				this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
				return;
			}		
		 }catch (Exception e){
			 this.ui.displayMessage("Error: "+e.getLocalizedMessage());
			  e.printStackTrace();
		 }
		this.ui.displayMessage("Column mapping file updated");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(dataType);
	}
	public boolean checkFormat(Vector<File> files){
		for (int i=0; i<files.size(); i++){
			try{
				BufferedReader br = new BufferedReader(new FileReader(files.get(i)));
				String line=br.readLine();
				int columnsNbr=line.split("\t", -1).length;
				if(columnsNbr<2){
					this.ui.displayMessage("Error:\nAt least two columns are required");
					br.close();
					return false;
				}
				while ((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] fields=line.split("\t", -1);
						if(fields.length!=columnsNbr){
							this.ui.displayMessage("Error:\nLines have no the same number of columns");
							br.close();
							return false;
						}
						if(fields[peptideIdNum.get(i)-1].compareTo("")==0){
							this.ui.displayMessage("Error:\nPeptide identifiers have to be set");
							br.close();
							return false;
						}
						for(int j=valueStartNum.get(i); j<valueEndNum.get(i); j++){
							if(fields[j].compareTo("")!=0){
								try{
									Double.valueOf(fields[j]);
								}
								catch(NumberFormatException e){
									this.ui.displayMessage("Error:\nIntensity values have to be numbers");
									br.close();
									return false;
								}
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
		}
		return true;
	}
}

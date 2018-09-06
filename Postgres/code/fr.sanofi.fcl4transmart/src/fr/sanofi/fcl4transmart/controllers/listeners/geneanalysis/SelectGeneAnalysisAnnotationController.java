package fr.sanofi.fcl4transmart.controllers.listeners.geneanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import au.com.bytecode.opencsv.CSVReader;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.CheckAnnotationUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectGeneAnalysisAnnotationController implements Listener{
	private DataTypeItf dataType;
	private CheckAnnotationUI checkAnnotationUI;
	public SelectGeneAnalysisAnnotationController(CheckAnnotationUI checkAnnotationUI, DataTypeItf dataType){
		this.dataType=dataType;
		this.checkAnnotationUI=checkAnnotationUI;
	}
	@Override
	public void handleEvent(Event event) {
		String path=this.checkAnnotationUI.getPath();
		if(path==null) return;
		File file=new File(path);
		if(file.exists()){
			if(file.isFile()){
				String  newPath;
				if(file.getName().endsWith(".annot")){
					newPath=((GeneExpressionAnalysis)this.dataType).getAnalysisPath().getAbsolutePath()+File.separator+file.getName();
				}
				else{
					newPath=((GeneExpressionAnalysis)this.dataType).getAnalysisPath().getAbsolutePath()+File.separator+file.getName()+".annot";
				}
				File newFile=new File(newPath);
				if(!this.checkFormat(file, newFile)) return;
				((GeneExpressionAnalysis)this.dataType).setAnnotFile(newFile);
				this.checkAnnotationUI.displayMessage("File has been added");
				
				WorkPart.updateSteps();
				//to do: update files list
				UsedFilesPart.sendFilesChanged(dataType);
				
			}
			else{
				this.checkAnnotationUI.displayMessage("This is a directory");
			}
		}
		else{
			this.checkAnnotationUI.displayMessage("This path does no exist");
		}
	}
	/**
	 *Checks the format of a column mapping file
	 */	
	public boolean checkFormat(File file, File newFile){
		CSVReader reader;
		BufferedWriter out;
		try{
			reader = new CSVReader(new FileReader(file.getAbsolutePath()));
			FileWriter fw= new FileWriter(newFile);
			out = new BufferedWriter(fw);
			String [] columns;
			while ((columns = reader.readNext()) != null) {
				if(columns[0].indexOf("#")==0){
					//metadata of the file
				}
				else if(columns.length!=41){
					this.checkAnnotationUI.displayMessage("Wrong file format:\nLines have no the right number of columns");
					reader.close();
					out.close();
					newFile.delete();
					return false;
				}else{
					String s="";
					for(int i=0; i<columns.length-1; i++){
						s+="\""+columns[i]+"\""+",";
					}
					s+="\""+columns[columns.length-1]+"\"";
					out.write(s+"\n");
				}
			}
			out.close();
			reader.close();
		}catch (Exception e){
			this.checkAnnotationUI.displayMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		
		WorkPart.updateSteps();
		return true;
	}
	
}

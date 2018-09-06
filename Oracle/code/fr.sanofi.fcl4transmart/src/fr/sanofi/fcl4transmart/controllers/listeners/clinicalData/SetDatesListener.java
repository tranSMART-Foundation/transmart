/*******************************************************************************
 * Copyright (c) 2012 Sanofi-Aventis Recherche et Developpement.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    Sanofi-Aventis Recherche et Developpement - initial API and implementation
 ******************************************************************************/
package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

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
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SetVisitDatesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class controls the visit names and site identifiers selection step
 *Since version 1.2: also controls observation names 
 */	
public class SetDatesListener implements Listener{
	private SetVisitDatesUI setDatesUI;
	private DataTypeItf dataType;
	public SetDatesListener(SetVisitDatesUI setdatesUI, DataTypeItf dataType){
		this.setDatesUI=setdatesUI;
		this.dataType=dataType;
	}
	@Override
	public void handleEvent(Event event) {
		 Vector<File> rawFiles=((ClinicalData)this.dataType).getRawFiles();
		 Vector<String> dates=this.setDatesUI.getDates();
		 Vector<String> formats=this.setDatesUI.getFormats();
		 String enrollFile=this.setDatesUI.getEnrollFile();
		 String enrollColumn=this.setDatesUI.getEnrollColumn();
		 String enrollFormat=this.setDatesUI.getEnrollFormat();
		 
		 if(enrollFile.compareTo("")!=0){
			 if(enrollColumn.compareTo("")==0){
				 this.setDatesUI.displayMessage("Column for enroll date has to be set");
				 return;
			 }
			 if(enrollFormat.compareTo("")==0){
				 this.setDatesUI.displayMessage("Foramt for enroll date has to be set");
				 return;
			 }
		 }
		 
		//check that if there is visit dates, all values for enroll_date are set
		 for(int i=0; i<rawFiles.size(); i++){
			 if(dates.elementAt(i).compareTo("")!=0){	
				  if(formats.elementAt(i).compareTo("")==0){
					this.setDatesUI.displayMessage("Date formats have to be set for each visit date");
					return;
				  }
				  if(enrollFile.compareTo("")==0 || enrollColumn.compareTo("")==0 || enrollFormat.compareTo("")==0){
					  this.setDatesUI.displayMessage("Enroll date has to be set to define visit dates");
					  return;  
				  }
			 }
		 }
		 
		//write in a new file
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".columns.tmp");
		try{			  
			  FileWriter fw = new FileWriter(file);
			  BufferedWriter out = new BufferedWriter(fw);
			  out.write("Filename\tCategory Code\tColumn Number\tData Label\tData Label Source\tControlled Vocab Code\n");
			  
			  if(enrollFile.compareTo("")!=0 && enrollColumn.compareTo("")!=0 && enrollFormat.compareTo("")!=0){
				  out.write(enrollFile+"\t\t"+FileHandler.getHeaderNumber(new File(this.dataType.getPath()+File.separator+enrollFile), enrollColumn)+"\tENROLL_DATE\t"+enrollFormat+"\t\n");
			  }
			  
			  for(int i=0; i<rawFiles.size(); i++){
				  if(dates.elementAt(i).compareTo("")!=0 && formats.elementAt(i).compareTo("")!=0){
					  int columnNumber=FileHandler.getHeaderNumber(rawFiles.elementAt(i), dates.elementAt(i));
					  if(columnNumber!=-1){
						  out.write(rawFiles.elementAt(i).getName()+"\t\t"+columnNumber+"\tVISIT_DATE\t"+formats.elementAt(i)+"\t\n");
					  }
				  }
			  }
			  //add lines from existing CMF
				try{
					BufferedReader br = new BufferedReader(new FileReader(((ClinicalData)this.dataType).getCMF()));
					String line=br.readLine();
					while ((line=br.readLine())!=null){
						String[] s=line.split("\t", -1);
						if(s[3].compareTo("ENROLL_DATE")!=0 && s[3].compareTo("VISIT_DATE")!=0){
							out.write(line+"\n");
						}
					}
					br.close();
				}catch (Exception e){
					this.setDatesUI.displayMessage("Error: "+e.getLocalizedMessage());
					e.printStackTrace();
					out.close();
				}
				out.close();
				try{
					File fileDest=new File(this.dataType.getPath()+File.separator+((ClinicalData)this.dataType).getCMF().getName());
					Utils.copyFile(file, fileDest);
					file.delete();
					((ClinicalData)this.dataType).setCMF(fileDest);
				}
				catch(IOException ioe){
					this.setDatesUI.displayMessage("File error: "+ioe.getLocalizedMessage());
					return;
				}
				
				
			  }catch (Exception e){
				  this.setDatesUI.displayMessage("Error: "+e.getLocalizedMessage());
				  e.printStackTrace();
			  }
			this.setDatesUI.displayMessage("Column mapping file updated");
			WorkPart.updateSteps();
			WorkPart.updateFiles();
			UsedFilesPart.sendFilesChanged(dataType);
	}
}

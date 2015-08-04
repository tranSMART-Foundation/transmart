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
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.SetPlatformUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetPlatformListener implements Listener{
	private DataTypeItf dataType;
	private SetPlatformUI ui;
	public SetPlatformListener(DataTypeItf dataType, SetPlatformUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		Vector<String> values=this.ui.getValues();
		Vector<String> samples=this.ui.getSamples();		
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".subject_mapping.tmp");
		File mappingFile=((SnpData)this.dataType).getMappingFile();
		if(mappingFile==null){
			this.ui.displayMessage("Error: no subject to sample mapping file");
		}
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			
			try{
				BufferedReader br = new BufferedReader(new FileReader(mappingFile));
				String line;
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					String sample=fields[3];
					String platform;
					if(samples.contains(sample)){
						platform=values.get(samples.indexOf(sample));
					}
					else{
						br.close();
						return;
					}
					out.write(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+sample+"\t"+platform+"\t"+fields[5]+"\t"+fields[6]+"\t"+fields[7]+"\t"+fields[8]+"\n");
				}
				br.close();
			}catch (Exception e){
				this.ui.displayMessage("Error: "+e.getLocalizedMessage());
				out.close();
				e.printStackTrace();
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
	}
}

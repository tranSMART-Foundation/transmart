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
package fr.sanofi.fcl4transmart.model.classes.workUI.proteomicsData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.HDData.SetAttribute2Listener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ProteomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SetAttribute2UI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class allows the creation of the composite to set the second optional attribute of the subject to sample mapping file
 */
public class ProteomicsSetAttribute2UI extends SetAttribute2UI implements WorkItf{
	public ProteomicsSetAttribute2UI(DataTypeItf dataType){
		super(dataType);
	}
	@Override
	protected void setListeners(){
		this.setAttributeListener=new SetAttribute2Listener(this.dataType, this);
	}
	@Override
	public void initiate(){
		this.values=new Vector<String>();
		this.samples=new Vector<String>();
		for(File rawFile: ((ProteomicsData)this.dataType).getRawFiles()){
			samples.addAll(FileHandler.getProteomicsSamplesId(rawFile, ((ProteomicsData)this.dataType).getColumnMappingFile()));
		}
		File stsmf=((ProteomicsData)this.dataType).getMappingFile();
		for(@SuppressWarnings("unused") String sample: samples){
			this.values.add("");
		}
		if(stsmf!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(stsmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					String sample=fields[3];
					if(samples.contains(sample)){
						this.values.set(this.samples.indexOf(sample), fields[7]);
					}
					else{
						br.close();
						return;
					}
				}
				br.close();
			}catch (Exception e){
				displayMessage("Error: "+e.getLocalizedMessage());
				e.printStackTrace();
			}		
		}
	}
}

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
package fr.sanofi.fcl4transmart.model.classes.steps.metabolomicsData;

import java.io.File;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.MetabolomicsLoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to load proteomics data
 */	
public class LoadData implements StepItf{
	private WorkItf workUI;
	private DataTypeItf dataType;
	public LoadData(DataTypeItf dataType){
		this.workUI=new MetabolomicsLoadDataUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Load data";
	}
	public String getDescription(){
		return "This step allows loading proteomics data from raw files and mapping files, using a Kettle job.\n"+
				"The Analyze tree is displayed, with the study to load in orange, to check that the study tree is well defined.\n"+
				"A database connection is needed for this step";
	}
	public boolean isAvailable(){
		try{
			if(((MetabolomicsData)this.dataType).getRawFiles()==null || ((MetabolomicsData)this.dataType).getRawFiles().size()==0) return false;
			if(((MetabolomicsData)this.dataType).getColumnMappingFile()==null) return false;
			File stsmf=((MetabolomicsData)this.dataType).getMappingFile();
			if(stsmf==null)return false;
			if(!FileHandler.checkPlatform(stsmf)) return false;
			if(!FileHandler.checkCategoryCodes(stsmf)) return false;
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
}

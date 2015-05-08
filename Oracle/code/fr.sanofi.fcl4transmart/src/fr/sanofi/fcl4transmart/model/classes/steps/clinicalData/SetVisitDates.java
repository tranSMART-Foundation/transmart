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
package fr.sanofi.fcl4transmart.model.classes.steps.clinicalData;

import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SetVisitDatesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to select the column mapping file
 */	
public class SetVisitDates implements StepItf{
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SetVisitDates(DataTypeItf dataType){
		this.workUI=new SetVisitDatesUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Set visit dates (optional)";
	}
	public String getDescription(){
		return "This step allows defining enroll dates and visit dates\n"+
				"Select a file and a columnn corresponding to enroll date, then for each file a column can be chosen to represent visit date.\n"+
				"This step is not used anymore in RC2 ETL";
	}
	public boolean isAvailable(){
		try{
			if(((ClinicalData)this.dataType).getRawFiles().size()<1){
				return false;
			}
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
}

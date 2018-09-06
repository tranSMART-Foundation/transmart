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
package fr.sanofi.fcl4transmart.model.classes.steps.geneanalysis;

import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.ChooseAssayUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to change a study name
 */	
public class ChooseAssay implements StepItf{
	private WorkItf workUI;
	public ChooseAssay(DataTypeItf dataType){
		this.workUI=new ChooseAssayUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Choose assay";
	}
	public String getDescription(){
		return "This step allows choosing an existing analysis where to load data. The analysis has to belong to the selected study.\n"+
				"A database connection is needed for this step.";
	}
	public boolean isAvailable(){
		return true;
	}
}

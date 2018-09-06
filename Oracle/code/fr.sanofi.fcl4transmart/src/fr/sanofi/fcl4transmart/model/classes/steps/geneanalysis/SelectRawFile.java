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

import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.SelectRawFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to change a study name
 */	
public class SelectRawFile implements StepItf{
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SelectRawFile(DataTypeItf dataType){
		this.workUI=new SelectRawFileUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Select analyzed results file";
	}
	public String getDescription(){
		return "This step allows selecting the file containing the analysis results.\n"+
				"The file has to contain the several columns: Probe ID, Raw P Value, Adjusted P Value and Fold Change. All values are mandatory, except for the adjusted p-value, which column can be empty.";
	}
	public boolean isAvailable(){
		if(((GeneExpressionAnalysis)this.dataType).getAnalysisId()==null || ((GeneExpressionAnalysis)this.dataType).getAnalysisId().compareTo("")==0){
			return false;
		}
		return true;
	}
}

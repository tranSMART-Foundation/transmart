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
import fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis.LoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to change a study name
 */	
public class LoadData implements StepItf{
	private WorkItf workUI;
	private DataTypeItf dataType;
	public LoadData(DataTypeItf dataType){
		this.workUI=new LoadDataUI(dataType);
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
		return "This step allows loading data\n"+
				"A database connection is needed for this step.";
	}
	public boolean isAvailable(){
		if(((GeneExpressionAnalysis)this.dataType).getAnalysisId()==null || ((GeneExpressionAnalysis)this.dataType).getAnalysisId().compareTo("")==0){
			return false;
		}
		if(((GeneExpressionAnalysis)this.dataType).getResultsFile()==null) return false;
		return true;
	}
}

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
package fr.sanofi.fcl4transmart.model.classes.steps.studyDescription;

import fr.sanofi.fcl4transmart.model.classes.workUI.description.StudyPathUI;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class represents the step to change a study name
 */	
public class StudyPath implements StepItf{
	private WorkItf workUI;
	public StudyPath(StudyItf study){
		this.workUI=new StudyPathUI(study);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}
	public String toString(){
		return "Study local path";
	}
	public String getDescription(){
		return "This step allows getting the study path in the local computer.";
	}
	public boolean isAvailable(){
		return true;
	}
}

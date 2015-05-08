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
package fr.sanofi.fcl4transmart.model.classes.dataType;


import fr.sanofi.fcl4transmart.model.classes.steps.SetLoadingType;
import fr.sanofi.fcl4transmart.model.classes.steps.geneExpressionData.CheckAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.geneExpressionData.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.geneExpressionData.Monitoring;
import fr.sanofi.fcl4transmart.model.classes.steps.geneExpressionData.QualityControl;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.CheckIncremental;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SelectRawFile;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SelectSTSMF;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetAttribute1;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetAttribute2;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetPlatforms;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetSerialNode;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetSiteId;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetStudyTree;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetSubjectsId;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetTissueType;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
/**
 *This class handles the gene expression data loading. It contains the paths to the folder representing gene expression data for a study, and paths to:
 *-raw data file
 *-subject to sample mapping file
 *-Kettle log file for gene expression loading
 *-Kettle log file for platform annotation loading
 * The list of steps required for gene expression data loading are set in this class
 */	
public class GeneExpressionData extends HDDData{
	public GeneExpressionData(StudyItf study){
		super(study);
	}
	@Override
	protected void addSteps(){
		this.steps.add(new SetLoadingType(this));
		this.steps.add(new SelectRawFile(this));
		this.steps.add(new SelectSTSMF(this));
		this.steps.add(new SetSubjectsId(this));
		this.steps.add(new SetPlatforms(this));
		this.steps.add(new SetTissueType(this));
		this.steps.add(new SetSiteId(this));
		this.steps.add(new SetAttribute1(this));
		this.steps.add(new SetAttribute2(this));
		this.steps.add(new SetStudyTree(this));
		this.steps.add(new CheckAnnotation(this));
		this.steps.add(new SetSerialNode(this));
		this.steps.add(new CheckIncremental(this));
		this.steps.add(new LoadData(this));
		this.steps.add(new Monitoring(this));
		this.steps.add(new QualityControl(this));
	}
	public String toString(){
		return "Gene expression data";
	}
}

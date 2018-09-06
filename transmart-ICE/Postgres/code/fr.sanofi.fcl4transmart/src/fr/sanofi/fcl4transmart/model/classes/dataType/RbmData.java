package fr.sanofi.fcl4transmart.model.classes.dataType;

import java.util.Vector;

import fr.sanofi.fcl4transmart.model.classes.steps.SetLoadingType;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.CheckAnnotation;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.LoadData;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.Monitoring;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.QualityControl;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SelectMappingFile;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SelectRawFiles;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetAttribute1;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetAttribute2;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetPlatforms;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetSiteId;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.CheckIncremental;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetSerialNode;
import fr.sanofi.fcl4transmart.model.classes.steps.HDData.SetStudyTree;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetSubjectsId;
import fr.sanofi.fcl4transmart.model.classes.steps.rbmData.SetTissueType;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;

public class RbmData extends HDDData {
	public RbmData(StudyItf study){
		super(study);
	}
	@Override
	protected void addSteps(){
		this.steps=new Vector<StepItf>();
		this.steps.add(new SetLoadingType(this));
		this.steps.add(new SelectRawFiles(this));
		this.steps.add(new SelectMappingFile(this));
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
		return "RBM data";
	}
}

package fr.sanofi.fcl4transmart.model.classes.steps.HDData;

import java.io.File;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.CheckIncrementalLoadingUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class CheckIncremental implements StepItf {
	private DataTypeItf dataType;
	private WorkItf ui;
	public CheckIncremental(DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=new CheckIncrementalLoadingUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows, in case of incremental loading, to check the modifications that will be brought to the study by the loading.\n"+
				"The modifications can belong to four categories: new variables, new patients, new values for existing variables/patients and overwritten values";
	}

	@Override
	public boolean isAvailable() {
		try{
			if(((HDDData)this.dataType).getRawFiles()==null || ((HDDData)this.dataType).getRawFiles().size()==0) return false;
			File stsmf=((HDDData)this.dataType).getMappingFile();
			if(stsmf==null)return false;
			if(!FileHandler.checkPlatform(stsmf)) return false;
			if(!FileHandler.checkCategoryCodes(stsmf)) return false;
			if(!((HDDData)this.dataType).isIncremental()) return false;
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	public String toString(){
		return "Check incremental loading";
	}
}

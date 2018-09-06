package fr.sanofi.fcl4transmart.model.classes.steps.HDData;

import java.io.File;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDData;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SetSerialNodeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetSerialNode implements StepItf {
	private DataTypeItf dataType;
	private WorkItf ui;
	public SetSerialNode(DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=new SetSerialNodeUI(this.dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows defining serial nodes for data. For each node, set the value and the unit.\n"+
				"This step is optional.\n"+
				"The button 'OK' allows updating the subject to sample mapping file.\n";
	}

	@Override
	public boolean isAvailable() {
		try{
			if(((HDDData)this.dataType).getRawFiles()==null || ((HDDData)this.dataType).getRawFiles().size()==0){
				return false;
			}
			File stsmf=((HDDData)this.dataType).getMappingFile();
			if(stsmf==null){
				return false;
			}
			if(!FileHandler.checkPlatform(stsmf)){
				return false;
			}
			if(!FileHandler.checkCategoryCodes(stsmf)){
				return false;
			}
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	public String toString(){
		return "Set serial nodes (optional)";
	}
}

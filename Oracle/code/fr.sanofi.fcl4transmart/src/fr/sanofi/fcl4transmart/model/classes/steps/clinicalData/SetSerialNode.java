package fr.sanofi.fcl4transmart.model.classes.steps.clinicalData;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SetSerialNodeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SetSerialNode implements StepItf {
	private DataTypeItf dataType;
	private SetSerialNodeUI ui;
	public SetSerialNode(DataTypeItf dataType){
		this.dataType=dataType;
		this.ui=new SetSerialNodeUI(dataType);
	}
	@Override
	public WorkItf getWorkUI() {
		return this.ui;
	}

	@Override
	public String getDescription() {
		return "This step allows defining serial nodes for data. For each node, set the value and the unit.\n"+
				"This step is optional.\n"+
				"The button 'OK' allows updating the dimension mapping file.\n";
	}

	@Override
	public boolean isAvailable() {
		try{
			if(((ClinicalData)this.dataType).getRawFiles().size()<1){
				return false;
			}
			if(((ClinicalData)this.dataType).getCMF()==null){
				return false;
			}
			if(!FileHandler.checkTreeSet(((ClinicalData)this.dataType).getCMF())){
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

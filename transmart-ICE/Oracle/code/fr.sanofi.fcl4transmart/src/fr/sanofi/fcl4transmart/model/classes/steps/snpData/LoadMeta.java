package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.LoadMetaUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class LoadMeta implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public LoadMeta(DataTypeItf dataType){
		this.workUI=new LoadMetaUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows loading the meta tables for a study.\n"+
				"The data tree has to be defined. It represents the path to the node containing SNP in tranSMART. The SNP node will be replaced during loading by the platform name and the tissue type.\n"+
				"Nodes can be added as free text, by indicating a value in the field and clicking on the button  \"Add node\". Then the SNP node is added with the button \"Add SNP node\".\n"+
				"The steps of the loading can be selected. This can be useful if a part of the loading has succeeded and another failed, to run only the loading part which failed.\n"+
				"Finally, you can choose to use an ETL server to perform the loading.\n"+
				"A database connection is needed for this step";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null 
				&& ((SnpData)this.dataType).getMappingFile()!=null && ((SnpData)this.dataType).checkMappingFileComplete()
				&& ((SnpData)this.dataType).isAnnotationLoaded()) return true;
	return false;
	}
	public String toString(){
		return "Load meta tables";
	}
}

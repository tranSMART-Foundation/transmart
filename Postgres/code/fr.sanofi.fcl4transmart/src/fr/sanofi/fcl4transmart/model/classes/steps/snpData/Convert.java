package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.ConvertUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class Convert implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public Convert(DataTypeItf dataType){
		this.workUI=new ConvertUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows converting the files before the data loading\n"+
				"The conversion requires the program plink, which path has to be provided in the field \"Plink executable path\".\n"+
				"The steps of the loading can be selected. This can be useful if a part of the loading has succeeded and another failed, to run only the loading part which failed.\n"+
				"Finally, you can choose to use an ETL server to perform the conversion.\n"+
				"A database connection is needed for this step";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null 
				&& ((SnpData)this.dataType).getMappingFile()!=null && ((SnpData)this.dataType).checkMappingFileComplete()
				&& ((SnpData)this.dataType).isAnnotationLoaded()
				&& ((SnpData)this.dataType).isMetaLoaded()) return true;
		return false;
	}
	public String toString(){
		return "Convert files for loading";
	}
}

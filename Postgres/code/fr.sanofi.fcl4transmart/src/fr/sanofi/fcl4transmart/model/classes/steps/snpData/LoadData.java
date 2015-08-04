package fr.sanofi.fcl4transmart.model.classes.steps.snpData;

import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.LoadDataUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class LoadData implements StepItf {
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

	@Override
	public String getDescription() {
		return "This step allows loading the SNP data for a study.\n"+
				"The first and last chromosomes to treat have to be specified in corresponding fields. Warning: Chromosomes X, Y and XY are numerated as 24, 25 and 26 by Plink, this notation has to be kept for data loading.\n"+
				"The steps of the loading can be selected. This can be useful if a part of the loading has succeeded and another failed, to run only the loading part which failed.\n"+
				"Finally, you can choose to use an ETL server to perform the loading.\n"+
				"A database connection is needed for this step";
	}

	@Override
	public boolean isAvailable() {
		if(((SnpData)this.dataType).getRawFile()!=null && ((SnpData)this.dataType).getAnnotationFile()!=null 
		&& ((SnpData)this.dataType).getMappingFile()!=null && ((SnpData)this.dataType).checkMappingFileComplete()
		&& ((SnpData)this.dataType).isAnnotationLoaded()
		&& ((SnpData)this.dataType).isMetaLoaded() && ((SnpData)this.dataType).isFileConverted()) return true;
		return false;
	}
	public String toString(){
		return "Load data";
	}
}

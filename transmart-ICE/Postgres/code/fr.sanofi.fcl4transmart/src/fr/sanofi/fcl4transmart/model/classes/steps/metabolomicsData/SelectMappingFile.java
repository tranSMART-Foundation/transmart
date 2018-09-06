package fr.sanofi.fcl4transmart.model.classes.steps.metabolomicsData;


import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData.SelectMappingFileUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.StepItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectMappingFile implements StepItf {
	private WorkItf workUI;
	private DataTypeItf dataType;
	public SelectMappingFile(DataTypeItf dataType){
		this.workUI=new SelectMappingFileUI(dataType);
		this.dataType=dataType;
	}
	@Override
	public WorkItf getWorkUI() {
		return this.workUI;
	}

	@Override
	public String getDescription() {
		return "This step allows choosing the subject to sample mapping file, by indicating the path or by choosing it with the 'browse' button.\n"+
				"When the button 'Add file' is clicked, the format of the file is checked, and then the file is copied in the workspace with the extension 'subject_mapping'.\n"+
				"The columns of the subject to sample are the following: study identifier, site identifier, subject identifier, sample identifier, platform, tissue type, attribute 1, attribute 2, category code";
	}

	@Override
	public boolean isAvailable() {
		try{
			if(((MetabolomicsData)this.dataType).getRawFiles()==null || ((MetabolomicsData)this.dataType).getRawFiles().size()==0) return false;
			if(((MetabolomicsData)this.dataType).getColumnMappingFile()==null) return false;
			return true;
		}
		catch(NullPointerException e){
			return false;
		}
	}
	public String toString(){
		return "Select mapping file (optional)";
	}

}

package fr.sanofi.fcl4transmart.controllers.listeners.HDData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDataItf;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SetStudyTreeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetStudyTreeListener implements Listener {
	private DataTypeItf dataType;
	private SetStudyTreeUI ui;
	public SetStudyTreeListener(DataTypeItf dataType, SetStudyTreeUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".mapping.tmp");
		File stsmf=((HDDataItf)this.dataType).getMappingFile();
		if(stsmf==null){
			this.ui.displayMessage("Error: no subject to sample mapping file");
		}
		String category="";
		TreeNode node=this.ui.getRoot();
		if(!node.hasChildren()){
			this.ui.displayMessage("You have to set a category code");
			return;
		}
		node=node.getChildren().get(0);
		while(node!=null){
			if(category.compareTo("")==0){
				category+=node.toString().replace(' ', '_');
			}
			else{
				category+="+"+node.toString().replace(' ', '_');
			}
			if(node.hasChildren()){
				node=node.getChildren().get(0);
			}
			else{
				node=null;
			}
		}
		try{			  
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("study_id\tsite_id\tsubject_id\tSAMPLE_ID\tPLATFORM\tTISSUETYPE\tATTR1\tATTR2\tcategory_cd\n");
			
			try{
				BufferedReader br = new BufferedReader(new FileReader(stsmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					out.write(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+fields[3]+"\t"+fields[4]+"\t"+fields[5]+"\t"+fields[6]+"\t"+fields[7]+"\t"+category+"\n");
				}
				br.close();
			}catch (Exception e){
				this.ui.displayMessage("File error: "+e.getLocalizedMessage());
				out.close();
				e.printStackTrace();
			}	
			out.close();
			try{
				File fileDest;
				if(stsmf!=null){
					fileDest=new File(this.dataType.getPath()+File.separator+stsmf.getName());
				}
				else{
					fileDest=new File(this.dataType.getPath()+File.separator+this.dataType.getStudy().toString()+".subject_mapping");
				}			
				Utils.copyFile(file, fileDest);
				file.delete();
				((HDDataItf)this.dataType).setMappingFile(fileDest);
			}
			catch(IOException ioe){
				this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
				return;
			}		
	  }catch (Exception e){
		  e.printStackTrace();
		  this.ui.displayMessage("Eerror: "+e.getLocalizedMessage());
		  e.printStackTrace();
	  }
	this.ui.displayMessage("Subject to sample mapping file updated");
	WorkPart.updateSteps();
	WorkPart.updateFiles();
	}

}

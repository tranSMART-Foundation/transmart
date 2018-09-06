package fr.sanofi.fcl4transmart.controllers.listeners.HDData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.HDDataItf;
import fr.sanofi.fcl4transmart.model.classes.workUI.HDData.SetPlatformsUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetPlatformListener implements Listener {
	private DataTypeItf dataType;
	private SetPlatformsUI ui;
	private File tmpFile;
	private File stsmf;
	private Vector<String> values;
	private Vector<String> samples;
	public SetPlatformListener(DataTypeItf dataType, SetPlatformsUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		values=this.ui.getValues();
		samples=this.ui.getSamples();		
		tmpFile=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".mapping.tmp");
		stsmf=((HDDataItf)this.dataType).getMappingFile();
		this.writeMappingFile();
	}
	protected void writeMappingFile(){
		if(stsmf==null){
			this.ui.displayMessage("Error: no subject to sample mapping file");
		}
		try{
			FileWriter fw = new FileWriter(tmpFile);
			BufferedWriter out = new BufferedWriter(fw);
			out.write("study_id\tsite_id\tsubject_id\tSAMPLE_ID\tPLATFORM\tTISSUETYPE\tATTR1\tATTR2\tcategory_cd\n");
			try{
				BufferedReader br = new BufferedReader(new FileReader(stsmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					String sample=fields[3];
					String platform;
					if(samples.contains(sample)){
						platform=values.get(samples.indexOf(sample));
					}
					else{
						br.close();
						out.close();
						this.ui.displayMessage("Error: unknown sample "+sample+" in sample mapping file");
						this.tmpFile.delete();
						return;
					}
					out.write(fields[0]+"\t"+fields[1]+"\t"+fields[2]+"\t"+sample+"\t"+platform+"\t"+fields[5]+"\t"+fields[6]+"\t"+fields[7]+"\t"+fields[8]+"\n");
				}
				br.close();
			}catch (Exception e){
				this.ui.displayMessage("Error: "+e.getLocalizedMessage());
				out.close();
				this.tmpFile.delete();
				e.printStackTrace();
			}	
			out.close();
			try{
				File tmpFileDest;
				if(stsmf!=null){
					tmpFileDest=stsmf;
				}
				else{
					tmpFileDest=new File(this.dataType.getPath()+File.separator+this.dataType.getStudy().toString()+".subject_mapping");
				}			
				Utils.copyFile(tmpFile, tmpFileDest);
				tmpFile.delete();
				((HDDataItf)this.dataType).setMappingFile(tmpFileDest);
			}
			catch(IOException ioe){
				this.ui.displayMessage("File error: "+ioe.getLocalizedMessage());
				ioe.printStackTrace();
				this.tmpFile.delete();
				return;
			}		
		}catch (Exception e){
			this.ui.displayMessage("Error: "+e.getLocalizedMessage());
			this.tmpFile.delete();
			e.printStackTrace();
		}
		this.ui.displayMessage("Subject to sample mapping file updated");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
	}
}

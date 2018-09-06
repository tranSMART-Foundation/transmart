/*******************************************************************************
 * Copyright (c) 2012 Sanofi-Aventis Recherche et Developpement.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    Sanofi-Aventis Recherche et Developpement - initial API and implementation
 ******************************************************************************/
package fr.sanofi.fcl4transmart.controllers.listeners.clinicalData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SetStudyTreeUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class controls the study tree setting step
 *v1.2: OMIT lines are not added anymore
 */	
public class SetStudyTreeListener implements Listener{
	private DataTypeItf dataType;
	private SetStudyTreeUI setStudyTreeUI;
	private HashMap<String, String> labels;
	public SetStudyTreeListener(SetStudyTreeUI setStudyTreeUI, DataTypeItf dataType){
		this.setStudyTreeUI=setStudyTreeUI;
		this.dataType=dataType;
		this.labels=new HashMap<String, String>();
	}
	@Override
	public void handleEvent(Event event) {
		if(((ClinicalData)this.dataType).getCMF()==null){
			this.setStudyTreeUI.displayMessage("Error: no column mapping file");
			return;
		}
		File file=new File(this.dataType.getPath().toString()+File.separator+this.dataType.getStudy().toString()+".columns.tmp");
		try{			  
			  FileWriter fw = new FileWriter(file);
			  BufferedWriter out = new BufferedWriter(fw);
			  out.write("Filename\tCategory Code\tColumn Number\tData Label\tData Label Source\tControlled Vocab Code\n");
			  
				try{
					BufferedReader br = new BufferedReader(new FileReader(((ClinicalData)this.dataType).getCMF()));
					String line=br.readLine();
					while ((line=br.readLine())!=null){
						if(line.split("\t", -1)[3].compareTo("SUBJ_ID")==0 || line.split("\t", -1)[3].compareTo("VISIT_NAME")==0 || line.split("\t", -1)[3].compareTo("SITE_ID")==0|| line.split("\t", -1)[3].compareTo("ENROLL_DATE")==0|| line.split("\t", -1)[3].compareTo("VISIT_DATE")==0){
							out.write(line+"\n");
						}
						else if(line.split("\t", -1)[3].compareTo("UNITS")==0){
							out.write(line+"\n");
							File rawFile=new File(this.dataType.getPath()+File.separator+line.split("\t", -1)[0]);
							this.labels.put(rawFile.getName()+"-"+FileHandler.getColumnByNumber(rawFile, Integer.parseInt(line.split("\t", -1)[2])), line.split("\t", -1)[3]);
						}
						else if(line.split("\t", -1)[3].compareTo("OMIT")!=0 && line.split("\t", -1)[3].compareTo("\\")!=0 && line.split("\t", -1)[3].compareTo("DATA_LABEL")!=0 && line.split("\t", -1)[3].compareTo("MEAN")!=0 && line.split("\t", -1)[3].compareTo("MIN")!=0 && line.split("\t", -1)[3].compareTo("MAX")!=0){
							File rawFile=new File(this.dataType.getPath()+File.separator+line.split("\t", -1)[0]);
							this.labels.put(rawFile.getName()+"-"+FileHandler.getColumnByNumber(rawFile, Integer.parseInt(line.split("\t", -1)[2])), line.split("\t", -1)[3]);
						}
					}
					br.close();
				}catch (Exception e){
					this.setStudyTreeUI.displayMessage("File error: "+e.getLocalizedMessage());
					e.printStackTrace();
					out.close();
					return;
				}
				
				this.writeLine(this.setStudyTreeUI.getRoot(), out, "");
				
				out.close();
				try{
					String fileName=((ClinicalData)this.dataType).getCMF().getName();
					File fileDest=new File(this.dataType.getPath()+File.separator+fileName);
					Utils.copyFile(file, fileDest);
					file.delete();
					((ClinicalData)this.dataType).setCMF(fileDest);
				}
				catch(IOException ioe){
					this.setStudyTreeUI.displayMessage("File error: "+ioe.getLocalizedMessage());
					return;
				}
		  }catch (Exception e){
			  this.setStudyTreeUI.displayMessage("Error: "+e.getLocalizedMessage());
			  e.printStackTrace();
		  }
		this.setStudyTreeUI.displayMessage("Column mapping file updated");
		WorkPart.updateSteps();
		WorkPart.updateFiles();
	}
	/**
	 *Writes in the column mapping file the line corresponding to a given node (recursive method)
	 */	
	public void writeLine(TreeNode node, BufferedWriter out, String path){
		for(TreeNode child: node.getChildren()){
			if(child.isOperation()){
				String op=child.toString().split(": ", 2)[0];
				String[] str = (child.toString().split(": ",2)[1]).split(" - ", 2);
				String rawFileName = StringUtils.join(Arrays.asList(str).subList(0, str.length-1), " - ");
				String property = str[str.length-1];
				File rawFile=new File(((ClinicalData)this.dataType).getPath()+File.separator+rawFileName);

				try {
					String dataLabel=this.labels.get(rawFile.getName()+"-"+property);
					if(dataLabel==null){
						dataLabel=property;
					}
					int columnNumber=FileHandler.getHeaderNumber(rawFile, property);
					
					out.write(rawFileName+"\t"+path.replace(' ', '_')+"\t"+columnNumber+"\t"+dataLabel+"\t"+op+"\t\n");
				} catch (IOException e) {
					this.setStudyTreeUI.displayMessage("File error: "+e.getLocalizedMessage());
					e.printStackTrace();
				}
				
			}else{
				String newPath=path;
				if(child.isLabel()){
					if(child.getParent().isLabel()){//has a data label source
						String fullname=child.toString();
						String[] str = fullname.split(" - ", -1);
						String rawFileName=StringUtils.join(Arrays.asList(str).subList(0, str.length-1), " - ");
						String header=str[str.length-1];
						System.out.println(rawFileName);

						File rawFile=new File(((ClinicalData)this.dataType).getPath()+File.separator+rawFileName);
						try {
							String dataLabel=this.labels.get(rawFile.getName()+"-"+header);
							if(dataLabel==null){
								dataLabel=header;
							}
							int columnNumber=FileHandler.getHeaderNumber(rawFile, header);
							out.write(rawFileName+"\t"+path.replace(' ', '_')+"\t"+columnNumber+"\t"+"\\"+"\t"+FileHandler.getHeaderNumber(rawFile, child.getParent().toString().split(" - ", -1)[1])+"B\t\n");
						} catch (IOException e) {
							this.setStudyTreeUI.displayMessage("File error: "+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}else{
						String fullname=child.toString();
						String[] str = fullname.split(" - ", -1);
						String rawFileName=StringUtils.join(Arrays.asList(str).subList(0, str.length-1), " - ");
						String header=str[str.length-1];
						System.out.println(rawFileName);
						
						File rawFile=new File(((ClinicalData)this.dataType).getPath()+File.separator+rawFileName);
						try {
							String dataLabel=this.labels.get(rawFile.getName()+"-"+header);
							if(dataLabel==null){
								dataLabel=header;
							}
							int columnNumber=FileHandler.getHeaderNumber(rawFile, header);
							
							if(child.hasChildren()){	
								out.write(rawFileName+"\t"+path.replace(' ', '_')+"\t"+columnNumber+"\t"+"DATA_LABEL"+"\t\t\n");
							}
							else{
								out.write(rawFileName+"\t"+path.replace(' ', '_')+"\t"+columnNumber+"\t"+dataLabel+"\t\t\n");
							}
						} catch (IOException e) {
							this.setStudyTreeUI.displayMessage("File error: "+e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				}
				else{
					if(path.compareTo("")!=0){
						newPath=path+"+";
					}
					newPath+=child.toString().replace(' ', '_');
				}
				if(child.hasChildren()){
					writeLine(child, out, newPath);
				}
			}
		}
	}
}

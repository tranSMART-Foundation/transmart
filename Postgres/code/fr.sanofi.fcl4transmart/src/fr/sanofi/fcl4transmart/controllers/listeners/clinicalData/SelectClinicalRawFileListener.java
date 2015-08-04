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
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import au.com.bytecode.opencsv.CSVReader;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.Utils;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData.SelectRawFilesUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class controls a clinical raw data file selection
 */	
public class SelectClinicalRawFileListener implements Listener{
	private SelectRawFilesUI selectRawFilesUI;
	private DataTypeItf dataType;
	private HashMap<String, BufferedWriter> outs;
	private Vector<String> filters;
	public SelectClinicalRawFileListener(SelectRawFilesUI selectRawFilesUI, DataTypeItf dataType){
		this.selectRawFilesUI=selectRawFilesUI;
		this.dataType=dataType;
	}
	@Override
	public void handleEvent(Event event) {
		this.selectRawFilesUI.openLoadingShell();
		new Thread(){
			public void run() {
				String[] paths=selectRawFilesUI.getPath().split("\\?", -1);
				for(int i=0; i<paths.length; i++){
					String path=paths[i];
					if(path==null){
						selectRawFilesUI.setIsLoading(false);
						return;
					}
					File rawFile=new File(path);
					if(rawFile.exists()){
						if(rawFile.isFile()){
							if(path.contains("%")){
								selectRawFilesUI.setMessage("File name can not contain percent ('%') symbol.");
								selectRawFilesUI.setIsLoading(false);
								return;
							}
							Pattern patternTxt=Pattern.compile(".*\\.txt");
							Pattern patternSoft=Pattern.compile(".*\\.soft");
							Pattern patternCsv=Pattern.compile(".*\\.soft");
									
							String newPath=dataType.getPath().getAbsolutePath()+File.separator+rawFile.getName();
							if(selectRawFilesUI.getFormat().compareTo("Tab delimited raw file")!=0 && selectRawFilesUI.getFormat().compareTo("SOFT")!=0 && selectRawFilesUI.getFormat().compareTo("Tab delimited raw file with filter")!=0 && selectRawFilesUI.getFormat().compareTo("CSV")!=0){
								selectRawFilesUI.setMessage("File format does not exist");
								selectRawFilesUI.setIsLoading(false);
								return;
							}
							if(selectRawFilesUI.getFormat().compareTo("SOFT")==0){
								Matcher matcherSoft=patternSoft.matcher(rawFile.getName());
								if(!matcherSoft.matches()){
									selectRawFilesUI.setMessage("File extension must be '.soft'");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
								newPath=newPath.replace(".soft", ".txt");
								File newFile=new File(newPath);
								if(newFile.exists()){
									selectRawFilesUI.setMessage("File has already been added");
									selectRawFilesUI.setIsLoading(false);
									return;
								}else{
									if(createTabFileFromSoft(rawFile, newFile)){
										((ClinicalData)dataType).addRawFile(newFile);
										selectRawFilesUI.setMessage("File has been added");
									}
								}
							}
							else if(selectRawFilesUI.getFormat().compareTo("Tab delimited raw file")==0){
								Matcher matcherTxt=patternTxt.matcher(rawFile.getName());
								if(!matcherTxt.matches()){
									selectRawFilesUI.setMessage("File extension must be '.txt'");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
								if(!checkTabFormat(rawFile)){
									selectRawFilesUI.setIsLoading(false);
									return;
								}
	
								File copiedRawFile=new File(newPath);
								if(!copiedRawFile.exists()){
									try {
										Utils.copyFile(rawFile, copiedRawFile);
										((ClinicalData)dataType).addRawFile(copiedRawFile);
										selectRawFilesUI.setMessage("File has been added");
									} catch (IOException e) {
										e.printStackTrace();
										selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
										selectRawFilesUI.setIsLoading(false);
										try{
											copiedRawFile.delete();
										}catch(Exception e2){
											return;
										}
										return;
									}
								}
								else{
									selectRawFilesUI.setMessage("File has already been added");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
                                                        }
							else if(selectRawFilesUI.getFormat().compareTo("Tab delimited raw file with filter")==0){
								Matcher matcherTxt=patternTxt.matcher(rawFile.getName());
								if(!matcherTxt.matches()){
									selectRawFilesUI.setMessage("File extension must be '.txt'");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
				         	   	filters=selectRawFilesUI.getFilters();
								if(filters==null || filters.size()<1){
									selectRawFilesUI.setMessage("No selected filter");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
								if(!createFilteredFiles(rawFile, dataType.getPath().getAbsolutePath(), filters)){
									return;
								}
								for(String s: outs.keySet()){
									((ClinicalData)dataType).addRawFile(new File(dataType.getPath().getAbsolutePath()+File.separator+s));
								}
								selectRawFilesUI.setMessage("Files have been added");
							}
							else if(selectRawFilesUI.getFormat().compareTo("CSV")==0){
								Matcher matcherCsv=patternCsv.matcher(rawFile.getName());
								if(!matcherCsv.matches()){
									selectRawFilesUI.setMessage("File extension must be '.csv'");
									selectRawFilesUI.setIsLoading(false);
									return;
								}
								newPath=newPath.replace(".csv", ".txt");
								File newFile=new File(newPath);
								if(newFile.exists()){
									selectRawFilesUI.setMessage("File has already been added");
									selectRawFilesUI.setIsLoading(false);
									return;
								}else{
									if(createTabFileFromCsv(rawFile, newFile)){
										((ClinicalData)dataType).addRawFile(newFile);
										selectRawFilesUI.setMessage("File has been added");
									}
								}
							}
						}
						else{
							selectRawFilesUI.setMessage("File is a directory");
							selectRawFilesUI.setIsLoading(false);
							return;
						}
					}
					else{
						selectRawFilesUI.setMessage("Path does no exist");
						selectRawFilesUI.setIsLoading(false);
						return;
					}
				}
				selectRawFilesUI.setIsLoading(false);
			}
		}.start();
		this.selectRawFilesUI.waitForThread();
		selectRawFilesUI.updateViewer();
		WorkPart.updateSteps();
		UsedFilesPart.sendFilesChanged(dataType);
	}
	/**
	 *Checks the format of a tab delimited raw data file
	 */	
	public boolean checkTabFormat(File rawFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			int columnsNbr=line.split("\t", -1).length;
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					if(line.split("\t", -1).length!=columnsNbr){
						selectRawFilesUI.setMessage("Wrong file format:\nLines have no the same number of columns");
						selectRawFilesUI.setIsLoading(false);
						br.close();
						return false;
					}
				}
			}
			br.close();
		}catch (Exception e){
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 *Creates a tab delimited file from a SOFT file
	 */	
	public boolean createTabFileFromSoft(File rawFile, File newFile){
		Vector<String> columns=new Vector<String>();
		Vector<HashMap<String, String>> lines=new Vector<HashMap<String, String>>();	
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line;
			Pattern p1=Pattern.compile(".SAMPLE = .*");
			Pattern p2=Pattern.compile("!Sample_characteristics_ch. = .*: .*");
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					Matcher m1=p1.matcher(line);
					Matcher m2=p2.matcher(line);
					if(m1.matches()){
						lines.add(new HashMap<String, String>());
						if(!columns.contains("sample")){
							columns.add("sample");
						}
						lines.get(lines.size()-1).put("sample", line.split(".SAMPLE = ", -1)[1]);
					}
					else if(m2.matches()){
						String s=line.split("!Sample_characteristics_ch. = ", -1)[1];
						String tag=s.split(": ", -1)[0];
						if(!columns.contains(tag)){
							columns.add(tag);
						}
						lines.get(lines.size()-1).put(tag, s.split(": ", -1)[1]);
					}
				}
			}
			br.close();
		}catch (Exception e){
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		if(columns.size()<=1){
			selectRawFilesUI.setMessage("Wrong soft format: no characteristics");
			selectRawFilesUI.setIsLoading(false);
			return false;
		}
		FileWriter fw;
		try {
			fw = new FileWriter(newFile);
			BufferedWriter out = new BufferedWriter(fw);
	
			for(int i=0; i<columns.size()-1; i++){
				out.write(columns.get(i)+"\t");
			}
			out.write(columns.get(columns.size()-1)+"\n");
			
			for(HashMap<String, String> sample: lines){
				for(int i=0; i<columns.size()-1; i++){
					String value=sample.get(columns.get(i));
					if(value==null) value="";
					out.write(value+"\t");
				}
				String value=sample.get(columns.get(columns.size()-1));
				if(value==null) value="";
				out.write(value+"\n");
			}
			out.close();
			return true;
		} catch (IOException e) {
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			selectRawFilesUI.setIsLoading(false);
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createFilteredFiles(File rawFile, String newPath, Vector<String> filters){
		this.outs=new HashMap<String, BufferedWriter>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			int columnsNumber=-1;
			String headers="";
			if(line!=null){
				headers=line;
			}
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					String[] columns=line.split("\t", -1);
					if(columnsNumber!=-1){
						if(columns.length!=columnsNumber){
							selectRawFilesUI.setMessage("Wrong file format:\nLines have no the same number of columns");
							selectRawFilesUI.setIsLoading(false);
							br.close();
							return false;
						}
					}else{
						columnsNumber=columns.length;
					}
					String fileName=rawFile.getName();
					for(String s: filters){
						int n=FileHandler.getHeaderNumber(rawFile, s);
						if(n!=-1){
							fileName+="_"+s.replace("\\", "_").replace("/", "_").replace("%", "Pct")+"."+columns[n-1].replace("\\", "_").replace("/", "_").replace("%", "Pct").replace(": ", "_").replace("<", "_").replace(">", "_").replace("|", "_").replace("*", "_").replace("?", "_").replace("\"", "_");
						}
					}
					BufferedWriter out=this.outs.get(fileName);
					if(out==null){
						FileWriter fw=new FileWriter(newPath+File.separator+fileName);
						out = new BufferedWriter(fw);
						out.write(headers+"\n");
						this.outs.put(fileName, out);
					}
					out.write(line+"\n");
				}
			}
			br.close();
			for(String k:this.outs.keySet()){
				this.outs.get(k).close();
			}
		}catch (Exception e){
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}
	public boolean createTabFileFromCsv(File rawFile, File newFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line;
			FileWriter fw=new FileWriter(rawFile.getAbsolutePath()+".tmp");
			BufferedWriter out1 = new BufferedWriter(fw);
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0){
					out1.write(line.replaceAll("\\\\", "")+"\n");
				}
			}
			br.close();
			out1.close();
		}catch (Exception e){
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		CSVReader reader;
		BufferedWriter out;
		try{
			FileWriter fw= new FileWriter(newFile);
			out = new BufferedWriter(fw);
			
			int columnsNumber=-1;
			reader = new CSVReader(new FileReader(rawFile.getAbsolutePath()+".tmp"));
			String [] columns;
			while ((columns = reader.readNext()) != null) {
				if(columnsNumber!=-1){
					if(columns.length!=columnsNumber){
						selectRawFilesUI.setMessage("Wrong file format:\nLines have no the same number of columns");
						selectRawFilesUI.setIsLoading(false);
						out.close();
						newFile.delete();
						return false;
					}
				}else{
					columnsNumber=columns.length;
				}
				String s="";
				for(int i=0; i<columns.length-1; i++){
					s+=columns[i]+"\t";
				}
				s+=columns[columns.length-1];
				out.write(s+"\n");
			}
			out.close();
			reader.close();
		}catch (Exception e){
			selectRawFilesUI.setMessage("File error: "+e.getLocalizedMessage());
			selectRawFilesUI.setIsLoading(false);
			e.printStackTrace();
			return false;
		}
		return true;	
	}
}

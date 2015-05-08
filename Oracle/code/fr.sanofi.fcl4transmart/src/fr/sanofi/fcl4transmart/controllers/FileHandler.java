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
package fr.sanofi.fcl4transmart.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
/**
 * This class offers methods to parse files (raw data and mapping files)
 */
public class FileHandler {
	private static String[] reserved_word={"SUBJ_ID", "OMIT", "VISIT_NAME", "VISIT_NAME_2", "SITE_ID", "VISIT_DATE", "ENROLL_DATE"};
	private static boolean isReserved(String s){
		for(int i=0; i<FileHandler.reserved_word.length; i++){
			if(FileHandler.reserved_word[i].compareTo(s)==0){
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns a vector with all headers of a clinical raw data file
	 */
	public static Vector<String> getHeaders(File file){
		Vector<String> headers=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			for(int i=0; i<s.length; i++){
				headers.add(s[i]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return headers;
	}
	/**
	 * Returns the column number of a given header for a clinical data file
	 */
	public static int getHeaderNumber(File file, String string){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			for(int i=0; i<s.length; i++){
				if(s[i].compareTo(string)==0){
					br.close();
					return i+1;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return -1;	
	}
	/**
	 * Returns the count of columns for a raw clinical data file
	 */
	public static int getColumnsNumber(File file){
		int n=-1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			n=s.length;
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return n;
	}

	/**
	 *Reads a column mapping file, and returns the column number for the line with a given label, for a given raw clinical data file 
	 */
	public static int getNumberForLabel(File file, String string, File rawFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo(string)==0 && s[0].compareTo(rawFile.getName())==0){//data label: third column
					try{
						br.close();
						return Integer.parseInt(s[2]);
					}catch(NumberFormatException nfe){
						br.close();
						return -1;
					}
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 *Reads a column mapping file, and returns the raw data file name for athe line with a given label
	 */
	public static String getRawForLabel(File file, String string){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo(string)==0){//data label: third column
					br.close();
					return s[0];
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}

	/**
	 *Returns the header for the n-th column of data file 
	 */
	public static String getColumnByNumber(File file, int n){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			br.close();
			return s[n-1];			
		}catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}
	/**
	 *Returns a vector of string containing all headers of clinical raw data file that will be integrated in databases (non omited and not identifiers in cmf) 
	 */	
	public static Vector<String> getHeadersFromCmf(File cmf, File rawFile){
		String[] rawHeaders=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			rawHeaders=line.split("\t", -1);
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		Vector<String>headers=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0){
					if(!FileHandler.isReserved(s[3]) && s[3].compareTo("\\")!=0 && s[3].compareTo("MIN")!=0 && s[3].compareTo("MAX")!=0 && s[3].compareTo("MEAN")!=0 && s[3].compareTo("UNITS")!=0){
						if(!headers.contains(rawHeaders[Integer.parseInt(s[2])-1])){
							headers.add(rawHeaders[Integer.parseInt(s[2])-1]);
						}
					}
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return headers;
	}
	
	/**
	 *Returns a vector of string containing all headers of raw data that are not omitted
	 */	
	public static Vector<String> getNonOmittedHeaders(File cmf, File rawFile){
		String[] rawHeaders=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			rawHeaders=line.split("\t", -1);
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		
		Vector<String>headers=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0){
					if(s[3].compareTo("OMIT")!=0){
						if(!headers.contains(rawHeaders[Integer.parseInt(s[2])-1])){
							headers.add(rawHeaders[Integer.parseInt(s[2])-1]);
						}
					}
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return headers;
	}
	
	/**
	 *Returns the data label from a header of raw clinical data
	 */	
	public static String getDataLabel(File cmf, File rawFile, String header){
		String[] rawHeaders=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			rawHeaders=line.split("\t", -1);
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
		int columnNumber=-1;
		for(int i=0; i<rawHeaders.length; i++){
			if(rawHeaders[i].compareTo(header)==0) columnNumber=i;
		}
		if(columnNumber!=-1){
			try{
				BufferedReader br = new BufferedReader(new FileReader(cmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					if(s[0].compareTo(rawFile.getName())==0 && (Integer.parseInt(s[2])-1)==columnNumber){
						if(s[3].compareTo("MIN")!=0 && s[3].compareTo("MAX")!=0 && s[3].compareTo("MEAN")!=0 && s[3].compareTo("UNITS")!=0){
							br.close();
							return s[3];
						}
					}
				}
				
				br.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 *Returns the controlled vocabulary code for a given header of a raw clinical data file
	 */	
	public static String getCodeFromHeader(File cmf, File rawFile, String header){
		String[] rawHeaders=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			rawHeaders=line.split("\t", -1);
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
		int columnNumber=-1;
		for(int i=0; i<rawHeaders.length; i++){
			if(rawHeaders[i].compareTo(header)==0) columnNumber=i;
		}
		if(columnNumber!=-1){
			try{
				BufferedReader br = new BufferedReader(new FileReader(cmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					if((Integer.parseInt(s[2])-1)==columnNumber){
						br.close();
						return s[5];
					}
				}
				
				br.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 *Checks if a new data value exists for a given old data value, for one header of a clinical raw data file
	 */	
	public static String getNewDataValue(File wmf, File rawFile, String header, String oldData){
		if(wmf==null) return null;
		try{
			int columnNumber=FileHandler.getHeaderNumber(rawFile, header);
			BufferedReader br = new BufferedReader(new FileReader(wmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0 && Integer.parseInt(s[1])==columnNumber && s[2].compareTo(oldData)==0){
					br.close();
					return s[3];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 *Checks if a new data value exists for a given old data value, for a given columnNumber of a clinical raw data file
	 */	
	public static String getNewDataValue(File wmf, File rawFile, int columnNumber, String oldData){
		if(wmf==null) return null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(wmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0 && Integer.parseInt(s[1])==columnNumber && s[2].compareTo(oldData)==0){
					br.close();
					return s[3];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 *Returns a vector with all terms of a clinical raw data file for a given header
	 */	
	public static Vector<String> getTerms(File rawFile, String header){
		Vector<String> terms=new Vector<String>();
		try{
			int columnNumber=FileHandler.getHeaderNumber(rawFile, header);
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(!terms.contains(s[columnNumber-1])){
					terms.add(s[columnNumber-1]);
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return terms;
	}
	/**
	 *Returns a vector with all terms of a clinical raw data file for a given column number
	 */	
	public static Vector<String> getTermsByNumber(File rawFile, int columnNumber){
		Vector<String> terms=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(columnNumber!=-1 && s.length>=columnNumber && !terms.contains(s[columnNumber-1])){
					terms.add(s[columnNumber-1]);
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return terms;
	}
	/**
	 *Returns a vector of data labels from a column mapping file
	 */	
	public static Vector<String> getDataLabels(File cmf, Vector<File> rawFiles){
		Vector<String> dataLabels=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();

			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(!FileHandler.isReserved(s[3])){
					if(s[3].compareTo("")!=0){
						dataLabels.add(s[3]);
					}
					else{
						File rawFile=null;
						for(File f: rawFiles){
							if(f.getName().compareTo(s[0])==0){
								rawFile=f;
							}
						}
						dataLabels.add(FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[2])));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return dataLabels;
	}
	
	public static Vector<String> getDataLabelsForQC(File cmf, File rawFile){
		Vector<String> dataLabels=new Vector<String>();
		HashMap<String, String> dataLabelSources=new HashMap<String, String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo("DATA_LABEL")==0){
					dataLabelSources.put(s[2], s[1]);
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return dataLabels;
		}
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0){
					if(!FileHandler.isReserved(s[3]) && s[3].compareTo("UNITS")!=0){
						if(s[3].compareTo("DATA_LABEL")==0);
						else if(s[3].compareTo("\\")==0){
							if(dataLabelSources.get(s[4]).compareTo("")!=0){
								dataLabels.add(dataLabelSources.get(s[4].substring(0,1)).replace("_", " ").replace("+", "\\")+"\\"+"DATA_LABEL_SOURCE["+s[4].substring(0,1)+"]");	
							}else{
								dataLabels.add(s[3]);
							}
						}
						else if(s[3].compareTo("")!=0){
							if(s[1].compareTo("")!=0){
								dataLabels.add(s[1].replace("_", " ").replace("+", "\\")+"\\"+s[3]);	
							}else{
								dataLabels.add(s[3]);
							}
						}
						else{
							String label=FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[2]));
							if(s[1].compareTo("")!=0){
								dataLabels.add(s[1].replace("_", " ").replace("+", "\\")+"\\"+label);	
							}else{
								dataLabels.add(label);
							}
						}
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return dataLabels;
	}
		
	/**
	 *Returns a value for a given subject and a given data label
	 */	
	public static String getValueForSubject(File cmf, Vector<File> rawFiles, String subjectId, String dataLabel, File wmf){
		int columnNumber=-1;
		File rawFile=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo(dataLabel)==0){
					columnNumber=Integer.parseInt(s[2]);
					for(File file: rawFiles){
						if(file.getName().compareTo(s[0])==0){
							rawFile=file;
						}
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
		for(File f: rawFiles){
			if(columnNumber==-1){
				columnNumber=FileHandler.getHeaderNumber(f, dataLabel);
				rawFile=f;
			}
		}
		if(rawFile!=null){
			int subjIdNumber=FileHandler.getNumberForLabel(cmf, "SUBJ_ID", rawFile);
			try{
				BufferedReader br = new BufferedReader(new FileReader(rawFile));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					if(s[subjIdNumber-1].compareTo(subjectId)==0){
						if(wmf==null){
							br.close();
							return s[columnNumber-1];
						}
						else{
							try{
								BufferedReader br2 = new BufferedReader(new FileReader(wmf));
								String line2=br2.readLine();
								while ((line2=br2.readLine())!=null){
									String[] s2=line2.split("\t", -1);
									if(s2[0].compareTo(rawFile.getName())==0 && s2[1].compareTo(String.valueOf(columnNumber))==0 && s2[2].compareTo(s[columnNumber-1])==0){
										br.close();
										br2.close();
										return  s2[3];
									}
								}
								br2.close();
								return s[columnNumber-1];
							}catch (Exception e){
								e.printStackTrace();
								br.close();
								return "";
							}
						}
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return "";
			}
		}
		return "";
	}
	
	/**
	 *Returns the last element of the category code for a given column number and clinical raw file name
	 */	
	public static String getValueForSubjectByColumn(File cmf, Vector<File> rawFiles, String subjectId, String columnNumber, String rawFileName, File wmf){
		File rawFile=null;
		for(File file: rawFiles){
			if(file.getName().compareTo(rawFileName)==0){
				rawFile=file;
			}
		}
		if(rawFile==null) return null;
		int column=-1;
		try{
			column=Integer.parseInt(columnNumber);
		}
		catch(NumberFormatException e){
			return null;
		}
		String value=null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(subjectId)==0){
					value=s[column-1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		if(value==null) return null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(wmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFileName)==0 && s[1].compareTo(columnNumber)==0 && s[2].compareTo(value)==0){
					value=s[3];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 *Returns a hashmap for a given subject with a data label and its value in files
	 */	
	public static HashMap<String, Vector<String>> getValueForSubjectForQC(File cmf, File rawFile, String subjectId, File wmf){
		HashMap<String, Vector<String>> values=new HashMap<String, Vector<String>>();
		HashMap<String, String> dataLabelSources=new HashMap<String, String>();
		int visitColumn=-1;
		//serach in mapping file if there are visit names or data label sources in the files
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0){
					if(s[3].compareTo("DATA_LABEL")==0){
						dataLabelSources.put(s[2], s[1]);
					}
					else if(s[3].compareTo("VISIT_NAME")==0){
						visitColumn=Integer.parseInt(s[2]);
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return values;
		}

		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			int subjIdNumber=-1;
			boolean severalVisits=false;
			if(rawFile!=null){
				subjIdNumber=FileHandler.getNumberForLabel(cmf, "SUBJ_ID", rawFile);
				severalVisits=FileHandler.getSeveralVisit(rawFile, visitColumn);
			}
			while ((line=br.readLine())!=null){
				String dataLabel="";
				int columnNumber=-1;
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0 && !FileHandler.isReserved(s[3]) && s[3].compareTo("UNITS")!=0 && s[3].compareTo("DATA_LABEL")!=0){
					//if there is a data label sources, it is required to go through the raw file a first time to find the value of the data label source
					if(s[3].compareTo("\\")==0){
						if(rawFile!=null){
							dataLabel=getDataLabelFromSource(subjIdNumber,subjectId, rawFile, wmf, dataLabelSources,Integer.parseInt(s[4].substring(0,1)));
						}
					}
					//if there is a data label
					else if(s[3].compareTo("")!=0){
						if(s[1].compareTo("")!=0){
							dataLabel=s[1].replace("_", " ").replace("+", "\\")+"\\"+s[3];	
						}else{
							dataLabel=s[3];
						}
					}
					//if no data label, find the header of the column
					else{
						String label=FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[2]));
						if(s[1].compareTo("")!=0){
							dataLabel=s[1].replace("_", " ").replace("+", "\\")+"\\"+label;	
						}else{
							dataLabel=label;
						}
					}
					columnNumber=Integer.parseInt(s[2]);
					if(rawFile!=null){
						try{
							BufferedReader br2 = new BufferedReader(new FileReader(rawFile));
							String line2=br2.readLine();
							while ((line2=br2.readLine())!=null){
								String[] s2=line2.split("\t", -1);
								if(s2[subjIdNumber-1].compareTo(subjectId)==0){
									if(wmf==null){
										if(visitColumn==-1 || !severalVisits){
											Vector<String> v=new Vector<String>();
											if(values.get(dataLabel)!=null) v=values.get(dataLabel);
											else values.put(dataLabel, v);
											v.add(s2[columnNumber-1]);
										}
										else{
											Vector<String> v=new Vector<String>();
											if(values.get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=values.get(dataLabel+"\\"+s2[visitColumn-1]);
											else values.put(dataLabel+"\\"+s2[visitColumn-1], v);
											v.add(s2[columnNumber-1]);
										}
									}
									else{
										try{
											BufferedReader br3 = new BufferedReader(new FileReader(wmf));
											String line3=br3.readLine();
											boolean found=false;
											while ((line3=br3.readLine())!=null){
												String[] s3=line3.split("\t", -1);
												if(s3[0].compareTo(rawFile.getName())==0 && s3[1].compareTo(String.valueOf(columnNumber))==0 && s3[2].compareTo(s2[columnNumber-1])==0){
													if(visitColumn==-1 || !severalVisits){
														Vector<String> v=new Vector<String>();
														if(values.get(dataLabel)!=null) v=values.get(dataLabel);
														else values.put(dataLabel, v);
														v.add(s3[3]);
													}
													else{
														Vector<String> v=new Vector<String>();
														if(values.get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=values.get(dataLabel+"\\"+s2[visitColumn-1]);
														else values.put(dataLabel+"\\"+s2[visitColumn-1], v);
														v.add(s3[3]);
													}
													found=true;
												}
											}
											if(!found){
												if(visitColumn==-1 || !severalVisits){
													Vector<String> v=new Vector<String>();
													if(values.get(dataLabel)!=null) v=values.get(dataLabel);
													else values.put(dataLabel, v);
													v.add(s2[columnNumber-1]);
												}
												else{
													Vector<String> v=new Vector<String>();
													if(values.get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=values.get(dataLabel+"\\"+s2[visitColumn-1]);
													else values.put(dataLabel+"\\"+s2[visitColumn-1], v);
													v.add(s2[columnNumber-1]);
												}
											}
											br3.close();
										}catch (Exception e){
											e.printStackTrace();
											br.close();
											br2.close();
											return values;
										}
									}
								}
							}
							br2.close();
						}catch (Exception e){
							e.printStackTrace();
							br.close();
							return values;
						}
					}
				}
			}
			br.close();
			}catch (Exception e){
				e.printStackTrace();
				return values;
			}
		return values;
	}
	
	private static String getDataLabelFromSource(int subjIdNumber, String subjectId, File rawFile, File wmf, HashMap<String, String> dataLabelSources, int sourceNumber){
		String dataLabel="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[subjIdNumber-1].compareTo(subjectId)==0){
					if(wmf==null){
						if(dataLabelSources.get(String.valueOf(sourceNumber)).compareTo("")!=0){
							dataLabel=dataLabelSources.get(String.valueOf(sourceNumber)).replace("+", "\\")+"\\"+s[sourceNumber-1];
						}else{
							dataLabel=s[sourceNumber-1];
						}
					}
					else{
						try{
							BufferedReader br2 = new BufferedReader(new FileReader(wmf));
							String line2=br2.readLine();
							boolean found=false;
							while ((line2=br2.readLine())!=null){
								String[] s2=line2.split("\t", -1);
								if(s2[0].compareTo(rawFile.getName())==0 && s2[1].compareTo(String.valueOf(sourceNumber-1))==0 && s2[2].compareTo(s[sourceNumber-1])==0){
									found=true;
									if(dataLabelSources.get(String.valueOf(sourceNumber)).compareTo("")!=0){
										dataLabel=dataLabelSources.get(String.valueOf(sourceNumber))+"\\"+s2[3];
									}else{
										 dataLabel=s2[3];
									}
								}
							}
							br2.close();
							if(!found){
								if(dataLabelSources.get(String.valueOf(sourceNumber)).compareTo("")!=0){
									dataLabel=dataLabelSources.get(String.valueOf(sourceNumber))+"\\"+s[sourceNumber-1];
									dataLabel=dataLabel.replace("_", " ").replace("+", "\\");	
								}else{
									dataLabel=s[sourceNumber-1];
								}
							}
						}catch (Exception e){
							e.printStackTrace();
							br.close();
							return dataLabel;
						}
					}
					break;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return dataLabel;
		}
		return dataLabel;
	}
	
	
	/**
	 *Returns a vector of sample identifiers from gene expression raw data file
	 */	
	public static Vector<String> getSamplesId(File geneFile){
		Vector<String> samples=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(geneFile));
			String line=br.readLine();
			String[] s=line.split("\t", -1);
			for(int i=1; i<s.length; i++){
				samples.add(s[i]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		return samples;
	}
	
	/**
	 *Returns a vector of sample identifiers from SNP data file
	 */	
	public static Vector<String> getSamplesIdForSnp(File file){
		Vector<String> samples=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("!")!=0){
					String[] s=line.split("\t", -1);
					for(int i=1; i<s.length; i++){
						samples.add(s[i].replace("\"", ""));
					}
					break;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		return samples;
	}
	
	/**
	 *Returns a vector of headers of the SNP annotation file
	 */	
	public static Vector<String> getHeadersSnpAnnotation(File annotFile){
		Vector<String> headers=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotFile));
			String line;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					String[] s=line.split("\t", -1);
					for(int i=0; i<s.length; i++){
						headers.add(s[i].replace("\"", ""));
					}
					break;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return headers;
		}
		return headers;
	}
	
	/**
	 *Checks that a subject to sample mapping file contains subject identifiers
	 */	
	public static boolean checkSubjId(File stsmf){
		try{
			BufferedReader br = new BufferedReader(new FileReader(stsmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[2].compareTo("")==0){
					br.close();
					return false;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 *Checks that a subject to sample mapping file contains platform
	 */	
	public static boolean checkPlatform(File stsmf){
		try{
			BufferedReader br = new BufferedReader(new FileReader(stsmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[4].compareTo("")==0){
					br.close();
					return false;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 *Checks that a subject to sample mapping file contains category codes
	 */	
	public static boolean checkCategoryCodes(File stsmf){
		try{
			BufferedReader br = new BufferedReader(new FileReader(stsmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[8].compareTo("")==0){
					br.close();
					return false;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 *Returns the probe identifier from a gene raw data file
	 */	
	public static Vector<String> getProbes(File rawFile){
		Vector<String> probes=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				probes.add(s[0]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return probes;
	}
	

	/**
	 *Returns the probe identifier from a gene raw data file
	 */	
	public static Vector<String> getTranscripts(File rawFile){
		Vector<String> probes=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				probes.add(s[0]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return probes;
	}

	/**
	 *Returns the antigen identifier from a RBM raw data file
	 */	
	public static Vector<String> getAntigens(File rawFile){
		Vector<String> probes=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				probes.add(getAntigenWithoutUnit(s[5]));
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return probes;
	}
	public static String getAntigenWithoutUnit(String antigenWithValue){
		String antigen=antigenWithValue.replaceAll("\\(.*\\)", "");
		antigen=antigen.trim();
		return antigen;		
	}

	/**
	 *Returns the intensity value for a given sample and a given probe in a gene raw data file
	 */	
	public static Double getIntensityBySample(File rawFile, String sample, String probe){
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			String[] samples=line.split("\t", -1);
			int columnNumber=-1;
			for(int i=1; i<samples.length; i++){
				if(samples[i].compareTo(sample)==0){
					columnNumber=i;
				}
			}
			if(columnNumber==-1) {
				br.close();
				return null;
			}
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(probe)==0){
					br.close();
					return Double.valueOf(s[columnNumber]);
				}
			}
			br.close();
			return null;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}	
	}
	/**
	 *Returns a hashmap with sample and intensity value for a given probe in a gene raw data file. Used for gene expression QC.
	 */	
	public static HashMap<String, Double> getIntensity(File rawFile, String probe){
		HashMap<String, Double> intensities=new HashMap<String, Double>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			String[] samples=line.split("\t", -1);
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(probe)==0){
					for(int i=1; i<samples.length; i++){
						if(s[i].compareTo("")!=0) intensities.put(samples[i], Double.valueOf(s[i]));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return intensities;
	}
	/**
	 *Returns a vector of vectors with for each unit line, the file name, the column number and the data label source
	 *Since version 1.2
	 */	
	public static Vector<Vector<String>> getUnitsLines(File cmf){
		Vector<Vector<String>> v=new Vector<Vector<String>>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo("UNITS")==0){
					Vector<String> vectorLine=new Vector<String>();
					vectorLine.add(s[0]);
					vectorLine.add(s[2]);
					vectorLine.add(s[4]);
					v.add(vectorLine);
				}
			}
			br.close();
			return v;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}	
	}
	/**
	 *Checks that there are properties in the study tree for clinical data
	 *Since version 1.2
	 */	
	static public boolean checkTreeSet(File cmf){
		boolean bool=false;
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[1].compareTo("")!=0){
					bool=true;
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return bool;
	}
	/**
	 *Checks that all terms of a column (replaced if there is a word mapping file) are numerical
	 *Since version 1.2
	 */	
	static public boolean isColumnNumerical(File rawFile, File wmf, int columnNumber){
		Vector<String> terms=FileHandler.getTermsByNumber(rawFile, columnNumber);
		for(String term: terms){
			String newTerm=FileHandler.getNewDataValue(wmf, rawFile, columnNumber, term);
			if(newTerm==null) newTerm=term;
			try{
				if(newTerm.compareTo(".")!=0 && newTerm.compareTo("")!=0){
					Double.parseDouble(newTerm);
				}
			}
			catch(NumberFormatException e){
				return false;
			}
		}
		return true;
	}
	/**
	 *Return all headers from word mapping files under the form <file name> - <header>, in a vector of string
	 */	
	static public Vector<String> getHeadersFromWmf(Vector<File> rawFiles, File wmf){
		Vector<String> headers=new Vector<String>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(wmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				String rawName=s[0];
				int columnNumber=Integer.valueOf(s[1]);
				File file=null;
				for(File rawFile: rawFiles){
					if(rawFile.getName().compareTo(rawName)==0){
						file=rawFile;
						break;
					}
				}
				if(file!=null){
					String header=FileHandler.getColumnByNumber(file, columnNumber);
					headers.add(rawName+" - "+header);
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}	

		return headers;
	}
	public static boolean hasVisitName(File rawFile, File cmf){
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[0].compareTo(rawFile.getName())==0 && s[3].compareTo("VISIT_NAME")==0){
					br.close();
					return true;
				}
			}
			br.close();
			return false;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}	
	}
	public static boolean getSeveralVisit(File rawFile, int visitColumn){
		if(visitColumn==-1) return false;
		Vector<String> v=new Vector<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				boolean found=false;
				for(String existing:v){
					if(existing.compareTo(s[visitColumn-1])==0){
						found=true;
						break;
					}
				}
				if(!found) v.add(s[visitColumn-1]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}	
		return v.size()>1;
	}
	public static boolean containsString(Set<String> v, String s){
		for(String vs: v){
			if(vs.compareTo(s)==0){
				return true;
			}
		}
		return false;
	}
	/**
	 *Return a hashmap with for each subject a hashmap with all values
	 */	
	public static HashMap<String, HashMap<String, Vector<String>>> getValueForQC(File cmf, Vector<File> rawFiles, File wmf){
		HashMap<String, HashMap<String, Vector<String>>> valuesBySubject=new HashMap<String, HashMap<String, Vector<String>>>();
		for(File rawFile :rawFiles){
			int columnNumber=FileHandler.getNumberForLabel(cmf, "SUBJ_ID", rawFile);
			for(String s:FileHandler.getTermsByNumber(rawFile, columnNumber)){
				if(!valuesBySubject.keySet().contains(s)){
					valuesBySubject.put(s, new HashMap<String, Vector<String>>());
				}
			}
		}
		for(File rawFile: rawFiles){
			HashMap<String, String> dataLabelSources=new HashMap<String, String>();

			int visitColumn=-1;
			try{
				BufferedReader br = new BufferedReader(new FileReader(cmf));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					if(s[0].compareTo(rawFile.getName())==0){
						if(s[3].compareTo("DATA_LABEL")==0){
							dataLabelSources.put(s[2], s[1]);
						}
						else if(s[3].compareTo("VISIT_NAME")==0){
							visitColumn=Integer.parseInt(s[2]);
						}
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return valuesBySubject;
			}
	
			try{
				BufferedReader br = new BufferedReader(new FileReader(cmf));
				String line=br.readLine();
				int subjIdNumber=-1;
				boolean severalVisits=false;
				if(rawFile!=null){
					subjIdNumber=FileHandler.getNumberForLabel(cmf, "SUBJ_ID", rawFile);
					if(visitColumn!=-1) severalVisits=FileHandler.getSeveralVisit(rawFile, visitColumn);
				}
				while ((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String dataLabel="";
						int columnNumber=-1;
						String[] s=line.split("\t", -1);
						if(s[0].compareTo(rawFile.getName())==0 && !FileHandler.isReserved(s[3]) && s[3].compareTo("UNITS")!=0  && s[3].compareTo("DATA_LABEL")!=0){
							if(s[3].compareTo("\\")==0){//if there is a data label sources, it is required to go through the raw file a first time to find the value of the data label source
								if(rawFile!=null){
									try{
										BufferedReader br2 = new BufferedReader(new FileReader(rawFile));
										String line2=br2.readLine();
										while ((line2=br2.readLine())!=null){
											String[] s2=line2.split("\t", -1);
											if(wmf==null){
												if(dataLabelSources.get(s[4]).compareTo("")!=0){
													dataLabel=dataLabelSources.get(s[4].substring(0,1))+"\\"+s2[Integer.parseInt(s[4].substring(0,1))-1];
												}else{
													dataLabel=s2[Integer.parseInt(s[4].substring(0,1))-1];
												}
											}
											else{
												try{
													BufferedReader br3 = new BufferedReader(new FileReader(wmf));
													String line3=br3.readLine();
													boolean found=false;
													while ((line3=br3.readLine())!=null){
														String[] s3=line3.split("\t", -1);
														if(s3[0].compareTo(rawFile.getName())==0 && s3[1].compareTo(String.valueOf(Integer.parseInt(s[4].substring(0,1))-1))==0 && s3[2].compareTo(s2[Integer.parseInt(s[4].substring(0,1))-1-1])==0){
															found=true;
															if(dataLabelSources.get(s[4].substring(0,1)).compareTo("")!=0){
																dataLabel=dataLabelSources.get(s[4].substring(0,1))+"\\"+s3[3];
															}else{
																 dataLabel=s3[3];
															}
														}
													}
													br3.close();
													if(!found){
														if(dataLabelSources.get(s[4]).compareTo("")!=0){
															dataLabel=dataLabelSources.get(s[4].substring(0,1))+"\\"+s2[Integer.parseInt(s[4].substring(0,1))-1];
															dataLabel=dataLabel.replace("_", " ").replace("+", "\\");
														}else{
															dataLabel=s2[Integer.parseInt(s[4].substring(0,1))-1];
														}
													}
												}catch (Exception e){
													e.printStackTrace();
													br2.close();
													return valuesBySubject;
												}
											}
											break;
										}
										br2.close();
									}catch (Exception e){
										e.printStackTrace();
										br.close();
										return valuesBySubject;
									}
								}
							}
							else if(s[3].compareTo("")!=0){
								if(s[1].compareTo("")!=0){
									dataLabel=s[1].replace("_", " ").replace("+", "\\")+"\\"+s[3];	
		
								}else{
									dataLabel=s[3];
								}
							}
							else{
								String label=FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[2]));
								if(s[1].compareTo("")!=0){
									dataLabel=s[1].replace("_", " ").replace("+", "\\")+"\\"+label;	
								}else{
									dataLabel=label;
								}
							}
							columnNumber=Integer.parseInt(s[2]);
							if(rawFile!=null){
								try{
									BufferedReader br2 = new BufferedReader(new FileReader(rawFile));
									String line2=br2.readLine();
									while ((line2=br2.readLine())!=null){
										String[] s2=line2.split("\t", -1);
										if(wmf==null){
											if(visitColumn==-1 || !severalVisits){
												if(valuesBySubject.get(s2[subjIdNumber-1])!=null){
													Vector<String> v=new Vector<String>();
													if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel)!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel);
													else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel, v);
													v.add(s2[columnNumber-1]);
												}
											}
											else {
												if(valuesBySubject.get(s2[subjIdNumber-1])!=null){
													Vector<String> v=new Vector<String>();
													if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1]);
													else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel+"\\"+s2[visitColumn-1], v);
													v.add(s2[columnNumber-1]);
												}
											}
										}
										else{
											try{
												BufferedReader br3 = new BufferedReader(new FileReader(wmf));
												String line3=br3.readLine();
												boolean found=false;
												while ((line3=br3.readLine())!=null){
													String[] s3=line3.split("\t", -1);
													if(s3[0].compareTo(rawFile.getName())==0 && s3[1].compareTo(String.valueOf(columnNumber))==0 && s3[2].compareTo(s2[columnNumber-1])==0){
														if(visitColumn==-1 || !severalVisits) {
															if(valuesBySubject.get(s2[subjIdNumber-1])!=null){
																Vector<String> v=new Vector<String>();
																if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel)!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel);
																else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel, v);
																v.add(s3[3]);
															}
														}
														else {
															if(valuesBySubject.get(s2[subjIdNumber-1])!=null){
																Vector<String> v=new Vector<String>();
																if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1]);
																else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel+"\\"+s2[visitColumn-1], v);
																v.add(s3[3]);
															}
														}
														found=true;
													}
												}
												if(!found){
													if(visitColumn==-1 || !severalVisits) {
														if(valuesBySubject.get(s2[subjIdNumber-1])!=null){
															Vector<String> v=new Vector<String>();
															if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel)!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel);
															else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel, v);
															v.add(s2[columnNumber-1]);
														}
													}
													else{
														Vector<String> v=new Vector<String>();
														if(valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1])!=null) v=valuesBySubject.get(s2[subjIdNumber-1]).get(dataLabel+"\\"+s2[visitColumn-1]);
														else valuesBySubject.get(s2[subjIdNumber-1]).put(dataLabel+"\\"+s2[visitColumn-1], v);
														v.add(s2[columnNumber-1]);
													}
												}
												br3.close();
											}catch (Exception e){
												e.printStackTrace();
												br.close();
												br2.close();
												
												
												return valuesBySubject;
											}
										}
									}
									br2.close();
								}catch (Exception e){
									e.printStackTrace();
									br.close();
									return valuesBySubject;
								}
							}
						}
					}
				}
				br.close();
				}catch (Exception e){
					e.printStackTrace();
					return valuesBySubject;
				}
		}
		return valuesBySubject;
	}
	/**
	 *Reads a column mapping file, and returns the column number for the line with a given label, for a given raw clinical data file 
	 */
	public static String getDataLabelSource(File cmf, String string, File rawFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(cmf));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(s[3].compareTo(string)==0 && s[0].compareTo(rawFile.getName())==0){//data label: third column
					try{
						br.close();
						return s[4].substring(0,1);
					}catch(NumberFormatException nfe){
						br.close();
						return "";
					}
				}
			}
			
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}
	/**
	 *Returns a hashmap with for each probe a hashmap with sample and intensity value in gene raw data files. Used for gene expression QC.
	 */	
	public static HashMap<String, HashMap<String, Double>> getIntensitiesAllProbes(Vector<File> rawFiles){
		HashMap<String, HashMap<String, Double>> intensities=new HashMap<String, HashMap<String, Double>>();
		try{
			for(File rawFile: rawFiles){
				BufferedReader br = new BufferedReader(new FileReader(rawFile));
				String line=br.readLine();
				String[] samples=line.split("\t", -1);
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					String probe=s[0];
					if(intensities.get(probe)==null) intensities.put(probe, new HashMap<String, Double>());
					for(int i=1; i<samples.length; i++){
						if(s[i].compareTo("")!=0) intensities.get(probe).put(samples[i], Double.valueOf(s[i]));
					}	
				}
				br.close();
			}
		}catch (Exception e){
			e.printStackTrace();
		}	
		return intensities;
	}
	/**
	 *Returns a SNP platform id from annotation.properties file
	 */	
	public static String getSnpGpl(File file){
		String gpl="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("platform=")==0){
					gpl=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return gpl;
	}
	/**
	 *Returns the different tissues from subject to sample mapping file. Takes a boolean as parameters to indicate if there is a header line or not
	 */	
	public static HashSet<String> getTissues(File file, boolean hasHeader){
		HashSet<String> hash=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if(hasHeader){
				line=br.readLine();
			}
			while ((line=br.readLine())!=null){
				hash.add(line.split("\t", -1)[5]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return hash;
	}
	
	/**
	 *Returns the tree paths from the SNP metatables.properties file and subject to sample mapping file
	 */	
	public static HashSet<String> getPaths(File propFile, File mappingFile){
		HashSet<String> hash=new HashSet<String>();
		String snpBaseNode=getSnpBaseNode(propFile);
		String[] splited=snpBaseNode.split("/", -1);
		String node="\\"+splited[1]+"\\";
		for(int i=2; i<splited.length; i++){
			node+=splited[i]+"\\";
			hash.add(node);
		}
		for(String platformId: getPlatforms(mappingFile, false)){
			String nodeP=node+RetrieveData.getPlatformName(platformId)+"\\";
			hash.add(nodeP);
			for(String tissue: getTissues(mappingFile, false)){
				hash.add(nodeP+tissue+"\\");
			}
		}
		return hash;
	}
	/**
	 *Returns the number of observation_fact from the SNP metatables.properties file and subject to sample mapping file
	 */	
	public static int getNumberForObservation(File mappingFile){
		HashSet<String> hash=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line;
			while ((line=br.readLine())!=null){
				hash.add(line.split("\t", -1)[2]+":"+line.split("\t", -1)[4]+line.split("\t", -1)[5]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return hash.size();
	}
	/**
	 *Returns the different samples from the mapping file
	 */	
	public static HashSet<String> getSamples(File mappingFile, boolean hasHeader){
		HashSet<String> hash=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(mappingFile));
			String line;
			while ((line=br.readLine())!=null){
				hash.add(line.split("\t", -1)[3]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return hash;
	}
	/**
	 *Returns the different platforms from subject to sample mapping file. Takes a boolean as parameters to indicate if there is a header line or not
	 */	
	public static HashSet<String> getPlatforms(File file, boolean hasHeader){
		HashSet<String> hash=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if(hasHeader){
				line=br.readLine();
			}
			while ((line=br.readLine())!=null){
				hash.add(line.split("\t", -1)[4]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return hash;
	}
	/**
	 *Returns the snp base node from the SNP metatables.properties file
	 */	
	public static String getSnpBaseNode(File file){
		String node="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("snp_base_node=")==0){
					node=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return node;
	}
	/**
	 *Returns the different subjects sourcesystem_cd from subject to sample mapping file. Takes a boolean as parameters to indicate if there is a header line or not
	 */	
	public static HashSet<String> getSubjects(File file, boolean hasHeader){
		HashSet<String> hash=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if(hasHeader){
				line=br.readLine();
			}
			while ((line=br.readLine())!=null){
				if(line.compareTo("")!=0) hash.add(line.split("\t", -1)[0]+":"+line.split("\t", -1)[2]);
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return hash;
	}
	/**
	 *Returns the number of probe identifiers from SNP data file
	 */	
	public static int getProbeNumberForSnp(File file){
		int n=0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			boolean headerPassed=false;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("!")!=0){
					if(!headerPassed){
						headerPassed=true;
					}else{
						n++;
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		return n;
	}
	/**
	 *Returns the number of chromosomes present in the SNP annotation file
	 */	
	public static int getChrNumberFromAnnotation(File annotFile, File annotProp){
		String head="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotProp));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("chr=")==0){
					head=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		HashSet<String> chrs=new HashSet<String>();
		int column=-1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotFile));
			String line;
			boolean headerPassed=false;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					if(!headerPassed){
						headerPassed=true;
						String[] s=line.split("\t", -1);
						for(int i=0; i<s.length; i++){
							if(s[i].compareTo(head)==0){
								column=i;
								break;
							}
						}
					}else if(column!=-1){
						String[] s=line.split("\t", -1);
						chrs.add(s[column].replace("\"", ""));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		return chrs.size();
	}
	/**
	 *Returns the number of probes present in the SNP annotation file
	 */	
	public static int getProbesNumberFromAnnotation(File annotFile, File annotProp){
		String head="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotProp));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("snp_id=")==0){
					head=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		int column=-1;
		HashSet<String> probes=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotFile));
			String line;
			boolean headerPassed=false;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					if(!headerPassed){
						headerPassed=true;
						String[] s=line.split("\t", -1);
						for(int i=0; i<s.length; i++){
							if(s[i].compareTo(head)==0){
								column=i;
								break;
							}
						}
					}else if(column!=-1){
						String[] s=line.split("\t", -1);
						probes.add(s[column].replace("\"", ""));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		return probes.size();
	}
	/**
	 *Returns the different probes present in the SNP annotation file
	 */	
	public static HashSet<String> getProbesFromAnnotation(File annotFile, File annotProp){
		String head="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotProp));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("snp_id=")==0){
					head=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		int column=-1;
		HashSet<String> probes=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotFile));
			String line;
			boolean headerPassed=false;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					if(!headerPassed){
						headerPassed=true;
						String[] s=line.split("\t", -1);
						for(int i=0; i<s.length; i++){
							if(s[i].compareTo(head)==0){
								column=i;
								break;
							}
						}
					}else if(column!=-1){
						String[] s=line.split("\t", -1);
						probes.add(s[column].replace("\"", ""));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return probes;
		}
		return probes;
	}
	/**
	 *Returns the different SNP names present in the SNP annotation file
	 */	
	public static HashSet<String> getSnpFromAnnotation(File annotFile, File annotProp){
		String head="";
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotProp));
			String line;
			while ((line=br.readLine())!=null){
				if(line.indexOf("rsId=")==0){
					head=line.split("=", 2)[1];
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		int column=-1;
		HashSet<String> probes=new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(annotFile));
			String line;
			boolean headerPassed=false;
			while((line=br.readLine())!=null){
				if(line.compareTo("")!=0 && line.indexOf("#")!=0){
					if(!headerPassed){
						headerPassed=true;
						String[] s=line.split("\t", -1);
						for(int i=0; i<s.length; i++){
							if(s[i].compareTo(head)==0){
								column=i;
								break;
							}
						}
					}else if(column!=-1){
						String[] s=line.split("\t", -1);
						probes.add(s[column].replace("\"", ""));
					}
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			return probes;
		}
		return probes;
	}
	/**
	 *Returns the gene identifiers for a SNP data
	 */	
	public static HashSet<String> getGenesForSnp(DataTypeItf dataType){
		HashSet<String> genes=new HashSet<String>();
		File file=new File(dataType.getPath().getAbsoluteFile()+File.separator+dataType.getStudy().toString()+".genemap");
		if(file.exists() && file.isFile()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						genes.add(s[1].replace("\"", ""));
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return genes;
			}
		}
		return genes;
	}
	/**
	 *Returns the probes names in genemap file for a SNP data
	 */	
	public static HashSet<String> getProbesForGenesForSnp(DataTypeItf dataType){
		HashSet<String> probes=new HashSet<String>();
		File file=new File(dataType.getPath().getAbsoluteFile()+File.separator+dataType.getStudy().toString()+".genemap");
		if(file.exists() && file.isFile()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						probes.add(s[0].replace("\"", ""));
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return probes;
			}
		}
		return probes;
	}
	public static Vector<String> getRbmSamplesId(File file){
		Vector<String> samples=new Vector<String>();
		if(file.exists() && file.isFile()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line=br.readLine();
				while((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						if(!samples.contains(s[2]))samples.add(s[2]);
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return samples;
			}
		}
		return samples;
	}
	
	public static HashMap<String, Double> getRbmValue(File rawFile, String probe){
		HashMap<String, Double> intensities=new HashMap<String, Double>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(rawFile));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				if(getAntigenWithoutUnit(s[5]).compareTo(probe)==0){
					if(s[7].compareTo("")!=0) intensities.put(s[2], Double.valueOf(s[7]));
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}	
		return intensities;
	}
	
	public static HashMap<String, HashMap<String, Double>> getRbmValuesProbes(Vector<File> rawFiles){
		HashMap<String, HashMap<String, Double>> intensities=new HashMap<String, HashMap<String, Double>>();
		try{
			for(File rawFile: rawFiles){
				BufferedReader br = new BufferedReader(new FileReader(rawFile));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					String probe=getAntigenWithoutUnit(s[5]);
					if(intensities.get(probe)==null) intensities.put(probe, new HashMap<String, Double>());
					if(s[7].compareTo("")!=0) intensities.get(probe).put(s[2], Double.valueOf(s[7]));
				}
				br.close();
			}
		}catch (Exception e){
			e.printStackTrace();
		}	
		return intensities;
	}
	//works also for metabolomics
	public static Vector<String> getProteomicsSamplesId(File file, File columnMapping){
		Vector<String> samples=new Vector<String>();
		int peptideId=-1;
		int valueStart=-1;
		int valueEnd=-1;
		if(file.exists() && file.isFile() && columnMapping.exists() && columnMapping.isFile()){
			try{
				BufferedReader br1 = new BufferedReader(new FileReader(columnMapping));
				String line=br1.readLine();
				while((line=br1.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						if(s[0].compareTo(file.getName())==0){
							peptideId=Integer.valueOf(s[1])-1;
							valueStart=Integer.valueOf(s[2]);
							valueEnd=Integer.valueOf(s[3]);
						}
					}
				}
				br1.close();
				if(peptideId==-1 || valueStart==-1 || valueEnd==-1) return null;
				BufferedReader br = new BufferedReader(new FileReader(file));
				if((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					for(int i=valueStart; i<s.length; i++){
						if(!samples.contains(s[i]))samples.add(s[i]);
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return samples;
			}
		}
		return samples;
	}
	//works also for metabolomics
	public static HashMap<String, Double> getProteomicsValue(File file, File columnMapping,  String probe){
		HashMap<String, Double> intensities=new HashMap<String, Double>();
		if(file.exists() && file.isFile() && columnMapping.exists() && columnMapping.isFile()){
			int peptideId=-1;
			int valueStart=-1;
			int valueEnd=-1;
			try{
				BufferedReader br1 = new BufferedReader(new FileReader(columnMapping));
				String line=br1.readLine();
				while((line=br1.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						if(s[0].compareTo(file.getName())==0){
							peptideId=Integer.valueOf(s[1])-1;
							valueStart=Integer.valueOf(s[2]);
							valueEnd=Integer.valueOf(s[3]);
						}
					}
				}
				br1.close();
				if(peptideId==-1 || valueStart==-1 || valueEnd==-1) return null;
					
				BufferedReader br = new BufferedReader(new FileReader(file));
				String[] samples=null;
				if((line=br.readLine())!=null){
					samples=line.split("\t", -1);
				}
				if(samples==null){
					br.close();
					return null;
				}
				boolean found=false;
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					for(int i=valueStart; i<s.length; i++){
						if(s[peptideId].compareTo(probe)==0){
							intensities.put(samples[i], Double.valueOf(s[i]));
							found=true;
						}
					}
					if(found) break;
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
			}	
		}
		return intensities;
	}
	//works also for metabolomics
	public static HashMap<String, HashMap<String, Double>> getProteomicsValuesProbes(Vector<File> rawFiles, File columnMapping){
		HashMap<String, HashMap<String, Double>> intensities=new HashMap<String, HashMap<String, Double>>();
		try{
			for(File file: rawFiles){
				if(file.exists() && file.isFile() && columnMapping.exists() && columnMapping.isFile()){
					int peptideId=-1;
					int valueStart=-1;
					int valueEnd=-1;
					
					BufferedReader br1 = new BufferedReader(new FileReader(columnMapping));
					String line=br1.readLine();
					while((line=br1.readLine())!=null){
						if(line.compareTo("")!=0){
							String[] s=line.split("\t", -1);
							if(s[0].compareTo(file.getName())==0){
								peptideId=Integer.valueOf(s[1])-1;
								valueStart=Integer.valueOf(s[2]);
								valueEnd=Integer.valueOf(s[3]);
							}
						}
					}
					br1.close();
					if(peptideId==-1 || valueStart==-1 || valueEnd==-1) return null;
					
					BufferedReader br = new BufferedReader(new FileReader(file));
					String[] samples=null;
					if((line=br.readLine())!=null){
						samples=line.split("\t", -1);
					}
					if(samples==null){
						br.close();
						return null;
					}
					while ((line=br.readLine())!=null){
						String[] s=line.split("\t", -1);
						for(int i=valueStart; i<s.length; i++){
							String probe=s[peptideId];
							if(intensities.get(probe)==null) intensities.put(probe, new HashMap<String, Double>());
							if(s[i].compareTo("")!=0) intensities.get(probe).put(samples[i], Double.valueOf(s[i]));
						}
					}
					br.close();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}	
		return intensities;
	}
	public static Vector<String> getPeptide(File rawFile, File columnMappingFile){
		Vector<String> probes=new Vector<String>();
		if(rawFile.exists() && rawFile.isFile() && columnMappingFile.exists() && columnMappingFile.isFile()){
			int peptideId=-1;
			try{
				BufferedReader br1 = new BufferedReader(new FileReader(columnMappingFile));
				String line=br1.readLine();
				while((line=br1.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						if(s[0].compareTo(rawFile.getName())==0){
							peptideId=Integer.valueOf(s[1])-1;
	
						}
					}
				}
				br1.close();
				if(peptideId==-1) return null;

				BufferedReader br = new BufferedReader(new FileReader(rawFile));
				line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] s=line.split("\t", -1);
					probes.add(s[peptideId]);
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
			}	
		}
		return probes;
	}
	
	//retrieve category codes (with values replaced) from subject to sample mapping file
	public static Vector<String> getFullCategoryCode(File file){
		Vector<String> categories=new Vector<String>();
		try{
			if(file!=null){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line=br.readLine();
				while((line=br.readLine())!=null){
					if(line.compareTo("")!=0){
						String[] s=line.split("\t", -1);
						String[] c=s[8].split("\\+");
						String cat="";
						for(int i=0; i<c.length; i++){
							if(c[i].compareTo("PLATFORM")==0){
								String gplName=RetrieveData.getGplName(s[4]);
								if(gplName.compareTo("")!=0) cat+=gplName+"+";
								else{
									cat="Platform not found: "+s[4]+" ";
									break;
								}
							}
							else if(c[i].compareTo("TISSUETYPE")==0) cat+=s[5]+"+";
							else if(c[i].compareTo("ATTR1")==0) cat+=s[6]+"+";
							else if(c[i].compareTo("ATTR2")==0) cat+=s[7]+"+";					
							else cat+=c[i]+"+";
						}
						if(cat.length()>0) cat = cat.substring(0, cat.length()-1);
						boolean found=false;
						for(String ca: categories){
							if(ca.compareTo(cat)==0){
								found=true;
								break;
							}
						}
						if(!found) categories.add(cat);
					}
				}
				br.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			return new Vector<String>();
		}
		return categories;
	}
	
	//retrieve category codes (with values replaced) from a single line of the sample mapping file
	public static String getFullCategoryCode(String line){
		if(line.compareTo("")!=0){
			String[] s=line.split("\t", -1);
			String[] c=s[8].split("\\+");
			String cat="";
			for(int i=0; i<c.length; i++){
				if(c[i].compareTo("PLATFORM")==0){
					String gplName=RetrieveData.getGplName(s[4]);
					if(gplName.compareTo("")!=0) cat+=gplName+"+";
					else{
						cat="Platform not found: "+s[4]+" ";
						break;
					}
				}
				else if(c[i].compareTo("TISSUETYPE")==0) cat+=s[5]+"+";
				else if(c[i].compareTo("ATTR1")==0) cat+=s[6]+"+";
				else if(c[i].compareTo("ATTR2")==0) cat+=s[7]+"+";					
				else cat+=c[i]+"+";
			}
			if(cat.length()>0) cat = cat.substring(0, cat.length()-1);
					
			return cat;
		}
		return "";
	}
	public static Vector<String[]> getFullClinicalCategoryCode(File mappingFile, Vector<File> rawFiles){
		Vector<String[]> cat=new Vector<String[]>();
		try{
			for(File f: rawFiles){
				int visitNumber=FileHandler.getNumberForLabel(mappingFile, "VISIT_NAME", f);
				if(visitNumber==-1){//no visit names, just get category code and labels
					BufferedReader br = new BufferedReader(new FileReader(mappingFile));
					String line=br.readLine();
					while((line=br.readLine())!=null){
						String[] fields=line.split("\t", -1);
						if(fields[0].compareTo(f.getName())==0 && !isReserved(fields[3])){
							String[] tab=new String[2];
							tab[0]=fields[1];
							if(fields[3].compareTo("")!=0) tab[1]=fields[3];
							else tab[1]=getHeaders(f).get(Integer.valueOf(fields[2])-1);
							cat.add(tab);
						}
					}
					br.close();					
				}else{
					Vector<String> visits=new Vector<String>();
					BufferedReader br = new BufferedReader(new FileReader(f));
					String line=br.readLine();
					while((line=br.readLine())!=null){
						String[] fields=line.split("\t", -1);
						if(!visits.contains(fields[visitNumber-1])) visits.add(fields[visitNumber-1]);
					}
					br.close();
					
					br = new BufferedReader(new FileReader(mappingFile));
					line=br.readLine();
					while((line=br.readLine())!=null){
						String[] fields=line.split("\t", -1);
						if(fields[0].compareTo(f.getName())==0 && !isReserved(fields[3])){
							for(String s: visits){
								String[] tab=new String[2];
								if(fields[3].compareTo("")!=0) tab[0]=fields[1]+"+"+fields[3];
								else tab[0]=tab[0]=fields[1]+"+"+getHeaders(f).get(Integer.valueOf(fields[2])-1);
								tab[1]=s;
								cat.add(tab);
							}
						}
					}
					br.close();		
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return new Vector<String[]>();
		}
		return cat;
	}
}

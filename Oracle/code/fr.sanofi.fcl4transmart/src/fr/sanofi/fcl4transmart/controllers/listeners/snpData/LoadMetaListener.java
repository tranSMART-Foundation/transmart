package fr.sanofi.fcl4transmart.controllers.listeners.snpData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.LoadMetaUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class LoadMetaListener implements Listener {
	private DataTypeItf dataType;
	private LoadMetaUI ui;
	private String message;
	private boolean foundSnpnode;
	private ChannelSftp c;
	public LoadMetaListener(DataTypeItf dataType, LoadMetaUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		ui.openLoadingShell();
		this.message="";
		new Thread(){
			public void run() {
				//search top node
				TreeNode root=ui.getRoot();
				String baseNode="";
				foundSnpnode=false;
				String topNode=RetrieveFm.getStudyTopNode(dataType.getStudy().toString());
				if(topNode.compareTo("")==0){
					message="Error: this study does not exist in Browse part";
					ui.setIsLoading(false);
					return;
				}
				for(String t: topNode.split("\\\\", -1)){
					if(t.compareTo("")!=0){
						baseNode+="/"+t;							
					}
				}
				for(TreeNode topFolder: root.getChildren()){
					String path=getPath(topFolder, baseNode);
					if(path.compareTo("")!=0){
						baseNode=path;
						break;
					}
				}
				
				if(!foundSnpnode){
					message="The tree has to contain a SNP node";
					ui.setIsLoading(false);
					return;
				}
				
				try{
					//Firstly, creates the metaTables.properties file
					File file=new File(dataType.getPath().toString()+File.separator+"metaTables.properties.tmp");
					try{
						FileWriter fw = new FileWriter(file);
						BufferedWriter out = new BufferedWriter(fw);
						
						out.write("study_name="+dataType.getStudy().toString()+"\n");
						out.write("source_system_prefix="+dataType.getStudy().toString()+"\n");
						if(!ui.isUseServer()){
							if(File.separator.compareTo("\\")==0){
								String path=dataType.getPath().getAbsolutePath();
								out.write("source_directory="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("source_directory="+dataType.getPath().getAbsolutePath()+"\n");
							}
						}else{
							String dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
							out.write("source_directory="+dir+"\n");
						}
						out.write("snp_base_node="+baseNode+"\n");
						out.write("subject_sample_mapping="+((SnpData)dataType).getMappingFile().getName()+"\n");
						
						//search in annotation.properties for platform type and name
						try{
							BufferedReader br = new BufferedReader(new FileReader(((SnpData)dataType).getAnnotationProps()));
							String line;
							while((line=br.readLine())!=null){
								if(line.indexOf("title=")==0){
									out.write("platform_name="+line.split("=", 2)[1]+"\n");
								}else if(line.indexOf("marker_type=")==0){
									out.write("platform_type="+line.split("=", 2)[1]+"\n");
								}
							}
							br.close();
						}catch (Exception e){
							message="Error: File annotation.properties missing or not valide";
							ui.setIsLoading(false);
							out.close();
							return;
						}
						
						HashMap<Boolean, String> mapBool=new HashMap<Boolean, String>();
						mapBool.put(true, "yes");
						mapBool.put(false, "no");
						//write skips
						out.write("skip_concept_dimension="+mapBool.get(ui.isSkipConceptDim())+"\n");
						out.write("skip_patient_dimension="+mapBool.get(ui.isSkipPatientDim())+"\n");
						out.write("skip_i2b2="+mapBool.get(ui.isSkipI2b2())+"\n");
						out.write("skip_i2b2_secure="+mapBool.get(ui.isSkipI2b2Secure())+"\n");
						out.write("skip_observation_fact="+mapBool.get(ui.isSkipObsFact())+"\n");
						out.write("skip_concept_counts="+mapBool.get(ui.isSkipConceptCounts())+"\n");
						out.write("skip_de_subject_sample_mapping="+mapBool.get(ui.isSkipSubjectSample())+"\n");
						out.write("skip_de_gpl_info=yes\n");
						
						out.close();
						File fileDest=new File(dataType.getPath().toString()+File.separator+"metaTables.properties");
						if(((SnpData)dataType).getMetaTablesProps()!=null){	
							((SnpData)dataType).getMetaTablesProps().delete();
						}
						FileUtils.moveFile(file, fileDest);
						((SnpData)dataType).setMetaTablesProps(fileDest);		
			
					  }catch (Exception e){
						  message="Error: "+e.getLocalizedMessage();
						  e.printStackTrace();
						  ui.setIsLoading(false);
						  return;
					  }
					
					//create the log4.properties file
					File logFile=new File(dataType.getPath().toString()+File.separator+"log4j.properties.tmp");
					try{
						FileWriter fw = new FileWriter(logFile);
						BufferedWriter out = new BufferedWriter(fw);
						
						out.write("log4j.rootLogger=INFO, console,file\n");
						out.write("log4j.appender.console=org.apache.log4j.ConsoleAppender\n");
						out.write("log4j.appender.console.layout=org.apache.log4j.PatternLayout\n");
						out.write("log4j.appender.console.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n\n");
						out.write("log4j.appender.file=org.apache.log4j.RollingFileAppender\n");
						if(!ui.isUseServer()){
							if(File.separator.compareTo("\\")==0){
								String path=dataType.getPath()+File.separator+"load_meta_tables.log";
								out.write("log4j.appender.file.File="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("log4j.appender.file.File="+dataType.getPath()+File.separator+"load_meta_tables.log\n");
							}
						}else{
							String dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
							out.write("log4j.appender.file.File="+dir+"/"+"load_meta_tables.log\n");
						}
						out.write("log4j.appender.file.layout=org.apache.log4j.PatternLayout\n");
						out.write("log4j.appender.file.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n\n");
						
						out.close();
						File fileDest=new File(dataType.getPath().toString()+File.separator+"log4j.properties");
						if(((SnpData)dataType).getLogProps()!=null){	
							((SnpData)dataType).getLogProps().delete();
						}
						FileUtils.moveFile(logFile, fileDest);
						((SnpData)dataType).setLogProps(fileDest);		
					  }catch (Exception e){
						  message="Error: "+e.getLocalizedMessage();
						  e.printStackTrace();
						  ui.setIsLoading(false);
						  return;
					  }
					
					  WorkPart.updateFiles();
					  UsedFilesPart.sendFilesChanged(dataType);
					  
					  String[] splited=topNode.split("\\\\");
					  //check if top node exists, add it
					  try{
						Class.forName("oracle.jdbc.driver.OracleDriver");
						String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
						
						Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getTm_czUser(), PreferencesHandler.getTm_czPwd());
						
						Statement stmt = con.createStatement();
						//insert program node if it does not exist
						ResultSet rs=stmt.executeQuery("select * from i2b2metadata.table_access where c_name='"+splited[1]+"'");
						if(!rs.next()){
							String sql =  "begin i2b2_add_root_node(?, ?); end;" ; // stored proc
							CallableStatement call = con.prepareCall(sql);
							call.setString(1,splited[1]);
							call.setNull(2, Types.INTEGER);
							try{
								call.executeUpdate();
							}catch(SQLException e){
								  message="Error: "+e.getLocalizedMessage();
								  e.printStackTrace();
								  ui.setIsLoading(false);
								  return;
							}
						}
						
						//insert study node if it does not exist
						rs=stmt.executeQuery("select * from i2b2metadata.i2b2 where c_fullname='"+topNode+"'");
						if(!rs.next()){
							String sql =  "begin i2b2_add_node(?, ?, ?, ?); end;" ; 
							CallableStatement call = con.prepareCall(sql);
							call.setString(1,dataType.getStudy().toString());
							call.setString(2,topNode);
							call.setString(3,splited[2]);
							call.setNull(4, Types.INTEGER);
							try{
								call.executeUpdate();
							}catch(SQLException e){
								  message="Error: "+e.getLocalizedMessage();
								  e.printStackTrace();
								  ui.setIsLoading(false);
								  return;
							}
							rs=stmt.executeQuery("update i2b2metadata.i2b2 set c_visualattributes = 'FAS' where c_fullname='"+topNode+"'");
						}
						
						//insert each node
						String node=topNode;
						splited=baseNode.split("/", -1);
						for(int i=3; i<splited.length; i++){
							if(splited[i].compareTo("")!=0){
								node+=splited[i]+"\\";
								rs=stmt.executeQuery("select * from i2b2metadata.i2b2 where c_fullname='"+node+"'");
								if(!rs.next()){
									String sql =  "begin i2b2_add_node(?, ?, ?, ?); end;" ; 
									CallableStatement call = con.prepareCall(sql);
									call.setString(1,dataType.getStudy().toString());
									call.setString(2,node);
									call.setString(3,splited[i]);
									call.setNull(4, Types.INTEGER);
									try{
										call.executeUpdate();
									}catch(SQLException e){
										  message="Error: "+e.getLocalizedMessage();
										  e.printStackTrace();
										  ui.setIsLoading(false);
										  return;
									}
								}
							}
						}
						String sql =  "begin i2b2_create_security_for_trial(?, ?, ?); end;" ; 
						CallableStatement call = con.prepareCall(sql);
						call.setString(1,dataType.getStudy().toString());
						call.setString(2,"N");
						call.setNull(3, Types.INTEGER);
						try{
							call.executeUpdate();
						}catch(SQLException e){
							  message="Error: "+e.getLocalizedMessage();
							  e.printStackTrace();
							  ui.setIsLoading(false);
							  return;
						}
						sql =  "begin i2b2_load_security_data(?); end;" ; 
						call = con.prepareCall(sql);
						call.setNull(1, Types.INTEGER);
						try{
							call.executeUpdate();
						}catch(SQLException e){
							  message="Error: "+e.getLocalizedMessage();
							  e.printStackTrace();
							  ui.setIsLoading(false);
							  return;
						}
						
							con.close();
						}catch(SQLException e){
							e.printStackTrace();
							message="SQL exception: "+e.getLocalizedMessage();
							ui.setIsLoading(false);
							return;
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							message="Class Not Found exception";
							ui.setIsLoading(false);
							return;
						}
						////
					  
					  //Run the loader
					  if(!ui.isUseServer()){
							URL jarUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/loader.jar");
							jarUrl = FileLocator.toFileURL(jarUrl);  
							String jarPath = jarUrl.getPath();
							String[] cmd = { "java", "-classpath", jarPath, "com.recomdata.pipeline.loader.Loader", ((SnpData)dataType).getMetaTablesProps().getAbsolutePath(), ((SnpData)dataType).getLogProps().getAbsolutePath(), "jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName(), "oracle.jdbc.driver.OracleDriver", PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd(), PreferencesHandler.getMetadataUser(), PreferencesHandler.getMetadataPwd(), PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd()};

					        Process p = Runtime.getRuntime().exec(cmd);

					        BufferedReader stdInput = new BufferedReader(new 
					                InputStreamReader(p.getInputStream()));
					        BufferedReader stdError = new BufferedReader(new 
					                InputStreamReader(p.getErrorStream()));
					     // read the output from the command
					        String s;
					        while ((s = stdInput.readLine()) != null) {
					            System.out.println(s);
					        }
					     // read any errors from the attempted command
					        while ((s = stdError.readLine()) != null) {
					            System.out.println(s);
					            if(s.indexOf("java.lang.OutOfMemoryError")!=-1){
									  message="Error: Out of memory";
									  ui.setIsLoading(false);
									  return;
					            }
					        }
					        p.waitFor();
					        
					        File log=new File(dataType.getPath()+File.separator+"load_meta_tables.log");
					        if(log!=null && log.exists() && log.isFile()){
					        	((SnpData)dataType).addOutputFile(log);
					        }
					  }else{
						//etl server is used
							//transfer files in filesDirectory in ETL server  
							URL jarUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/loader.jar");
							jarUrl = FileLocator.toFileURL(jarUrl);  
							String jarPath = jarUrl.getPath();
							try{
								JSch jsch=new JSch();
								Session session=jsch.getSession(etlPreferences.getUser(), etlPreferences.getHost(), Integer.valueOf(etlPreferences.getPort()));
								session.setPassword(etlPreferences.getPass());
							 
								java.util.Properties config = new java.util.Properties(); 
								config.put("StrictHostKeyChecking", "no");
								config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
								session.setConfig(config);
								
								session.connect();
							 
								Channel channel=session.openChannel("sftp");
								channel.connect();
								c=(ChannelSftp)channel;
												
								//try to go to the right directory for file transfer
								String dir=etlPreferences.getFilesDirectory();
								if(dir.compareTo("")!=0){
									try{
										c.cd(dir);
									}catch(SftpException e){
										channel.disconnect();
										session.disconnect();
										message="The file directory does not exist in this server";
										ui.setIsLoading(false);
										return;
									}
								}else{
									channel.disconnect();
									session.disconnect();
									message="No file directory is indicated";
									ui.setIsLoading(false);
									return;
								}
								try{
									try{
										c.mkdir(dataType.getStudy().toString());
									}catch(Exception exist){
										//normal if directory already exists
									}
									c.cd(dataType.getStudy().toString());
									try{
										c.mkdir("snp");
									}catch(Exception exist2){
										//normal if directory already exists
									}
									c.cd("snp");
								}catch(SftpException e){
									channel.disconnect();
									session.disconnect();
									message="Directory can not be created";
									ui.setIsLoading(false);
				            	   	return;
								}

								//transfer files
								try{
									File snpPath=dataType.getPath();
									for(File f: snpPath.listFiles()){
										c.put(f.getAbsolutePath(), ".", null, ChannelSftp.OVERWRITE);
									}
									c.put(jarPath, ".", null, ChannelSftp.OVERWRITE);
									
								}catch(SftpException e){
									channel.disconnect();
									session.disconnect();
									message="Error when transferring files";
									ui.setIsLoading(false);
									return;
								}
								
								//close streams for transfer
								channel.disconnect();
	
								try{
									channel=session.openChannel("exec");
									dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
									String command = "java -classpath "+dir+"/loader.jar com.recomdata.pipeline.loader.Loader "+dir+"/"+((SnpData)dataType).getMetaTablesProps().getName()+" "+dir+"/"+((SnpData)dataType).getLogProps().getName()+" "+"jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName()+" "+"oracle.jdbc.driver.OracleDriver"+" "+PreferencesHandler.getDeappUser()+" "+PreferencesHandler.getDeappPwd()+" "+PreferencesHandler.getMetadataUser()+" "+PreferencesHandler.getMetadataPwd()+" "+PreferencesHandler.getDemodataUser()+" "+PreferencesHandler.getDemodataPwd();
									((ChannelExec)channel).setCommand(command);
								 
									 BufferedReader stdError = new BufferedReader(new 
								                InputStreamReader(((ChannelExec)channel).getErrStream()));
									channel.setInputStream(null);
									InputStream in=channel.getInputStream();
							 
									channel.connect();
							 
									byte[] tmp=new byte[1024];
									while(true){
										while(in.available()>0){
											int i=in.read(tmp, 0, 1024);
											if(i<0)break;
									      		System.out.print(new String(tmp, 0, i));
									      	}
									      	if(channel.isClosed()){
									      		System.out.println("exit-status: "+channel.getExitStatus());
									      		break;
									      	}
									      	try{Thread.sleep(1000);}catch(Exception ee){}
									}
									channel.disconnect();
							     // read any errors from the attempted command
									String s;
							        while ((s = stdError.readLine()) != null) {
							            System.out.println(s);
							            if(s.indexOf("java.lang.OutOfMemoryError")!=-1){
											  message="Error: Out of memory";
											  ui.setIsLoading(false);
											  return;
							            }
							        }
							    }
							    catch(Exception e){
							    	channel.disconnect();
							    	session.disconnect();
							    	e.printStackTrace();
									message="Error when converting files";
									ui.setIsLoading(false);
									return;
							    }
								channel.disconnect();								

								//get the files back
								dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
								channel=session.openChannel("sftp");
								channel.connect();
								c=(ChannelSftp)channel;
								c.cd(dir);
								@SuppressWarnings("unchecked")
								Vector<ChannelSftp.LsEntry> files = c.ls(".");
								if(files!=null){
									for(int i=0; i<files.size(); i++){
										if(files.get(i).getFilename().compareTo("loader.jar")!=0 && files.get(i).getFilename().compareTo(".")!=0 && files.get(i).getFilename().compareTo("..")!=0){
											System.out.println(files.get(i).getFilename());
											c.get(files.get(i).getFilename(), dataType.getPath().getAbsolutePath(), null, ChannelSftp.OVERWRITE);
										}
									}
								}

								channel.disconnect();
								session.disconnect();
							}catch (Exception e1){
								e1.printStackTrace();
								message="Error when transferring files: "+e1.getLocalizedMessage();
								ui.setIsLoading(false);
								return;
							}
					        File log=new File(dataType.getPath()+File.separator+"load_meta_tables.log");
					        if(log!=null && log.exists() && log.isFile()){
					        	((SnpData)dataType).addOutputFile(log);
					        }
					  }
					  
					  try{
							Class.forName("oracle.jdbc.driver.OracleDriver");
							String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
							
							Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getDemodataUser(), PreferencesHandler.getDemodataPwd());
							
							Statement stmt = con.createStatement();
							
							//update age_in_years_num from table patient_dimension to non null (for summary statistics to work)
							ResultSet rs=stmt.executeQuery("update patient_dimension set age_in_years_num=0 where age_in_years_num is null and sourcesystem_cd like'"+dataType.getStudy().toString()+":%'");
							
							//update concept_counts for study node
							String node=topNode;
							String programNode="\\"+topNode.split("\\\\", -1)[1]+"\\";
							rs=stmt.executeQuery("select count(*) from patient_dimension where sourcesystem_cd like'"+dataType.getStudy().toString()+":%'");
							if(rs.next()){
								int n=rs.getInt(1);
								rs=stmt.executeQuery("insert into concept_counts (concept_path, parent_concept_path, patient_count) values('"+topNode+"', '"+programNode+"', "+n+")");
							}
							
							//update concept_counts for other nodes
							splited=baseNode.split("/", -1);
							String parentNode=topNode;
							node=topNode;
							for(int i=3; i<splited.length-1; i++){
								if(splited[i].compareTo("")!=0){
									node+=splited[i]+"\\";
									rs=stmt.executeQuery("select count(*) from deapp.de_subject_sample_mapping where trial_name='"+dataType.getStudy().toString()+"' and platform='SNP'");
									if(rs.next()){
										int n=rs.getInt(1);
										rs=stmt.executeQuery("insert into concept_counts (concept_path, parent_concept_path, patient_count) values('"+node+"', '"+parentNode+"', "+n+")");
										parentNode=node;
									}				
								}
							}
							
								con.close();
							}catch(SQLException e){
								e.printStackTrace();
								message="SQL exception: "+e.getLocalizedMessage();
								ui.setIsLoading(false);
								return;
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								message="Class Not Found exception";
								ui.setIsLoading(false);
								return;
							}
				  }catch (Exception e){
					  message="Error: "+e.getLocalizedMessage();
					  e.printStackTrace();
					  ui.setIsLoading(false);
					  return;
				  }
			  WorkPart.updateSteps();  
			  ui.setIsLoading(false);
			}
		}.start();
		this.ui.waitForThread();
		if(message.compareTo("")==0){
			this.ui.displayMessage("Loading process is over.\n Please check log file and checking step.");
		}else{
			this.ui.displayMessage(message);
		}
		WorkPart.updateFiles();
		UsedFilesPart.sendFilesChanged(dataType);
	}
	/**
	 *Gets the Snp base node in the tree (recursive method)
	 */	
	private String getPath(TreeNode node, String path){
		path+="/"+node.toString();
		String baseNode="";
		for(TreeNode child: node.getChildren()){
			if(child.isLabel()){
				foundSnpnode=true;
				return path;
			}
			baseNode=this.getPath(child, path);
			if(baseNode.compareTo("")!=0){
				return baseNode;
			}
		}
		return "";
	}
}

package fr.sanofi.fcl4transmart.controllers.listeners.snpData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.handlers.etlPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.LoadAnnotationUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class LoadAnnotationListener implements Listener {
	private DataTypeItf dataType;
	private LoadAnnotationUI ui;
	private String message;
	private ChannelSftp c;
	public LoadAnnotationListener(DataTypeItf dataType, LoadAnnotationUI ui){
		this.dataType=dataType;
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		if(ui.getPlatformId().compareTo("")==0){
			ui.displayMessage("No platform identifier provided");
			return;
		}if(ui.getPlatformTitle().compareTo("")==0){
			ui.displayMessage("No platform title provided");
			return;
		}if(ui.getPlatformType().compareTo("")==0){
			ui.displayMessage("No platform marekr type provided");
			return;
		}if(ui.getPlatformOrganism().compareTo("")==0){
			ui.displayMessage("No platform organism provided");
			return;
		}if(ui.getSnpId().compareTo("")==0){
			ui.displayMessage("No SNP identifiers provided");
			return;
		}if(ui.getRsId().compareTo("")==0){
			ui.displayMessage("No pRS identifiers provided");
			return;
		}if(ui.getChr().compareTo("")==0){
			ui.displayMessage("No chromosome number provided");
			return;
		}if(ui.getPos().compareTo("")==0){
			ui.displayMessage("No SNP position provided");
			return;
		}
		ui.openLoadingShell();
		this.message="";
		new Thread(){
			public void run() {
				try{
					//Firstly, creates the annotation.properties file
					File file=new File(dataType.getPath().toString()+File.separator+"annotation.properties.tmp");
					try{
						FileWriter fw = new FileWriter(file);
						BufferedWriter out = new BufferedWriter(fw);
						
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
						out.write("input_file="+((SnpData)dataType).getAnnotationFile().getName()+"\n");
						if(!ui.isUseServer()){
							if(File.separator.compareTo("\\")==0){
								String path=dataType.getPath().getAbsolutePath();
								out.write("destination_directory="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("destination_directory="+dataType.getPath().getAbsolutePath()+"\n");
							}
						}else{
							String dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
							out.write("destination_directory="+dir+"\n");
						}
						out.write("probe_info_file="+dataType.getStudy().toString()+".probeinfo"+"\n");
						out.write("snp_gene_map_file="+dataType.getStudy().toString()+".genemap"+"\n");
						out.write("snp_map_file="+dataType.getStudy().toString()+".map"+"\n");
						out.write("platform="+ui.getPlatformId()+"\n");
						out.write("title="+ui.getPlatformTitle()+"\n");
						out.write("marker_type="+ui.getPlatformType()+"\n");
						out.write("organism="+ui.getPlatformOrganism()+"\n");
						out.write("snp_id="+ui.getSnpId()+"\n");
						out.write("rsId="+ui.getRsId()+"\n");
						out.write("chr="+ui.getChr()+"\n");
						out.write("pos="+ui.getPos()+"\n");
						out.write("gene="+ui.getGene()+"\n");
						
						HashMap<Boolean, String> mapBool=new HashMap<Boolean, String>();
						mapBool.put(true, "yes");
						mapBool.put(false, "no");
						out.write("skip_gpl_annotation_loader="+mapBool.get(ui.isSkipGplLoader())+"\n");
						out.write("skip_gx_annotation_loader=yes"+"\n");
						out.write("skip_taxonomy_name=yes"+"\n");
						out.write("skip_gene_info=yes"+"\n");
						out.write("skip_gx_gpl_loader=yes"+"\n");
						out.write("skip_de_gpl_info="+mapBool.get(ui.isSkipGplInfo())+"\n");
						out.write("skip_de_snp_info="+mapBool.get(ui.isSkipSnpInfo())+"\n");
						out.write("skip_de_snp_probe="+mapBool.get(ui.isSkipSnpProbe())+"\n");
						out.write("skip_de_snp_gene_map="+mapBool.get(ui.isSkipGeneMap())+"\n");
						
						out.close();
						File fileDest=new File(dataType.getPath().toString()+File.separator+"annotation.properties");
						if(((SnpData)dataType).getAnnotationProps()!=null){	
							((SnpData)dataType).getAnnotationProps().delete();
						}
						FileUtils.moveFile(file, fileDest);
						((SnpData)dataType).setAnnotationProps(fileDest);		
			
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
								String path=dataType.getPath()+File.separator+"load_annotation.log";
								out.write("log4j.appender.file.File="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("log4j.appender.file.File="+dataType.getPath()+File.separator+"load_annotation.log\n");
							}
						}else{
							String dir=etlPreferences.getFilesDirectory();
							out.write("log4j.appender.file.File="+dir+"/"+"load_annotation.log\n");
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
					  
					  //Run the loader
					  if(!ui.isUseServer()){
							URL jarUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/loader.jar");
							jarUrl = FileLocator.toFileURL(jarUrl);  
							String jarPath = jarUrl.getPath();
							String[] cmd = { "java", "-classpath", jarPath, "com.recomdata.pipeline.annotation.AnnotationLoader", ((SnpData)dataType).getAnnotationProps().getAbsolutePath(), ((SnpData)dataType).getLogProps().getAbsolutePath(), "jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName(), "oracle.jdbc.driver.OracleDriver", PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd(), PreferencesHandler.getBiomartUser(), PreferencesHandler.getBiomartPwd()};

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
					        
					        File log=new File(dataType.getPath()+File.separator+"load_annotation.log");
					        if(log!=null && log.exists() && log.isFile()){
					        	((SnpData)dataType).addOutputFile(log);
					        }
					        File map=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".map");
					        if(map!=null && map.exists() && map.isFile()){
					        	((SnpData)dataType).addOutputFile(map);
					        }
					        File geneMap=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".genemap");
					        if(geneMap!=null && geneMap.exists() && geneMap.isFile()){
					        	((SnpData)dataType).addOutputFile(geneMap);
					        }
					        File probeInfo=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".probeinfo");
					        if(probeInfo!=null && probeInfo.exists() && probeInfo.isFile()){
					        	((SnpData)dataType).addOutputFile(probeInfo);
					        }
					  }else{//etl server is used
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
									String command = "java -classpath "+dir+"/loader.jar com.recomdata.pipeline.annotation.AnnotationLoader "+dir+"/"+((SnpData)dataType).getAnnotationProps().getName()+" "+dir+"/"+((SnpData)dataType).getLogProps().getName()+" "+"jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName()+" "+"oracle.jdbc.driver.OracleDriver"+" "+PreferencesHandler.getDeappUser()+" "+PreferencesHandler.getDeappPwd()+" "+PreferencesHandler.getBiomartUser()+" "+PreferencesHandler.getBiomartPwd();
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
									message="Error when loading files: "+e.getLocalizedMessage();
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
					        File log=new File(dataType.getPath()+File.separator+"load_annotation.log");
					        if(log!=null && log.exists() && log.isFile()){
					        	((SnpData)dataType).addOutputFile(log);
					        }
					        File map=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".map");
					        if(map!=null && map.exists() && map.isFile()){
					        	((SnpData)dataType).addOutputFile(map);
					        }
					        File geneMap=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".genemap");
					        if(geneMap!=null && geneMap.exists() && geneMap.isFile()){
					        	((SnpData)dataType).addOutputFile(geneMap);
					        }
					        File probeInfo=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".probeinfo");
					        if(probeInfo!=null && probeInfo.exists() && probeInfo.isFile()){
					        	((SnpData)dataType).addOutputFile(probeInfo);
					        }
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

}

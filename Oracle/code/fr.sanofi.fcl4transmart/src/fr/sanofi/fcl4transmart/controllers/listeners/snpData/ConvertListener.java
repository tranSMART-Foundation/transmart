package fr.sanofi.fcl4transmart.controllers.listeners.snpData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
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
import fr.sanofi.fcl4transmart.model.classes.workUI.SNPData.ConvertUI;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.ui.parts.UsedFilesPart;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class ConvertListener implements Listener {
	private DataTypeItf dataType;
	private ConvertUI ui;
	private String message;
	private ChannelSftp c;
	public String snpId;
	public String rsId;
	public ConvertListener(DataTypeItf dataType, ConvertUI ui){
		this.dataType=dataType;
		this.ui=ui;
		this.message="";
	}
	@Override
	public void handleEvent(Event event) {
		if(ui.getPath().compareTo("")==0){
			ui.displayMessage("No Plink path provided");
			return;
		}
		ui.openLoadingShell();
		this.message="";
		new Thread(){
			public void run() {
				try{
					if(!searchInAnnotProperties()){
						 message="Error: Cannot read annotation.properties";
						  ui.setIsLoading(false);
						  return;
					}
					File file=new File(dataType.getPath().toString()+File.separator+"convert.properties.tmp");
					try{
						FileWriter fw = new FileWriter(file);
						BufferedWriter out = new BufferedWriter(fw);
						
						out.write("batch_size=10000\n");
						out.write("study_id="+dataType.getStudy().toString()+"\n");
						out.write("study_name="+dataType.getStudy().toString()+"\n");
						out.write("plink="+ui.getPath()+"\n");
						if(!ui.isUseEtlServer()){
							if(File.separator.compareTo("\\")==0){
								String path=((SnpData)dataType).getAnnotationFile().getAbsolutePath();
								out.write("snp_annotation="+path.replace("\\", "\\\\")+"\n");
								path=((SnpData)dataType).getRawFile().getAbsolutePath();
								out.write("genotype_data="+path.replace("\\", "\\\\")+"\n");
								path=((SnpData)dataType).getMappingFile().getAbsolutePath();
								out.write("subject_mapping="+path.replace("\\", "\\\\")+"\n");
								path=dataType.getPath().getAbsolutePath()+File.separator+dataType.getStudy().toString();
								out.write("output_genotype_data="+path.replace("\\", "\\\\")+".genotype"+"\n");
								out.write("output_lgen_data="+path.replace("\\", "\\\\")+".lgen"+"\n");
								out.write("output_lgen_gsm_data="+path.replace("\\", "\\\\")+".lgen.gsm"+"\n");
								out.write("output_fam="+path.replace("\\", "\\\\")+".fam"+"\n");
								path=dataType.getPath().getAbsolutePath();
								out.write("output_directory="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("snp_annotation="+((SnpData)dataType).getAnnotationFile().getAbsolutePath()+"\n");
								out.write("genotype_data="+((SnpData)dataType).getRawFile().getAbsolutePath()+"\n");
								out.write("subject_mapping="+((SnpData)dataType).getMappingFile().getAbsolutePath()+"\n");
								out.write("output_genotype_data="+dataType.getPath().getAbsolutePath()+File.separator+dataType.getStudy().toString()+".genotype"+"\n");
								out.write("output_lgen_data="+dataType.getPath().getAbsolutePath()+File.separator+dataType.getStudy().toString()+".lgen"+"\n");
								out.write("output_lgen_gsm_data="+dataType.getPath().getAbsolutePath()+File.separator+dataType.getStudy().toString()+".lgen.gsm"+"\n");
								out.write("output_fam="+dataType.getPath().getAbsolutePath()+File.separator+dataType.getStudy().toString()+".fam"+"\n");
								out.write("output_directory="+dataType.getPath().getAbsolutePath()+"\n");
							}
							
						}else{
							String dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
							out.write("snp_annotation="+dir+"/"+((SnpData)dataType).getAnnotationFile().getName()+"\n");
							out.write("genotype_data="+dir+"/"+((SnpData)dataType).getRawFile().getName()+"\n");
							out.write("subject_mapping="+dir+"/"+((SnpData)dataType).getMappingFile().getName()+"\n");
							out.write("output_genotype_data="+dir+"/"+dataType.getStudy().toString()+".genotype"+"\n");
							out.write("output_lgen_data="+dir+"/"+dataType.getStudy().toString()+".lgen"+"\n");
							out.write("output_lgen_gsm_data="+dir+"/"+dataType.getStudy().toString()+".lgen.gsm"+"\n");
							out.write("output_fam="+dir+"/"+dataType.getStudy().toString()+".fam"+"\n");
							out.write("output_directory="+dir+"\n");
						}
						out.write("snp_id="+snpId+"\n");
						out.write("rsId="+rsId+"\n");
						HashMap<Boolean, String> mapBool=new HashMap<Boolean, String>();
						mapBool.put(true, "yes");
						mapBool.put(false, "no");
						out.write("skip_format_genotype="+mapBool.get(ui.isSkipGenotype())+"\n");
						out.write("skip_format_lgen="+mapBool.get(ui.isSkipLgen())+"\n");
						out.write("skip_create_fam="+mapBool.get(ui.isSkipFam())+"\n");
						out.write("skip_plink_file_creation="+mapBool.get(ui.isSkipPlink())+"\n");
						
						out.close();
						File fileDest=new File(dataType.getPath().toString()+File.separator+"convert.properties");
						if(((SnpData)dataType).getConversionProps()!=null){	
							((SnpData)dataType).getConversionProps().delete();
						}
						FileUtils.moveFile(file, fileDest);
						((SnpData)dataType).setConversionProps(fileDest);		
			
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
						if(!ui.isUseEtlServer()){
							if(File.separator.compareTo("\\")==0){
								String path=dataType.getPath()+File.separator+"convert.log";
								out.write("log4j.appender.file.File="+path.replace("\\", "\\\\")+"\n");
							}else{
								out.write("log4j.appender.file.File="+dataType.getPath()+File.separator+"convert.log\n");
							}
						}else{
							String dir=etlPreferences.getFilesDirectory()+"/"+dataType.getStudy().toString()+"/snp";
							out.write("log4j.appender.file.File="+dir+"/"+"convert.log\n");
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
					  if(!ui.isUseEtlServer()){
							URL jarUrl = new URL("platform:/plugin/fr.sanofi.fcl4transmart/jobs_kettle/loader.jar");
							jarUrl = FileLocator.toFileURL(jarUrl);  
							String jarPath = jarUrl.getPath();
							jarPath = URLDecoder.decode(jarPath, "utf-8");
							jarPath = new File(jarPath).getPath();
							String[] cmd = { "java", "-classpath", jarPath, "com.recomdata.pipeline.converter.IlluminaGenotypingFormatter", 
									((SnpData)dataType).getConversionProps().getAbsolutePath(), ((SnpData)dataType).getLogProps().getAbsolutePath(), 
									"jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName(),
									"oracle.jdbc.driver.OracleDriver", PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd(), PreferencesHandler.getDemodataUser(),
									PreferencesHandler.getDemodataPwd()};

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
					        
					        File log=new File(dataType.getPath()+File.separator+"convert.log");
					        if(log!=null && log.exists() && log.isFile()){
					        	((SnpData)dataType).addOutputFile(log);
					        }
					        File f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".genotype");
					        if(f!=null && f.exists() && f.isFile()){
					        	((SnpData)dataType).addOutputFile(f);
					        }
					        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".lgen");
					        if(f!=null && f.exists() && f.isFile()){
					        	((SnpData)dataType).addOutputFile(f);
					        }
					        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".lgen.gsm");
					        if(f!=null && f.exists() && f.isFile()){
					        	((SnpData)dataType).addOutputFile(f);
					        }
					        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".fam");
					        if(f!=null && f.exists() && f.isFile()){
					        	((SnpData)dataType).addOutputFile(f);
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
									String command = "java -classpath "+dir+"/loader.jar com.recomdata.pipeline.converter.IlluminaGenotypingFormatter "+dir+"/"+((SnpData)dataType).getConversionProps().getName()+" "+dir+"/"+((SnpData)dataType).getLogProps().getName()+" "+ "jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName()+" "+ "oracle.jdbc.driver.OracleDriver"+" "+ PreferencesHandler.getDeappUser()+" "+PreferencesHandler.getDeappPwd()+" "+PreferencesHandler.getDemodataUser()+" "+PreferencesHandler.getDemodataPwd();
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
							
							    }catch(Exception e){
							    	channel.disconnect();
							    	session.disconnect();
							    	e.printStackTrace();
									message="Error when converting files: "+e.getLocalizedMessage();
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
							 File log=new File(dataType.getPath()+File.separator+"convert.log");
						        if(log!=null && log.exists() && log.isFile()){
						        	((SnpData)dataType).addOutputFile(log);
						        }
						        File f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".genotype");
						        if(f!=null && f.exists() && f.isFile()){
						        	((SnpData)dataType).addOutputFile(f);
						        }
						        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".lgen");
						        if(f!=null && f.exists() && f.isFile()){
						        	((SnpData)dataType).addOutputFile(f);
						        }
						        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".lgen.gsm");
						        if(f!=null && f.exists() && f.isFile()){
						        	((SnpData)dataType).addOutputFile(f);
						        }
						        f=new File(dataType.getPath()+File.separator+dataType.getStudy().toString()+".fam");
						        if(f!=null && f.exists() && f.isFile()){
						        	((SnpData)dataType).addOutputFile(f);
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
	public boolean searchInAnnotProperties(){
		if(((SnpData)this.dataType).getAnnotationProps()!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(((SnpData)this.dataType).getAnnotationProps()));
				String line;
				while((line=br.readLine())!=null){
					if(line.indexOf("snp_id=")==0){
						this.snpId=line.split("=", 2)[1];
					}else if(line.indexOf("rsId=")==0){
						this.rsId=line.split("=", 2)[1];
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}else{
			return false;
		}
		return true;
	}
}

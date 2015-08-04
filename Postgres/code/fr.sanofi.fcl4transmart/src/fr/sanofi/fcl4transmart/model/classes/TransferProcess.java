package fr.sanofi.fcl4transmart.model.classes;

import java.io.File;
import java.util.Vector;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import fr.sanofi.fcl4transmart.handlers.FTPPreferences;
import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;

public class TransferProcess extends Thread {
	private File fileToTransfer;
	private String remotePath;
	private boolean isEnded;
	private ProgressBar bar;
	private Label progressLabel;
	private Label statusLabel;
	private String status;
	private double done;
	private double size;
	private boolean cancelled;
	private DataTypeItf dataType;
	private boolean confirmation;
	private Button cancel;
	private ChannelSftp c;
	private TransferProgressMonitor monitor;
	private boolean waitConfirm;
	public TransferProcess(File fileToTransfer, DataTypeItf dataType){
		this.fileToTransfer=fileToTransfer;
		this.status="Not started";
		this.isEnded=false;
		this.dataType=dataType;
		this.cancelled=false;
		this.size=1.0;
		this.done=0.0;
		this.status="Not started";
		if(this.cancel!=null && !this.cancel.isDisposed()) this.setCancelButton();
		this.monitor=new TransferProgressMonitor(this);
		this.waitConfirm=false;
	}
	public void run(){
		if(this.remotePath!=null){
			try{
				JSch jsch=new JSch();
				Session session=jsch.getSession(FTPPreferences.getUser(), FTPPreferences.getHost(), Integer.valueOf(FTPPreferences.getPort()));
				session.setPassword(FTPPreferences.getPass());
			 
				java.util.Properties config = new java.util.Properties(); 
				config.put("StrictHostKeyChecking", "no");
				config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
				session.setConfig(config);
				
				session.connect();
			 
				Channel channel=session.openChannel("sftp");
				channel.connect();
				c=(ChannelSftp)channel;
								
				//try to go to the right directory for file transfer, should not fail as tested before
				String dir=FTPPreferences.getDirectory();
				if(dir.compareTo("")!=0){
					try{
						c.cd(dir);
					}catch(SftpException e){
						displayMessage("The transfer directory does not exist in this server");
						status="Writing error";
						monitor.setStatus("Writing Error");
						isEnded=true;
						((FilesTransfer)dataType).processesFinished();
						return;
					}
				}
				
				//check for a directory corresponding to the user name, then go in it
				boolean userDirExists=true;
				String userName=FTPPreferences.getUser();
				int groupId=FTPPreferences.getGroupId();
				try{
					c.cd(userName);
				}catch(SftpException e){
					userDirExists=false;
				}
				if(!userDirExists){
					try{
						c.mkdir(userName);
						try{
							if(groupId>0){
								c.chgrp(groupId, userName);
							}
							c.chmod(Integer.parseInt("770",8), userName);
						}catch(SftpException e){
							Display.getDefault().asyncExec(new Runnable() {
					               public void run() {
					            	   displayMessage("Unix group can not be changed");
					            	   status="Writing error";
					            	   monitor.setStatus("Writing Error");
					            	   isEnded=true;
					            	   ((FilesTransfer)dataType).processesFinished();
					               }
							});
							return;
						}
						c.cd(userName);
					}catch(SftpException e){
						try{
							c.cd(userName);
						}catch(SftpException e2){
							Display.getDefault().asyncExec(new Runnable() {
					               public void run() {
					            	   displayMessage("Directory can not be created");
					            	   status="Writing error";
					            	   monitor.setStatus("Writing Error");
					            	   isEnded=true;
					            	   ((FilesTransfer)dataType).processesFinished();
					               }
							});
							return;
						}
					}
				}
				
				//check for a directory corresponding to the remote Path, then go in it
				boolean idDirExists=true;
				try{
					c.cd(this.remotePath);
				}catch(SftpException e){
					idDirExists=false;
				}
				if(!idDirExists){
					try{
						c.mkdir(this.remotePath);
						try{
							if(groupId>0){
								c.chgrp(groupId, this.remotePath);
							}
							c.chmod(Integer.parseInt("770",8), this.remotePath);
						}catch(SftpException e){
							Display.getDefault().asyncExec(new Runnable() {
					               public void run() {
					            	   displayMessage("Unix group can not be changed");
					            	   status="Writing error";
					            	   monitor.setStatus("Writing Error");
					            	   isEnded=true;
					            	   ((FilesTransfer)dataType).processesFinished();
					               }
							});
							return;
						}
						c.cd(this.remotePath);
					}catch(SftpException e){
						try{
							c.cd(this.remotePath);	
						}catch(SftpException e2){
							Display.getDefault().asyncExec(new Runnable() {
					               public void run() {
					            	   displayMessage("Directory can not be created");
					            	   status="Writing error";
										monitor.setStatus("Writing Error");
										isEnded=true;
										((FilesTransfer)dataType).processesFinished();
					               }
							});
							return;
						}
					}
				}

				//check that this file name does not exist
				if(this.checkFileExists(this.fileToTransfer.getName())){
					confirmation=false;
					Display.getDefault().asyncExec(new Runnable() {
			               public void run() {
			            	  confirmation=confirm("A file with name "+fileToTransfer.getName()+" already exist on the FTP server. Are you sure you want to erase it");
			            	  waitConfirm=true;
			               }
					});
					while(!this.waitConfirm){}
					if(confirmation){
						try{
							c.rm(fileToTransfer.getName());
						}catch(SftpException e){
							this.status="Writing error";
							this.monitor.setStatus("Writing Error");
							this.isEnded=true;
							((FilesTransfer)dataType).processesFinished();
							return;
						}
					}else{
						return;
					}
				}
				try{
					c.put(this.fileToTransfer.getAbsolutePath(), ".", monitor, ChannelSftp.OVERWRITE);
					
				}catch(SftpException e){
					if(!this.isEnded && !this.cancelled){
						e.printStackTrace();
						this.status="Error";
						this.monitor.setStatus(this.status);
						Display.getDefault().asyncExec(new Runnable() {
				               public void run() {
				            	   displayMessage("Error when loading");
				               }
						});
					}
				}
			}catch (Exception e1){
				e1.printStackTrace();
				this.status="Error";
				monitor.setStatus(this.status);
				this.isEnded=true;
			}
		}
	}
	public void finished(){
		this.isEnded=true;
		try{
			int groupId=FTPPreferences.getGroupId();
			if(groupId>0){
				c.chgrp(groupId, this.fileToTransfer.getName());
			}
			c.chmod(Integer.parseInt("770",8), this.fileToTransfer.getName());
		}catch(SftpException e){
			e.printStackTrace();
		}
		((FilesTransfer)dataType).processesFinished();
		c.disconnect();
	}
	public void setRemotePath(String remotePath){
		this.remotePath=remotePath;
	}
	public String getRemotePath(){
		return this.remotePath;
	}
	public void setStatus(String status){
		this.status=status;
	}
	public String getStatus(){
		return this.status;
	}
	public boolean getIsEnded(){
		return this.isEnded;
	}
	public String toString(){
		return this.fileToTransfer.getName();
	}
	public void setBar(ProgressBar progressBar){
		this.bar=progressBar;
		if (bar==null || bar.isDisposed ()) return;
	    Display.getDefault().asyncExec(new Runnable() {
               public void run() {
            	   if(((done/size)*100)<100){
   					bar.setSelection((int) ((done/size)*100));
	              	}else{
		            	bar.setSelection(100);
	              	}
               }
	    });
	    this.monitor.setBar(bar);
	}
	public String getFileName(){
		return this.fileToTransfer.getName();
	}
	public void cancel(){
		this.status="Cancelled";
		this.cancelled=true;
		monitor.setCancelled(true);
		try{
			c.rm(this.fileToTransfer.getName());
			Display.getDefault().asyncExec(new Runnable() {
    			public void run() {
					if(!(bar==null || bar.isDisposed ())){
						progressLabel.setText("0%");
					}
					if(!(bar==null || bar.isDisposed ())){
						bar.setSelection(0);
					}
    			}
		    });	
			this.setRestartButton();
		}catch(SftpException e){
			this.status="Deleting error when cancelling";
		}
		((FilesTransfer)dataType).processesFinished();
		c.disconnect();
	}
	public void setProgressLabel(Label label){
		this.progressLabel=label;
		if (progressLabel==null || progressLabel.isDisposed ()) return;
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	if(((done/size)*100)<100){
            		progressLabel.setText((int)((done/size)*100)+"%");
            	}else{
            		progressLabel.setText("100%");
            	}
            }
	    });
		this.monitor.setProgressLabel(label);
	}
	public void setStatusLabel(Label label){
		this.statusLabel=label;
		if (statusLabel==null || statusLabel.isDisposed ()) return;
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	statusLabel.setText(status);
            }
	    });
		this.monitor.setStatusLabel(label);
	}
	public boolean confirm(String message){
		return MessageDialog.openConfirm(new Shell(), "Confirm", message);
	}
	public static boolean testConnection(){
		try{

			JSch jsch=new JSch();
			Session session=jsch.getSession(FTPPreferences.getUser(), FTPPreferences.getHost(), Integer.valueOf(FTPPreferences.getPort()));
			session.setPassword(FTPPreferences.getPass());
			
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
			session.setConfig(config);
			
			session.connect();
		 
			Channel channel=session.openChannel("sftp");
			channel.connect();
			ChannelSftp c=(ChannelSftp)channel;
			
			//try to go to the right directory for file transfer
			String dir=FTPPreferences.getDirectory();
			if(dir.compareTo("")!=0){
				try{
					c.cd(dir);
				}catch(SftpException e){
					displayMessage("The transfer directory does not exist in this server");
					return false;
				}
			}
			
			channel.disconnect();
			session.disconnect();
		}catch (Exception e1){
			e1.printStackTrace();
			displayMessage("Connection with the FTP server is not possible");
			return false;
		}
		return true;
	}
	public static void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public void setButton(Button cancelButton){
		this.cancel=cancelButton;
		if(this.status.compareTo("Cancelled")==0){
			this.setRestartButton();
		}else{
			this.setCancelButton();
		}
	}
	public void setCancelButton(){
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
				cancel.setText("Cancel");
				cancel.addSelectionListener(new SelectionListener(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						cancel();
					}
		
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing to do
						
					}
				});
            }
		});
	}
	public void setRestartButton(){
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
			cancel.setText("Restart");
			cancel.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					restartThis();
				}
	
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
					
				}
			});
            }
		});
	}
	public void restartThis(){
		this.status="Not started";
		monitor.setStatus(this.status);
		((FilesTransfer)dataType).restart(this);
	}
	public TransferProcess copyForRestart(){
		TransferProcess process=new TransferProcess(this.fileToTransfer, this.dataType);
		process.remotePath=this.remotePath;
		process.setBar(this.bar);
		process.setProgressLabel(this.progressLabel);
		process.setStatusLabel(this.statusLabel);
		process.setButton(this.cancel);
		return process;
	}
	public boolean checkFileExists(String name){
		if(c==null || !c.isConnected()) return false;
		 try{
		    @SuppressWarnings("rawtypes")
			Vector vv=c.ls(".");
		    if(vv!=null){
		      for(int ii=0; ii<vv.size(); ii++){
                Object obj=vv.elementAt(ii);
	                if(obj instanceof LsEntry){
	                  if(((LsEntry)obj).getFilename().compareTo(name)==0){
	                	  return true;
	                  }
	                }
		      }
		    }
		 }
		  catch(SftpException e){
		    e.printStackTrace();
		  }
		 return false;
	}
}

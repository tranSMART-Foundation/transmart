package fr.sanofi.fcl4transmart.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class FTPPreferences_back {
	private Text host;
	private Text port;
	private Text user;
	private Text pass;
	private Text max;
	private Text directory;
	private Text groupId;
	private static String staticHost;
	private static String staticPort;
	private static String staticUser;
	private static String staticPass;
	private static int staticMax;
	private static String staticDirectory;
	private static int staticGroupId;
	private ISecurePreferences node;
	private Shell shell;
	public FTPPreferences_back(){
		staticHost="";
		staticPort="";
		staticUser="";
		staticPass="";
		staticMax=2;
		staticDirectory="";
		staticGroupId=-1;
		try {
			ISecurePreferences securePref= SecurePreferencesFactory.getDefault();
	        node = securePref.node("ftp");
        
            staticHost=node.get("host", "");
            staticPort=node.get("port", "");
        	staticUser=node.get("user", "");
        	staticPass=node.get("password", "");
        	staticMax=node.getInt("max", 2);
        	staticDirectory=node.get("directory", "");
        	staticGroupId=node.getInt("groupId", -1);
        } catch (StorageException e1) {
        	e1.printStackTrace();
        }
	}
	
	@Execute
	public void execute(Display display) {		
		this.shell=new Shell(SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX | SWT.RESIZE);
	    this.shell.setMinimumSize(250,250);
	    this.shell.setText("FTP preferences");
	    GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		this.shell.setLayout(gridLayout);
						
		Composite body=new Composite(shell, SWT.NONE);
		gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		body.setLayout(gridLayout);
		GridData gridData=new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace=true;
		body.setLayoutData(gridData);
		
		Label lab1=new Label(body, SWT.NONE);
		lab1.setText("Host:");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab1.setLayoutData(gridData);
		this.host=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.host.setLayoutData(gridData);
		if(staticHost!=null && staticHost.compareTo("")!=0){
			this.host.setText(staticHost);
		}
		
		Label lab2=new Label(body, SWT.NONE);
		lab2.setText("Port:");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab2.setLayoutData(gridData);
		this.port=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.port.setLayoutData(gridData);
		if(staticPort!=null && staticPort.compareTo("")!=0){
			this.port.setText(staticPort);
		}
		
		Label lab3=new Label(body, SWT.NONE);
		lab3.setText("User:");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab3.setLayoutData(gridData);
		this.user=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.user.setLayoutData(gridData);
		if(staticUser!=null && staticUser.compareTo("")!=0){
			this.user.setText(staticUser);
		}
		
		Label lab4=new Label(body, SWT.NONE);
		lab4.setText("Password:");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab4.setLayoutData(gridData);
		this.pass=new Text(body, SWT.BORDER | SWT.PASSWORD);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.pass.setLayoutData(gridData);
		if(staticPass!=null && staticPass.compareTo("")!=0){
			this.pass.setText(staticPass);
		}
		
		Label labDir=new Label(body, SWT.WRAP);
		labDir.setText("Transfer directory");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		labDir.setLayoutData(gridData);
		this.directory=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.directory.setLayoutData(gridData);
		this.directory.setText(staticDirectory);
		
		Label labId=new Label(body, SWT.WRAP);
		labId.setText("Unix group identifier");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		labId.setLayoutData(gridData);
		this.groupId=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.groupId.setLayoutData(gridData);
		if(staticGroupId!=-1){
			this.groupId.setText(String.valueOf(staticGroupId));
		}
		
		//spacer
		@SuppressWarnings("unused")
		Label space1=new Label(body, SWT.NONE);
		@SuppressWarnings("unused")
		Label space2=new Label(body, SWT.NONE);
				
		Label lab5=new Label(body, SWT.WRAP);
		lab5.setText("Number of maximum simultaneous transfers:");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab5.setLayoutData(gridData);
		this.max=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.max.setLayoutData(gridData);
		this.max.setText(String.valueOf(staticMax));
		
		Composite buttonPart=new Composite(this.shell, SWT.NONE);
		GridLayout gl=new GridLayout();
		gl.numColumns=2;
		gl.horizontalSpacing=10;
		buttonPart.setLayout(gl);
		gridData=new GridData(GridData.FILL_BOTH);
		buttonPart.setLayoutData(gridData);
		
		Button ok=new Button(buttonPart, SWT.PUSH);
		ok.setText("OK");
		GridData gd=new GridData();
		gd.widthHint=100;
		ok.setLayoutData(gd);
		ok.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				
					if(host.getText().compareTo("")==0){
						displayMessage("Host is not set");
						return;
					}
					try {
			            node.put("host", host.getText(), true);
					 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
		            staticHost=host.getText();
		            try{
			            node.put("port", port.getText(), true);
		            } catch (StorageException e1) {
			            e1.printStackTrace();
			        }
		            staticPort=port.getText();
	            	try{
		            	node.put("user", user.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticUser=user.getText();
	            	try{
			            node.put("password", pass.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticPass=pass.getText();
	            	try{
			            node.put("directory", directory.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticDirectory=directory.getText();
		            
		            try{
		            	if(groupId.getText().compareTo("")==0){
		            		staticGroupId=-1;
		            		node.putInt("groupId", -1, true);
		            	}else{
			            	try{
			            		int n=Integer.valueOf(groupId.getText());
			            		staticGroupId=n;
			            		node.putInt("groupId", n, true);
			            	}catch(NumberFormatException nfe){
			            		displayMessage("Unix group identifier has to be a number");
			            		return;
			            	}
		            	}
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
		            
		            try{
		            	try{
		            		if(Integer.valueOf(max.getText())>10){
		            			displayMessage("Number of simultaneous transfers has to be less or equal than 10");
		            			return;
		            		}
				            staticMax=Integer.valueOf(max.getText());	            		
		            		node.putInt("max", Integer.valueOf(max.getText()), true);
		            	}catch(NumberFormatException nfe){
		            		displayMessage("Number of maximum simultaneous transfers has to be a number");
		            		return;
		            	}
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
				
				shell.close();
			}
		});
        
		Button cancel=new Button(buttonPart, SWT.PUSH);
		cancel.setText("Cancel");
		gd=new GridData();
		gd.widthHint=100;
		cancel.setLayoutData(gd);
		cancel.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				shell.close();
			}
		});
		
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		this.shell.open();
		while(!shell.isDisposed()){
	    	if (!display.readAndDispatch()) {
	            display.sleep();
	          }
	    }
	}
	
	public static String getHost(){
		return staticHost;
	}
	public static String getPort(){
		return staticPort;
	}
	public static String getUser(){
		return staticUser;
	}
	public static String getPass(){
		return staticPass;
	}
	public static int getMax(){
		return staticMax;
	}
	public static String getDirectory(){
		return staticDirectory;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	//get the number of simultaneous threads to transfer files
	public static int getNumberMax(){
		return 2;
	}
	public static int getGroupId(){
		return staticGroupId;
	}
}

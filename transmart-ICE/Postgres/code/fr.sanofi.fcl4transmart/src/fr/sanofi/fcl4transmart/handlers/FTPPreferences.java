package fr.sanofi.fcl4transmart.handlers;

import java.io.IOException;
import java.util.Vector;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public class FTPPreferences {
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
	private ISecurePreferences preferences;
	private ISecurePreferences ftpPreferences;
	private Shell shell;
	private Vector<String> servers;
	private static String staticSelectedName;
	private ListViewer viewer;
	private Composite preferencesPart;
	@Inject  private IEventBroker eventBroker;
	private Text saveNameField;
	public FTPPreferences(){
		staticHost="";
		staticPort="";
		staticUser="";
		staticPass="";
		staticMax=2;
		staticDirectory="";
		staticGroupId=-1;
		try {
			ISecurePreferences securePref= SecurePreferencesFactory.getDefault();
	        preferences = securePref.node("ftp");
	        
	        staticSelectedName=preferences.get("nameSelected", "");
	        boolean found=false;
	        String[] subPref;
			this.servers=new Vector<String>();
			subPref=this.preferences.childrenNames();
			this.servers.add("");
			for(int i=0; i<subPref.length; i++){
				if(subPref[i].compareTo("nameSelected")!=0){
					this.servers.add(subPref[i]);
					if(subPref[i].compareTo(staticSelectedName)==0) found=true;
				}
			}
			if(!found){
				staticSelectedName="";
			}else{
				ftpPreferences=preferences.node(staticSelectedName);
			}
        
			if(staticSelectedName.compareTo("")!=0){
	            staticHost=ftpPreferences.get("host", "");
	            staticPort=ftpPreferences.get("port", "");
	        	staticUser=ftpPreferences.get("user", "");
	        	staticPass=ftpPreferences.get("password", "");
	        	staticMax=ftpPreferences.getInt("max", 2);
	        	staticDirectory=ftpPreferences.get("directory", "");
	        	staticGroupId=ftpPreferences.getInt("groupId", -1);
			}else{
				staticHost="";
	            staticPort="";
	        	staticUser="";
	        	staticPass="";
	        	staticMax=2;
	        	staticDirectory="";
	        	staticGroupId=-1;
			}
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
		gridLayout.numColumns=2;
		this.shell.setLayout(gridLayout);
		
		Composite selectionPart=new Composite(this.shell, SWT.NONE);
	    selectionPart.setLayout(new GridLayout());
	    //fields
	   this.viewer=new ListViewer(selectionPart);

	   this.viewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("rawtypes")
				Vector v = (Vector)inputElement;
				return v.toArray();
			}
			public void dispose() {
				// nothing to do
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// nothing to do
			}
		});	
	   this.viewer.setInput(this.servers);
		for(int i=0; i<this.servers.size(); i++){
			if(this.servers.get(i).compareTo(staticSelectedName)==0){
				this.viewer.getList().setSelection(i);
			}
		}
		
		this.viewer.getList().addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selected=(String)viewer.getElementAt(viewer.getList().getSelectionIndex());
				try {
					preferences.put("nameSelected", selected, true);
				} catch (StorageException e1) {
					e1.printStackTrace();
				}
				ftpPreferences=preferences.node(selected);
				staticSelectedName=selected;				
				try {
					preferences.flush();
					}
				catch (IOException e1) {
					e1.printStackTrace();
				}
				preferencesPart.dispose();
				preferencesPart=changePrefPart();
				GridData data = new GridData(SWT.FILL, SWT.NONE, false, false);	    
				data.horizontalSpan=1;
				data.verticalSpan=1;
				preferencesPart.setLayoutData(data);		    
				shell.layout(true, true);					
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
			}
		});
		GridData gridData=new GridData(GridData.FILL_BOTH);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=125;
		gridData.grabExcessVerticalSpace=true;
		this.viewer.getControl().setLayoutData(gridData);
	   
		this.preferencesPart=this.changePrefPart();
	    
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		this.shell.open();
	    
	    while(!shell.isDisposed()){
	    	if (!display.readAndDispatch()) {
	            display.sleep();
	          }
	    }
	    eventBroker.send("preferencesChanged/syncEvent", "Preferences changed");
	}
	
		
	/**
	 *Change the preferences part of the shell if another ftp server is selected
	 */
	private Composite changePrefPart(){			
		Composite prefPart=new Composite(shell, SWT.NONE);
		GridLayout gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=0;
		prefPart.setLayout(gd);
		GridData gridData=new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace=true;
		prefPart.setLayoutData(gridData);
		
		Composite body=new Composite(prefPart, SWT.NONE);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		body.setLayout(gridLayout);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace=true;
		body.setLayoutData(gridData);
		
		Label saveNameLabel=new Label(body, SWT.NONE);
		saveNameLabel.setText("Save name: ");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		saveNameLabel.setLayoutData(gridData);
		this.saveNameField=new Text(body, SWT.BORDER);
		this.saveNameField.setText(staticSelectedName);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		saveNameField.setLayoutData(gridData);
		
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
		if(staticSelectedName.compareTo("")==0){
			this.host.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticHost=ftpPreferences.get("host", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
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
		if(staticSelectedName.compareTo("")==0){
			this.port.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticPort=ftpPreferences.get("port", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
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
		if(staticSelectedName.compareTo("")==0){
			this.user.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticUser=ftpPreferences.get("user", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
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
		if(staticSelectedName.compareTo("")==0){
			this.pass.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticPass=ftpPreferences.get("password", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
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
		if(staticSelectedName.compareTo("")==0){
			this.directory.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticDirectory=ftpPreferences.get("directory", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.directory.setText(staticDirectory);
		}
		
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
		if(staticSelectedName.compareTo("")==0){
			this.groupId.setText("");
		}else{
			try {
				if(ftpPreferences!=null) staticGroupId=ftpPreferences.getInt("groupId", -1);
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
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
		if(staticSelectedName.compareTo("")==0){
			this.max.setText("2");
		}else{
			try {
				if(ftpPreferences!=null) staticMax=ftpPreferences.getInt("max", 2);
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.max.setText(String.valueOf(staticMax));
		}
		
		Composite buttonPart=new Composite(prefPart, SWT.NONE);
		GridLayout gl=new GridLayout();
		gl.numColumns=2;
		gl.horizontalSpacing=10;
		buttonPart.setLayout(gl);
		gridData=new GridData(GridData.FILL_BOTH);
		buttonPart.setLayoutData(gridData);
		
		Button ok=new Button(buttonPart, SWT.PUSH);
		ok.setText("Save");
		gridData=new GridData();
		gridData.widthHint=100;
		ok.setLayoutData(gridData);
		ok.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				if(saveNameField.getText().compareTo("")==0){
				    int style = SWT.ICON_WARNING | SWT.OK;
				    MessageBox messageBox = new MessageBox(new Shell(), style);
				    messageBox.setMessage("Please fill the save name");
				    messageBox.open();
				    return;
				}
				boolean found=false;
				for(String s: servers){
					if(s.compareTo(saveNameField.getText())==0){
						found=true;
					}
				}
				if(!found){
					servers.add(saveNameField.getText());
				}
				viewer.setInput(servers);
				for(int i=0; i<servers.size(); i++){
					if(servers.get(i).compareTo(saveNameField.getText())==0){
						staticSelectedName=saveNameField.getText();
						viewer.getList().setSelection(i);
						ftpPreferences=preferences.node(staticSelectedName);
						try {
							preferences.put("nameSelected", staticSelectedName, true);
						} catch (StorageException e) {
							e.printStackTrace();
						}
						try {
							preferences.flush();
							}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
					if(host.getText().compareTo("")==0){
						displayMessage("Host is not set");
						return;
					}
					try {
			            ftpPreferences.put("host", host.getText(), true);
					 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
		            staticHost=host.getText();
		            try{
		            	ftpPreferences.put("port", port.getText(), true);
		            } catch (StorageException e1) {
			            e1.printStackTrace();
			        }
		            staticPort=port.getText();
	            	try{
	            		ftpPreferences.put("user", user.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticUser=user.getText();
	            	try{
	            		ftpPreferences.put("password", pass.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticPass=pass.getText();
	            	try{
	            		ftpPreferences.put("directory", directory.getText(), true);
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
	            	staticDirectory=directory.getText();
		            
		            try{
		            	if(groupId.getText().compareTo("")==0){
		            		staticGroupId=-1;
		            		ftpPreferences.putInt("groupId", -1, true);
		            	}else{
			            	try{
			            		int n=Integer.valueOf(groupId.getText());
			            		staticGroupId=n;
			            		ftpPreferences.putInt("groupId", n, true);
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
				            ftpPreferences.putInt("max", Integer.valueOf(max.getText()), true);
		            	}catch(NumberFormatException nfe){
		            		displayMessage("Number of maximum simultaneous transfers has to be a number");
		            		return;
		            	}
	            	 } catch (StorageException e1) {
				            e1.printStackTrace();
				     }
			}
		});
        
		Button load=new Button(buttonPart, SWT.PUSH);
	    load.setText("OK");
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		load.setLayoutData(gridData);
	    load.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
			}
	    	
	    });
		
		return prefPart;
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

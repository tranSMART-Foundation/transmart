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
public class etlPreferences {
	private Text host;
	private Text port;
	private Text user;
	private Text pass;
	private Text kettleDirectory;
	private Text jobsDirectory;
	private Text filesDirectory;
	private static String staticHost;
	private static String staticPort;
	private static String staticUser;
	private static String staticPass;
	private static String staticKettleDirectory;
	private static String staticJobsDirectory;
	private static String staticFilesDirectory;
	private ISecurePreferences preferences;
	private ISecurePreferences etlPreferences;
	private Shell shell;
	private Vector<String> servers;
	private static String staticSelectedName;
	private ListViewer viewer;
	private Composite preferencesPart;
	@Inject  private IEventBroker eventBroker;
	private Text saveNameField;
	public etlPreferences(){
		staticHost="";
		staticPort="";
		staticUser="";
		staticPass="";
		staticKettleDirectory="";
		staticJobsDirectory="";
		staticFilesDirectory="";
		try {
			ISecurePreferences securePref= SecurePreferencesFactory.getDefault();
	        preferences = securePref.node("etl");
	        
	        staticSelectedName=preferences.get("nameSelected", "");
	        boolean found=false;
	        String[] subPref;
			this.servers=new Vector<String>();
			subPref=this.preferences.childrenNames();
			this.servers.add("");
			for(int i=0; i<subPref.length; i++){
				if(subPref[i]!=null && staticSelectedName!=null && subPref[i].compareTo("nameSelected")!=0){
					this.servers.add(subPref[i]);
					if(subPref[i].compareTo(staticSelectedName)==0) found=true;
				}
			}
			if(!found){
				staticSelectedName="";
			}else{
				etlPreferences=preferences.node(staticSelectedName);
			}
        
			if(staticSelectedName.compareTo("")!=0){	        
	            staticHost=etlPreferences.get("host", "");
	            staticPort=etlPreferences.get("port", "");
	        	staticUser=etlPreferences.get("user", "");
	        	staticPass=etlPreferences.get("password", "");
	        	staticKettleDirectory=etlPreferences.get("kettleDirectory", "");
	        	staticJobsDirectory=etlPreferences.get("jobsDirectory", "");
	        	staticFilesDirectory=etlPreferences.get("filesDirectory", "");
			}else{
				staticHost="";
	            staticPort="";
	        	staticUser="";
	        	staticPass="";
	        	staticKettleDirectory="";
	        	staticJobsDirectory="";
	        	staticFilesDirectory="";
			}
        } catch (StorageException e1) {
        	e1.printStackTrace();
        }
	}
	
	@Execute
	public void execute(Display display) {		
		this.shell=new Shell(SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX | SWT.RESIZE);
	    this.shell.setMinimumSize(250,250);
	    this.shell.setText("ETL server preferences");
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
			if(this.servers.get(i)!=null && this.servers.get(i).compareTo(staticSelectedName)==0){
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
				etlPreferences=preferences.node(selected);
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
	 *Change the preferences part of the shell if another etl server is selected
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
				if(etlPreferences!=null) staticHost=etlPreferences.get("host", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			if(staticHost!=null && staticHost.compareTo("")!=0){
				this.host.setText(staticHost);
			}
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
				if(etlPreferences!=null) staticPort=etlPreferences.get("port", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			if(staticPort!=null && staticPort.compareTo("")!=0){
				this.port.setText(staticPort);
			}
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
				if(etlPreferences!=null) staticUser=etlPreferences.get("user", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			if(staticUser!=null && staticUser.compareTo("")!=0){
				this.user.setText(staticUser);
			}
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
				if(etlPreferences!=null) staticUser=etlPreferences.get("user", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			if(staticPass!=null && staticPass.compareTo("")!=0){
				this.pass.setText(staticPass);
			}
		}
		
		Label labDir1=new Label(body, SWT.WRAP);
		labDir1.setText("Kettle directory");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		labDir1.setLayoutData(gridData);
		this.kettleDirectory=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.kettleDirectory.setLayoutData(gridData);
		if(staticSelectedName.compareTo("")==0){
			this.kettleDirectory.setText("");
		}else{
			try {
				if(etlPreferences!=null) staticKettleDirectory=etlPreferences.get("kettleDirectory", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.kettleDirectory.setText(staticKettleDirectory);
		}
		
		Label labDir2=new Label(body, SWT.WRAP);
		labDir2.setText("Kettle jobs directory");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		labDir2.setLayoutData(gridData);
		this.jobsDirectory=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.jobsDirectory.setLayoutData(gridData);
		if(staticSelectedName.compareTo("")==0){
			this.jobsDirectory.setText("");
		}else{
			try {
				if(etlPreferences!=null) staticJobsDirectory=etlPreferences.get("jobsDirectory", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.jobsDirectory.setText(staticJobsDirectory);
		}
		
		Label labDir4=new Label(body, SWT.WRAP);
		labDir4.setText("Temporary directory (store data files)");
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		labDir4.setLayoutData(gridData);
		this.filesDirectory=new Text(body, SWT.BORDER);
		gridData=new GridData(GridData.FILL_BOTH);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.filesDirectory.setLayoutData(gridData);
		if(staticSelectedName.compareTo("")==0){
			this.filesDirectory.setText("");
		}else{
			try {
				if(etlPreferences!=null) staticFilesDirectory=etlPreferences.get("filesDirectory", "");
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.filesDirectory.setText(staticFilesDirectory);
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
						etlPreferences=preferences.node(staticSelectedName);
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
		            etlPreferences.put("host", host.getText(), true);
				 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
	            staticHost=host.getText();
	            try{
	            	try{
	            		@SuppressWarnings("unused")
						int n=Integer.valueOf(port.getText());
	            	}catch(NumberFormatException nfe){
	            		displayMessage("Port has to be a number");
	            		return;
	            	}
	            	etlPreferences.put("port", port.getText(), true);
	            } catch (StorageException e1) {
		            e1.printStackTrace();
		        }
	            staticPort=port.getText();
            	try{
            		etlPreferences.put("user", user.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
            	staticUser=user.getText();
            	try{
            		etlPreferences.put("password", pass.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
            	staticPass=pass.getText();
            	try{
            		etlPreferences.put("kettleDirectory", kettleDirectory.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
            	staticKettleDirectory=kettleDirectory.getText();
            	
            	try{
            		etlPreferences.put("jobsDirectory", jobsDirectory.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
            	staticJobsDirectory=jobsDirectory.getText();
            	
            	try{
            		etlPreferences.put("filesDirectory", filesDirectory.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
            	staticFilesDirectory=filesDirectory.getText();
            	
			}
		});
        
		Button cancel=new Button(buttonPart, SWT.PUSH);
		cancel.setText("OK");
		gridData=new GridData();
		gridData.widthHint=100;
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				shell.close();
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
	public static String getKettleDirectory(){
		return staticKettleDirectory;
	}
	public static String getJobsDirectory(){
		return staticJobsDirectory;
	}
	public static String getFilesDirectory(){
		return staticFilesDirectory;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

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
public class transmartPreferences {
	private Text adress;
	private static String staticAdress;
	private ISecurePreferences preferences;
	private ISecurePreferences transmartPreferences;
	private Shell shell;
	private Vector<String> adresses;
	private static String staticSelectedName;
	private ListViewer viewer;
	private Composite preferencesPart;
	@Inject  private IEventBroker eventBroker;
	private Text saveNameField;
	public transmartPreferences(){
		staticAdress="";
		try {
			ISecurePreferences securePref= SecurePreferencesFactory.getDefault();
	        preferences = securePref.node("transmart");
	        
	        staticSelectedName=preferences.get("nameSelected", "");
	        boolean found=false;
	        String[] subPref;
			this.adresses=new Vector<String>();
			subPref=this.preferences.childrenNames();
			this.adresses.add("");
			for(int i=0; i<subPref.length; i++){
				if(subPref[i].compareTo("nameSelected")!=0){
					this.adresses.add(subPref[i]);
					if(subPref[i].compareTo(staticSelectedName)==0) found=true;
				}
			}
			if(!found){
				staticSelectedName="";
			}else{
				transmartPreferences=preferences.node(staticSelectedName);
			}
			if(staticSelectedName.compareTo("")!=0){
				staticAdress=transmartPreferences.get("adress", "");
			}else{
				staticAdress="";
			}
        } catch (StorageException e1) {
        	e1.printStackTrace();
        }
	}
	
	@Execute
	public void execute(Display display) {		
		this.shell=new Shell(SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX | SWT.RESIZE);
	    this.shell.setMinimumSize(250,250);
	    this.shell.setText("tranSAMRT preferences");
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
	   this.viewer.setInput(this.adresses);
		for(int i=0; i<this.adresses.size(); i++){
			if(this.adresses.get(i).compareTo(staticSelectedName)==0){
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
				transmartPreferences=preferences.node(selected);
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
	 *Change the preferences part of the shell if another transmart adress is selected
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
		
		Label lab6=new Label(body, SWT.WRAP);
		lab6.setText("tranSMART application adress:");
		gridData=new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=false;
		lab6.setLayoutData(gridData);
		
		this.adress=new Text(body, SWT.BORDER );
		gridData=new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace=true;
		this.adress.setLayoutData(gridData);
		if(staticSelectedName.compareTo("")==0){
			this.adress.setText("");
		}else{
			try {
				if(transmartPreferences!=null){
					staticAdress=transmartPreferences.get("adress", "");
				}
			} catch (StorageException e2) {
				e2.printStackTrace();
			}
			this.adress.setText(staticAdress);
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
				for(String s: adresses){
					if(s.compareTo(saveNameField.getText())==0){
						found=true;
					}
				}
				if(!found){
					adresses.add(saveNameField.getText());
				}
				viewer.setInput(adresses);
				for(int i=0; i<adresses.size(); i++){
					if(adresses.get(i).compareTo(saveNameField.getText())==0){
						staticSelectedName=saveNameField.getText();
						viewer.getList().setSelection(i);
						transmartPreferences=preferences.node(staticSelectedName);
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
            	try{
		            transmartPreferences.put("adress", adress.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
	            staticAdress=adress.getText(); 
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

	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

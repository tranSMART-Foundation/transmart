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
public class transmartPreferences_back {
	private Text adress;
	private static String staticAdress;
	private ISecurePreferences node;
	private Shell shell;
	public transmartPreferences_back(){
		staticAdress="";
		try {
			ISecurePreferences securePref= SecurePreferencesFactory.getDefault();
	        node = securePref.node("transmart");
        	staticAdress=node.get("adress", "");
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
		this.adress.setText(String.valueOf(staticAdress));
		
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
            	try{
		            node.put("adress", adress.getText(), true);
            	 } catch (StorageException e1) {
			            e1.printStackTrace();
			     }
	            staticAdress=adress.getText(); 
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

	public static String getAdress(){
		String adress=staticAdress;
		if(adress.lastIndexOf("/")==adress.length()-1) adress=adress.substring(0, adress.length()-1); //trim last backslash if needed
		return adress;
	}

	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

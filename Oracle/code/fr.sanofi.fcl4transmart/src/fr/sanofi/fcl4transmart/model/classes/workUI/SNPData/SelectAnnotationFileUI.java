package fr.sanofi.fcl4transmart.model.classes.workUI.SNPData;

import java.io.File;
import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import fr.sanofi.fcl4transmart.controllers.listeners.snpData.SelectAnnotationFileListener;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectAnnotationFileUI implements WorkItf {
	private DataTypeItf dataType;
	private Text pathField;
	private String path;
	public SelectAnnotationFileUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	@Override
	public Composite createUI(Composite parent) {
		
		Composite composite=new Composite(parent, SWT.NONE);
		GridLayout gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=0;
		composite.setLayout(gd);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		ScrolledComposite scroller=new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=0;
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
				
		Composite pathPart=new Composite(scrolledComposite, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=3;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		pathPart.setLayout(gd);
		
		Label pathLabel=new Label(pathPart, SWT.NONE);
		pathLabel.setText("Path: ");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		pathLabel.setLayoutData(gridData);
		
		this.pathField=new Text(pathPart, SWT.BORDER);
		this.pathField.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				path=pathField.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace = true;
		this.pathField.setLayoutData(gridData);
		
		Button browse=new Button(pathPart, SWT.PUSH);
		browse.setText("Browse");
		browse.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				FileDialog fd=new FileDialog(new Shell(), SWT.SINGLE);
				fd.open();
				String filename=fd.getFileName();
				String filterPath=fd.getFilterPath(); 
				path="";
				if(filterPath!=null && filterPath.trim().length()>0){
					path=filterPath+File.separator+filename;
				}
				else{
					path=filename;
				}
				
				pathField.setText(path);
			}				
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		browse.setLayoutData(gridData);
		
		Button add=new Button(scrolledComposite, SWT.PUSH);
		add.setText("Add files");
		add.addListener(SWT.Selection, new SelectAnnotationFileListener(this.dataType, this));


		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}

	@Override
	public boolean canCopy() {
		return false;
	}

	@Override
	public boolean canPaste() {
		return false;
	}

	@Override
	public Vector<Vector<String>> copy() {
		return null;
	}

	@Override
	public void paste(Vector<Vector<String>> data) {
		// nothing to do

	}

	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		// nothing to do

	}
	public String getPath(){
		return this.pathField.getText();
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}

}

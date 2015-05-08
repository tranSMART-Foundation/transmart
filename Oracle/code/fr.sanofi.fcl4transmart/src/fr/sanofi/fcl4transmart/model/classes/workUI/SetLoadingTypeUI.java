package fr.sanofi.fcl4transmart.model.classes.workUI;


import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import fr.sanofi.fcl4transmart.model.classes.dataType.IncrementalLoadingPossibleItf;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetLoadingTypeUI implements WorkItf {
	private IncrementalLoadingPossibleItf dataType;
	private Button incremental;
	private Label incrementalLabel;
	private Combo incrementalCombo;
	private Button ok;
	public SetLoadingTypeUI(DataTypeItf dataType){
		this.dataType=(IncrementalLoadingPossibleItf)dataType;
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
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing=20;
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		boolean isIncremental=dataType.getSubFolders().size()>0;
		this.incremental=new Button(scrolledComposite, SWT.CHECK);  
		this.incremental.setText("Do you want to load data incrementally?");
		this.incremental.setSelection(isIncremental);
		this.incremental.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				boolean bool=incremental.getSelection();
				incrementalCombo.setEnabled(bool);
				if(bool) incrementalLabel.setForeground(WorkPart.display().getSystemColor(SWT.COLOR_BLACK));
				else incrementalLabel.setForeground(WorkPart.display().getSystemColor(SWT.COLOR_GRAY));
			}
		});
		
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=2;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		body.setLayout(gd);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.incrementalLabel=new Label(body, SWT.NONE);
		if(isIncremental) this.incrementalLabel.setForeground(WorkPart.display().getSystemColor(SWT.COLOR_BLACK));
		else this.incrementalLabel.setForeground(WorkPart.display().getSystemColor(SWT.COLOR_GRAY));
		this.incrementalLabel.setText("Choose a subfolder or create a new one to store your incremental data.");
		
		this.incrementalCombo=new Combo(body, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		for(File f: dataType.getSubFolders()){
			this.incrementalCombo.add(f.getName());
		}
		this.incrementalCombo.setEnabled(isIncremental);
		if(dataType.isIncremental()) this.incrementalCombo.setText(((DataTypeItf)dataType).getPath().getName());
		
		ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		GridData gridData = new GridData();
		gridData.widthHint=100;
		ok.setLayoutData(gridData);
		ok.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				try{
					if(!incremental.getSelection()){
						if(dataType.isIncremental()) dataType.setSubFolder("");
						displayMessage("You will now use main clinical folder");
					}else{
						if(incrementalCombo.getText().compareTo("")==0){
							displayMessage("The subfolder name is mandatory");
							return;
						}
						if(incrementalCombo.getText().contains(" ")){
							displayMessage("The subfolder name cannot contain spaces");
							return;
						}
						dataType.setSubFolder(incrementalCombo.getText());
						displayMessage("You will now use subfolder "+incrementalCombo.getText());
					}
					WorkPart.updateSteps();
					WorkPart.updateFiles();
					WorkPart.filesChanged((DataTypeItf)dataType);
				}catch(IOException e){
					displayMessage("The subfolder cannot be created");
					return;
				}
			}
			
		});
		
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
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
		//nothing to do
	}

	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		//nothing to do
	}

}

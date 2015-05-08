/*******************************************************************************
 * Copyright (c) 2012 Sanofi-Aventis Recherche et Developpement.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    Sanofi-Aventis Recherche et Developpement - initial API and implementation
 ******************************************************************************/
package fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData;

import java.io.File;
import org.eclipse.jface.dialogs.MessageDialog;
import java.util.Vector;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.RemoveRawFileListener;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.SelectClinicalRawFileListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class allows the creation of the composite to select clinical raw data files
 */
public class SelectRawFilesUI implements WorkItf{
	private DataTypeItf dataType;
	private Text pathField;
	private String path;
	private Combo fileTypeField;
	private ListViewer viewer;
	private boolean isLoading;
	private Display display;
	private Shell loadingShell;
	private String format;
	private String message="";
	private Composite scrolledComposite;
	private Composite filterPart;
	private Vector<Combo> columnsCombo;
	private Vector<String> columns;
	private Composite subFilterPart;
	private Vector<Button> buttons;
	private Vector<String> filters;
	public SelectRawFilesUI(DataTypeItf dataType){
		this.dataType=dataType;
		this.path="";
		this.format="";
	}
	@Override
	public Composite createUI(Composite parent){

   		this.display=WorkPart.display();
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
		
		scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite pathPart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		pathPart.setLayout(layout);
		pathPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
				FileDialog fd=new FileDialog(new Shell(), SWT.MULTI);
				fd.open();
				String[] filenames=fd.getFileNames();
				String filterPath=fd.getFilterPath(); 
				path="";
				for(int i=0; i<filenames.length; i++){
					if(path.compareTo("")==0){
						if(filterPath!=null && filterPath.trim().length()>0){
							path+=filterPath+File.separator+filenames[i];
						}
						else{
							path+=filenames[i];
						}
					}
					else{
						if(filterPath!=null && filterPath.trim().length()>0){
							path+="?"+filterPath+File.separator+filenames[i];
						}
						else{
							path+="?"+filenames[i];
						}
					}
				}
				pathField.setText(path);
			}		
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		browse.setLayoutData(gridData);
		
		Composite fileTypePart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		fileTypePart.setLayout(layout);
		Label fileTypeLabel=new Label(fileTypePart, SWT.NONE);
		fileTypeLabel.setText("Format: ");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileTypeLabel.setLayoutData(gridData);
		this.fileTypeField=new Combo(fileTypePart, SWT.DROP_DOWN | SWT.BORDER );
	    this.fileTypeField.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	}); 
		this.fileTypeField.add("Tab delimited raw file");
		this.fileTypeField.add("Tab delimited raw file with filter");
		this.fileTypeField.add("SOFT");
		this.fileTypeField.add("CSV");
		this.fileTypeField.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(fileTypeField.getSelectionIndex()==-1){
					format="";
				}
				else{
					format=fileTypeField.getItem(fileTypeField.getSelectionIndex());
					if(format.compareTo("Tab delimited raw file with filter")==0){
						if(filterPart!=null){
							fillFilterPart();
							filterPart.setVisible(true);
							scrolledComposite.layout(true, true);	
							scrolledComposite.getParent().layout(true, true);
							scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						}
					}else{
						if(filterPart!=null){
							for(int i=0; i<filterPart.getChildren().length; i++){
								filterPart.getChildren()[i].dispose();
							}
							filterPart.setVisible(false);
							scrolledComposite.layout(true, true);	
							scrolledComposite.getParent().layout(true, true);
							scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						}
					}
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing to do
				
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=120;
		this.fileTypeField.setLayoutData(gridData);
		
		this.filterPart=new Composite(scrolledComposite, SWT.BORDER);
		layout = new GridLayout();
		layout.numColumns = 2;
		filterPart.setLayout(layout);
		filterPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		filterPart.setVisible(false);
		
		Button add=new Button(scrolledComposite, SWT.PUSH);
		add.setText("Add files");
		add.addListener(SWT.Selection, new SelectClinicalRawFileListener(this, this.dataType));
		
		Label filesLabel=new Label(scrolledComposite, SWT.NONE);
		filesLabel.setText("\nRaw data files:");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		filesLabel.setLayoutData(gridData);
		
		Composite filesPart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		filesPart.setLayout(layout);
		
		this.viewer=new ListViewer(filesPart);
		this.viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

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
		this.viewer.setInput(((ClinicalData)this.dataType).getRawFiles());
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		gridData.heightHint=125;
		this.viewer.getControl().setLayoutData(gridData);
		this.displayNames();
		
		Button remove=new Button(filesPart, SWT.PUSH);
		remove.setText("Remove selected files");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		remove.setLayoutData(gridData);
		remove.addListener(SWT.Selection, new RemoveRawFileListener(this.dataType, this));

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	private void fillFilterPart(){
		for(int i=0; i<filterPart.getChildren().length; i++){
			filterPart.getChildren()[i].dispose();
		}
		
		Composite composite=new Composite(filterPart, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		this.subFilterPart=new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		this.subFilterPart.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.subFilterPart.setLayoutData(gridData);
		
		columns=new Vector<String>();
		String[] paths=this.path.split(String.valueOf(File.pathSeparatorChar), -1);
		for(int i=0; i<paths.length; i++){
			for(String s: FileHandler.getHeaders(new File(paths[i]))){
				this.columns.add(s);
			}
		}
		
		if(columns.size()==0){
			Label lab=new Label(subFilterPart, SWT.NONE);
			lab.setText("There is no selected file");
			return;
		}
		Label lab=new Label(subFilterPart, SWT.NONE);
		lab.setText("Choose the columns you want to filter");
		
		@SuppressWarnings("unused")
		Label space=new Label(subFilterPart, SWT.NONE);
		
		columnsCombo=new Vector<Combo>();
		Combo combo=new Combo(subFilterPart, SWT.DROP_DOWN | SWT.BORDER );
		gridData = new GridData();
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
	    combo.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	}); 
	    combo.addListener(SWT.Selection, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		filters=new Vector<String>();
	    		for(Combo c: columnsCombo){
	    			filters.add(c.getText());
	    		}
	    	} 
    	}); 

	    for(String s: this.columns){
	    	combo.add(s);
	    }
		columnsCombo.add(combo);
		
		@SuppressWarnings("unused")
		Label space2=new Label(subFilterPart, SWT.NONE);
		
		this.buttons=new Vector<Button>();
		Button addCombo=new Button(composite, SWT.PUSH);
		addCombo.setText("Add a column to filter");
		addCombo.addListener(SWT.Selection, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		Combo combo=new Combo(subFilterPart, SWT.DROP_DOWN | SWT.BORDER );
	    		GridData gridData = new GridData();
	    		gridData.widthHint=150;
	    		gridData.grabExcessHorizontalSpace = true;
	    		combo.setLayoutData(gridData);
	    	    combo.addListener(SWT.KeyDown, new Listener(){ 
	    	    	public void handleEvent(Event event) { 
	    	    		event.doit = false; 
	    	    	} 
	        	}); 
	    	    combo.addListener(SWT.Selection, new Listener(){ 
	    	    	public void handleEvent(Event event) { 
	    	    		filters=new Vector<String>();
	    	    		for(Combo c: columnsCombo){
	    	    			filters.add(c.getText());
	    	    		}
	    	    	} 
	        	}); 
	    	    for(String s: columns){
	    	    	combo.add(s);
	    	    }
	    		columnsCombo.add(combo);   
								
	    		Button remove=new Button(subFilterPart, SWT.PUSH);
				remove.setText("Remove filter");
				buttons.add(remove);
				remove.addSelectionListener(new SelectionListener(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						int n=buttons.indexOf((Button)e.getSource());
						columnsCombo.get(n+1).dispose();
						buttons.get(n).dispose();
						scrolledComposite.layout(true, true);	
						scrolledComposite.getParent().layout(true, true);
						scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT)); 
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// nothing to do
						
					}
				});
	    		
	    		scrolledComposite.layout(true, true);	
				scrolledComposite.getParent().layout(true, true);
				scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT)); 
	    	} 
    	}); 

	}
	public String getPath(){
		return this.path;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public boolean confirm(String message){
		return MessageDialog.openConfirm(new Shell(), "Confirm", message);
	}
	public String getFormat(){
		return this.format;
	}
	public void displayNames(){
		for(int i=0; i<this.viewer.getList().getItemCount(); i++){
			this.viewer.getList().setItem(i, ((File)this.viewer.getElementAt(i)).getName());
		}
	}
	public void updateViewer(){
		this.viewer.setInput(((ClinicalData)this.dataType).getRawFiles());
		this.displayNames();
	}
	public Vector<File> getSelectedRemovedFile(){
			Vector<File> files=new Vector<File>();
			String[] paths=this.viewer.getList().getSelection();
			for(int i=0; i<paths.length; i++){
				if(((ClinicalData)this.dataType).getRawFilesNames().contains(paths[i])){
					files.add(new File(((ClinicalData)this.dataType).getPath()+File.separator+paths[i]));
				}
			}
			return files; 
	}
	public void openLoadingShell(){
		this.isLoading=true;
		this.message="";
		this.loadingShell=new Shell(this.display);
		this.loadingShell.setSize(50, 100);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		this.loadingShell.setLayout(gridLayout);
		ProgressBar pb = new ProgressBar(this.loadingShell, SWT.HORIZONTAL | SWT.INDETERMINATE);
		pb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searching=new Label(this.loadingShell, SWT.NONE);
		searching.setText("Creating new file...");
		this.loadingShell.open();
	}
	public void waitForThread(){
        while(this.isLoading){
        	if (!this.display.readAndDispatch()) {
                this.display.sleep();
              }	
        }
        this.loadingShell.close();
        if(this.message.compareTo("")!=0){
        	this.displayMessage(message);
        }
	}
	public void setMessage(String message){
		this.message=message;
	}
	public void setIsLoading(boolean bool){
		this.isLoading=bool;
	}
	public Vector<String> getFilters(){
		return filters;
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
}

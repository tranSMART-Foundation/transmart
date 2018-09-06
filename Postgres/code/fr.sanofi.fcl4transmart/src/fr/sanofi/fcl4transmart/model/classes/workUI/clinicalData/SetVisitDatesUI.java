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
import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.SetDatesListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class allows the creation of the composite to select a column mapping file
 */
public class SetVisitDatesUI implements WorkItf{
	private DataTypeItf dataType;
	private Combo enrollFilesCombo;
	private Vector<String> enrollFiles;
	private Combo enrollColumnsCombo;
	private Vector<String> enrollColumns;
	private String selectedEnrollFile;
	private String selectedEnrollColumn;
	private Text enrollFormat;
	private String selectedEnrollFormat;
	private Vector<Combo> dateFields;
	private Vector<String> dates;
	private Vector<Text> formatFields;
	private Vector<String> formats;
	public SetVisitDatesUI(DataTypeItf dataType){
		this.dataType=dataType;
		this.selectedEnrollColumn="";
		this.selectedEnrollFile="";
		this.selectedEnrollFormat="";
	}
	@Override
	public Composite createUI(Composite parent){
		this.dateFields=new Vector<Combo>();
		this.formatFields=new Vector<Text>();
		this.initiate();
		Composite composite=new Composite(parent, SWT.NONE);
		GridLayout gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=0;
		composite.setLayout(gd);
		
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
		
		Label labEnroll=new Label(scrolledComposite, SWT.NONE);
		labEnroll.setText("Enroll Dates");
		
		Composite enrollPart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		enrollPart.setLayout(layout);
		enrollPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		enrollFiles=new Vector<String>();
		for(File f: ((ClinicalData)this.dataType).getRawFiles()){
			enrollFiles.add(f.getName());
		}
		
		Label labEnrollFiles=new Label(enrollPart, SWT.NONE);
		labEnrollFiles.setText("File: ");
		
		enrollFilesCombo=new Combo(enrollPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		enrollFilesCombo.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	});
		enrollFilesCombo.add("");
		for(String s: enrollFiles){
			enrollFilesCombo.add(s);
		}
	   	
	   	GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		enrollFilesCombo.setLayoutData(gridData);
		
		if(selectedEnrollFile!=null && selectedEnrollFile.compareTo("")!=0){
			enrollFilesCombo.setText(selectedEnrollFile);
		}
		
		Label labEnrollColumns=new Label(enrollPart, SWT.NONE);
		labEnrollColumns.setText("Column: ");
		
		enrollColumnsCombo=new Combo(enrollPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		enrollColumnsCombo.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	});
		
		enrollFilesCombo.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				selectedEnrollFile=enrollFilesCombo.getText();
				if(selectedEnrollFile==null || selectedEnrollFile.compareTo("")==0){
					enrollColumnsCombo.removeAll();
					enrollColumnsCombo.add("");
				}else{
					enrollColumns=FileHandler.getHeaders(new File(dataType.getPath()+File.separator+selectedEnrollFile));
				
					enrollColumnsCombo.removeAll();
					for(String s: enrollColumns){
						enrollColumnsCombo.add(s);
					}
				}
			}
		});
		if(this.selectedEnrollFile.compareTo("")==0){
			enrollColumnsCombo.add("");
		}else{
			enrollColumns=FileHandler.getHeaders(new File(dataType.getPath()+File.separator+selectedEnrollFile));
			for(String s: enrollColumns){
				enrollColumnsCombo.add(s);
			}
		}
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		enrollColumnsCombo.setLayoutData(gridData);
		
		enrollColumnsCombo.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				selectedEnrollColumn=enrollColumnsCombo.getText();
			}
		});
		
		if(selectedEnrollColumn!=null && selectedEnrollColumn.compareTo("")!=0){
			enrollColumnsCombo.setText(selectedEnrollColumn);
		}
		
		Label labEnrollFormat=new Label(enrollPart, SWT.NONE);
		labEnrollFormat.setText("Date format: ");
		
		enrollFormat=new Text(enrollPart, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		enrollFormat.setLayoutData(gridData);
		
		if(selectedEnrollFormat!=null && selectedEnrollFormat.compareTo("")!=0){
			enrollFormat.setText(selectedEnrollFormat);
		}
		
		@SuppressWarnings("unused")
		Label spacer=new Label(scrolledComposite, SWT.NONE);
		
		Label labVisit=new Label(scrolledComposite, SWT.NONE);
		labVisit.setText("Visit Dates");
		
		Vector<File> files=((ClinicalData)this.dataType).getRawFiles();
		for(int i=0; i<files.size(); i++){
			Label title=new Label(scrolledComposite, SWT.NONE);
			title.setText(files.elementAt(i).getName());
			
			Composite fieldsPart=new Composite(scrolledComposite, SWT.NONE);
			GridLayout gridLayout=new GridLayout();
			gridLayout.numColumns=2;
			fieldsPart.setLayout(gridLayout);
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			fieldsPart.setLayoutData(gridData);
			
			//site
			Label dateLabel=new Label(fieldsPart, SWT.NONE);
			dateLabel.setText("Column: ");
			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			dateLabel.setLayoutData(gridData);
			Combo dateField=new Combo(fieldsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		   	dateField.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 
		    dateField.setText(this.dates.elementAt(i));
		    this.dateFields.add(dateField);
		    dateField.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=dateFields.indexOf(e.getSource());
					dates.setElementAt(dateFields.elementAt(n).getText(), n);
				}
			});
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.widthHint=100;
			dateField.setLayoutData(gridData);
			
			//visit
			Label formatLabel=new Label(fieldsPart, SWT.NONE);
			formatLabel.setText("Date format: ");
			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			formatLabel.setLayoutData(gridData);
			Text formatField=new Text(fieldsPart, SWT.BORDER | SWT.WRAP);
		    formatField.setText(this.formats.elementAt(i));
		    this.formatFields.add(formatField);
		    formatField.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=formatFields.indexOf(e.getSource());
					formats.setElementAt(formatFields.elementAt(n).getText(), n);
				}
			});
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			formatField.setLayoutData(gridData);
			
			//fill combos for a file
			this.dateFields.elementAt(i).add("");
			for(String s: FileHandler.getHeaders(files.elementAt(i))){
		    	this.dateFields.elementAt(i).add(s);
		    }
		}
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new SetDatesListener(this, this.dataType));
		
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	/**
	 * Initiates the vectors containing identifiers from an eventually existing column mapping file
	 */
	private void initiate(){
		this.formats=new Vector<String>();
		this.dates=new Vector<String>();
		File cmf=((ClinicalData)this.dataType).getCMF();
		if(cmf!=null){
			for(File file: ((ClinicalData)this.dataType).getRawFiles()){
				int columnNumber=FileHandler.getNumberForLabel(cmf, "ENROLL_DATE", file);
				if(columnNumber!=-1){
					this.selectedEnrollFile=file.getName();
					this.selectedEnrollColumn=FileHandler.getColumnByNumber(file, columnNumber);

					this.selectedEnrollFormat=FileHandler.getDataLabelSource(cmf, "ENROLL_DATE", file);
				}
				columnNumber=FileHandler.getNumberForLabel(cmf, "VISIT_DATE", file);
				if(columnNumber!=-1){
					this.dates.add(FileHandler.getColumnByNumber(file, columnNumber));
					this.formats.add(FileHandler.getDataLabelSource(cmf, "VISIT_DATE", file));
				}
				else{
					this.dates.add("");
					this.formats.add("");
				}
			}
		}
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
		// nothing to do
		
	}
	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		// nothing to do
		
	}
	public String getEnrollFile(){
		return this.selectedEnrollFile;
	}
	public String getEnrollColumn(){
		return this.selectedEnrollColumn;
	}
	public String getEnrollFormat(){
		return this.enrollFormat.getText();
	}
	public Vector<String> getDates(){
		return this.dates;
	}
	public Vector<String> getFormats(){
		return this.formats;
	}
}

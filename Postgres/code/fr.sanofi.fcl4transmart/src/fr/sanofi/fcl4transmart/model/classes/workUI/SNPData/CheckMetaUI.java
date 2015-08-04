package fr.sanofi.fcl4transmart.model.classes.workUI.SNPData;

import java.util.HashSet;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class CheckMetaUI implements WorkItf {
	private DataTypeItf dataType;
	private Vector<String> tables;
	private Vector<String> expected;
	private Vector<String> inserted;
	private Vector<String> equals;
	private Shell loadingShell;
	private boolean isLoading;
	private String message;
	private Display display;
	public CheckMetaUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	@Override
	public Composite createUI(Composite parent) {
		this.initiate();
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
		scroller.setLayout(gd);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.horizontalSpacing=20;
		layout.verticalSpacing=20;
		scrolledComposite.setLayout(layout);
		
		Label lab1=new Label(scrolledComposite, SWT.NONE);
		lab1.setText("Table name");
		Label lab2=new Label(scrolledComposite, SWT.NONE);
		lab2.setText("Expected lines");
		Label lab3=new Label(scrolledComposite, SWT.NONE);
		lab3.setText("Inserted lines");
		Label lab4=new Label(scrolledComposite, SWT.NONE);
		lab4.setText("Equals");
		
		for(int i=0; i<7; i++){
			Label tableLabels=new Label(scrolledComposite, SWT.NONE);
			tableLabels.setText(tables.get(i));
			Label expectedLabels=new Label(scrolledComposite, SWT.NONE);
			expectedLabels.setText(expected.get(i));
			Label insertedLabels=new Label(scrolledComposite, SWT.NONE);
			insertedLabels.setText(inserted.get(i));
			Label equalslabel=new Label(scrolledComposite, SWT.NONE);
			equalslabel.setText(equals.get(i));
		}

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}

	public void initiate(){
		openLoadingShell();
		this.message="";
		new Thread(){
			public void run() {
				try{
					tables=new Vector<String>();
					tables.add("I2B2METADATA.I2B2");
					tables.add("I2B2METADATA.I2B2_SECURE");
					tables.add("I2B2DEMODATA.PATIENT_DIMENSION");
					tables.add("I2B2DEMODATA.CONCEPT_DIMENSION");
					tables.add("I2B2DEMODATA.CONCEPT_COUNTS");
					tables.add("I2B2DEMODATA.OBSERVATION_FACT");
					tables.add("DEAPP.DE_SUBJECT_SAMPLE_MAPPING");
					
					expected=new Vector<String>();
					inserted=new Vector<String>();
					equals=new Vector<String>();
					for (int i=0; i<7; i++){
						expected.add("");
						inserted.add("");
						equals.add("");
					}
				
					//get the number of lines for I2B2METADATA.I2B2
					//expected: study node+other nodes+one for each platform+one for each tissue
					HashSet<String> paths=FileHandler.getPaths(((SnpData)dataType).getMetaTablesProps(), ((SnpData)dataType).getMappingFile());
					int nI=paths.size();
					int nE=RetrieveData.getI2b2Lines(dataType.getStudy().toString(), paths);
					expected.set(0, String.valueOf(nI));
					inserted.set(0,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(0, "OK");
					}else{
						equals.set(0, "ERROR");
					}
					
					//idem for i2b2_secure
					nI=paths.size();
					nE=RetrieveData.getI2b2SecLines(dataType.getStudy().toString(), paths);
					expected.set(1, String.valueOf(nI));
					inserted.set(1,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(1, "OK");
					}else{
						equals.set(1, "ERROR");
					}
					
					//patient_dimension
					HashSet<String> subjects=FileHandler.getSubjects(((SnpData)dataType).getMappingFile(), false);
					nI=subjects.size();
					nE=RetrieveData.getPatientLines(dataType.getStudy().toString(), subjects);
					expected.set(2, String.valueOf(nI));
					inserted.set(2,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(2, "OK");
					}else{
						equals.set(2, "ERROR");
					}
					
					//concept_dimension
					nI=paths.size();
					nE=RetrieveData.getConceptsLines(dataType.getStudy().toString(), paths);
					expected.set(3, String.valueOf(nI));
					inserted.set(3,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(3, "OK");
					}else{
						equals.set(3, "ERROR");
					}
					
					//concept_counts
					nI=paths.size();
					nE=RetrieveData.getConceptsCountLines(paths);
					expected.set(4, String.valueOf(nI));
					inserted.set(4,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(4, "OK");
					}else{
						equals.set(4, "ERROR");
					}
					
					//observation fact
					nI=FileHandler.getNumberForObservation(((SnpData)dataType).getMappingFile());
					nE=RetrieveData.getObservationLines(dataType.getStudy().toString(), paths, subjects);
					expected.set(5, String.valueOf(nI));
					inserted.set(5,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(5, "OK");
					}else{
						equals.set(5, "ERROR");
					}
					
					//sample mapping
					HashSet<String> samples=FileHandler.getSamples(((SnpData)dataType).getMappingFile(), false);
					nI=samples.size();
					nE=RetrieveData.getSamplesLines(dataType.getStudy().toString(), samples);
					expected.set(6, String.valueOf(nI));
					inserted.set(6,String.valueOf(nE));
					if(String.valueOf(nI).compareTo(String.valueOf(nE))==0){
						equals.set(6, "OK");
					}else{
						equals.set(6, "ERROR");
					}
				}catch (Exception e){
					message="Error: "+e.getLocalizedMessage();
					e.printStackTrace();
					setIsLoading(false);
					return;
				}
				 setIsLoading(false);
			}
		}.start();
		waitForThread();
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
	public void openLoadingShell(){
		this.message="";
		this.isLoading=true;

        this.display=WorkPart.display();
		this.loadingShell=new Shell(this.display);
		this.loadingShell.setSize(50, 100);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		this.loadingShell.setLayout(gridLayout);
		ProgressBar pb = new ProgressBar(this.loadingShell, SWT.HORIZONTAL | SWT.INDETERMINATE);
		pb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searching=new Label(this.loadingShell, SWT.NONE);
		searching.setText("Searching...");
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
	public void setIsLoading(boolean bool){
		this.isLoading=bool;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

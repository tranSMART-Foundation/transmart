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
package fr.sanofi.fcl4transmart.model.classes.workUI.geneanalysis;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.controllers.listeners.geneanalysis.LoadAnalysisDataListener;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.dataType.GeneExpressionAnalysis;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class allows the creation of the composite to set the study top node
 */
public class LoadDataUI implements WorkItf{
	private DataTypeItf dataType;
	private boolean testTm_cz;
	private boolean testTm_lz;
	private String message;
	private boolean isLoading;
	private Shell loadingShell;
	private Display display;
	private boolean etlServer;
	private boolean isSearching;
	private String name;
	private Button etlServerButton;
	private boolean analysisFound;
	private Text platformId;
	public LoadDataUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	@Override
	public Composite createUI(Composite parent){
		this.display=WorkPart.display();
		Shell shell=new Shell(this.display);
		shell.setSize(50, 100);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		shell.setLayout(gridLayout);
		ProgressBar pb = new ProgressBar(shell, SWT.HORIZONTAL | SWT.INDETERMINATE);
		pb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searching=new Label(shell, SWT.NONE);
		searching.setText("Searching...");
		shell.open();
		this.isSearching=true;
		new Thread(){
			public void run() {
				testTm_cz=RetrieveData.testTm_czConnection();
				testTm_lz=RetrieveData.testTm_lzConnection();

				HashMap<String, String> analyses=RetrieveFm.getAssays(dataType.getStudy().toString());
				analysisFound=false;
				for(String k: analyses.keySet()){
					if(analyses.get(k).compareTo(((GeneExpressionAnalysis)dataType).getAnalysisId())==0){
						name=k;
						analysisFound=true;
					}
				}
				isSearching=false;
			}
        }.start();
        this.display=WorkPart.display();
        while(this.isSearching){
        	if (!display.readAndDispatch()) {
                display.sleep();
              }	
        }
		shell.close();	
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
		
		if(!analysisFound){
			Label lab=new Label(scrolledComposite, SWT.NONE);
			lab.setText("The analysis could not be found");
		}else if(!this.testTm_cz || !this.testTm_lz){
			Label lab=new Label(scrolledComposite, SWT.NONE);
			lab.setText("You are not connected to a database");
			
			Button load=new Button(scrolledComposite, SWT.PUSH);
			load.setText("Load");
			load.setEnabled(false);
		}else{
			//platform identifier part definition
			Composite platformIdPart=new Composite(scrolledComposite, SWT.NONE);gd=new GridLayout();
			gd.numColumns=2;
			gd.horizontalSpacing=5;
			gd.verticalSpacing=5;
			scroller.setLayout(gd);
			platformIdPart.setLayout(gd);
			
			Label platformLabel=new Label(platformIdPart, SWT.NONE);
			platformLabel.setText("Platform identifier:");
			this.platformId=new Text(platformIdPart, SWT.BORDER);
			GridData gridData=new GridData();
			gridData.widthHint=100;
			this.platformId.setLayoutData(gridData);
			
			Label lab=new Label(scrolledComposite, SWT.NONE);
			String text="You are going to load the analysis "+this.name+"\n";
			text+="You are connected to database "+PreferencesHandler.getDb();
			lab.setText(text);
			
			etlServerButton=new Button(scrolledComposite, SWT.CHECK);
			etlServerButton.setText("Use ETL server");
			etlServerButton.addListener(SWT.Selection, new Listener(){

				@Override
				public void handleEvent(Event event) {
					etlServer=etlServerButton.getSelection();
				}
			});
			
			Button load=new Button(scrolledComposite, SWT.PUSH);
			load.setText("Load");
			load.addListener(SWT.Selection, new LoadAnalysisDataListener(this, this.dataType));
		}

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
		// nothing to do
		
	}
	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		// nothing to do
		
	}
	public void openLoadingShell(){
		this.message="";
		this.isLoading=true;
		this.loadingShell=new Shell(this.display);
		this.loadingShell.setSize(50, 100);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		this.loadingShell.setLayout(gridLayout);
		ProgressBar pb = new ProgressBar(this.loadingShell, SWT.HORIZONTAL | SWT.INDETERMINATE);
		pb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label searching=new Label(this.loadingShell, SWT.NONE);
		searching.setText("Loading...");
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
	public void setMessage(String message){
		this.message=message;
	}
	public boolean getEtlServer() {
		return this.etlServer;
	}
	public String getPlatform(){
		return this.platformId.getText();
	}
}

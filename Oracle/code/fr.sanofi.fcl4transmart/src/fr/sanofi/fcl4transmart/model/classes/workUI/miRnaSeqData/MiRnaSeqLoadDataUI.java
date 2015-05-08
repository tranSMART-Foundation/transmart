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
package fr.sanofi.fcl4transmart.model.classes.workUI.miRnaSeqData;

import java.io.File;
import java.util.Vector;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
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

import fr.sanofi.fcl4transmart.controllers.RetrieveData;
import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.controllers.fileTransfer.FolderContentProvider;
import fr.sanofi.fcl4transmart.controllers.listeners.miRnaSeqData.MiRnaSeqLoadDataListener;
import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.FolderNode;
import fr.sanofi.fcl4transmart.model.classes.FoldersTree;
import fr.sanofi.fcl4transmart.model.classes.dataType.MiRnaSeqData;
import fr.sanofi.fcl4transmart.model.classes.workUI.LoadDataUIItf;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class allows the creation of the composite to load miRNA seq expression data
 */
public class MiRnaSeqLoadDataUI implements WorkItf, LoadDataUIItf{
	private DataTypeItf dataType;
	private TreeViewer viewer;
	private String topNode;
	private Display display;
	private boolean isSearching;
	private boolean testTm_cz;
	private boolean testTm_lz;
	private boolean testDeapp;
	private String message;
	private Shell loadingShell;
	private boolean isLoading;
	private Button etlServerButton;
	private boolean etlServer;
	private FoldersTree folders;
	boolean treeBuilt;
	public MiRnaSeqLoadDataUI(DataTypeItf dataType){
		this.dataType=dataType;
		this.topNode="";
		this.etlServer=false;
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
				testDeapp=RetrieveData.testDeappConnection();
				
				folders=new FoldersTree();
				treeBuilt=RetrieveFm.buildTree(folders, false);
				topNode=RetrieveFm.searchTopNode(folders, dataType.getStudy().toString());
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
		layout.numColumns = 1;
		layout.verticalSpacing=10;
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if(topNode!=null && topNode.compareTo("")!=0){
			viewer = new TreeViewer(scrolledComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
			viewer.setContentProvider(new FolderContentProvider());
			viewer.setAutoExpandLevel(2);

			viewer.setInput(this.folders);
			
			Object elements[]=((FolderContentProvider)viewer.getContentProvider()).getElement(topNode);
			if(elements!=null) viewer.setExpandedElements(elements);	
			
			//viewer.setInput(this.studyTree);
			GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalAlignment = SWT.FILL;
			gridData.verticalAlignment=SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace=true;
			gridData.heightHint=300;
			gridData.widthHint=250;
			this.viewer.getControl().setLayoutData(gridData);
			viewer.setLabelProvider(new ColumnLabelProvider() {
			    @Override
			    public String getText(Object element) {
			        return element.toString();
			    }

			    @Override
			    public Color getBackground(Object element) {
			    	if(((FolderNode)element).getId()==RetrieveFm.getStudyId()){
			    		return new Color(Display.getCurrent(), 237, 91, 67);
			    	}
			    	return null;
			    }
			});
			
			etlServerButton=new Button(scrolledComposite, SWT.CHECK);
			etlServerButton.setText("Use ETL server");
			etlServerButton.addListener(SWT.Selection, new Listener(){

				@Override
				public void handleEvent(Event event) {
					etlServer=etlServerButton.getSelection();
				}
			});
		}
		else{
			Label label=new Label(scrolledComposite, SWT.NONE);
			if(!RetrieveData.testFmappConnection()){
				label.setText("Connection to database is not possible.");
			}
			else{
				label.setText("The study top node can not be found for the study with accession "+this.dataType.getStudy().toString()+" in the transmart database "+PreferencesHandler.getDb()+".");
			}
		}
		
		Button button=new Button(scrolledComposite, SWT.PUSH);
		button.setText("Load");
		
		if(topNode!=null && topNode.compareTo("")!=0){
			if(this.testTm_cz && this.testTm_lz && this.testDeapp){
				button.addListener(SWT.Selection, new MiRnaSeqLoadDataListener(this, this.dataType));
				Label dbLabel=new Label(scrolledComposite, SWT.NONE);
				dbLabel.setText("You are connected to database '"+PreferencesHandler.getDb()+"'");
			}
			else{
				button.setEnabled(false);
				Label warn=new Label(scrolledComposite, SWT.NONE);
				warn.setText("Warning: connection to database is not possible");
			}
		}
		else{
			button.setEnabled(false);
		}
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public String getTopNode(){
		return this.topNode;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
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
	public boolean getEtlServer(){
		return this.etlServer;
	}
	@Override
	public void setLogFile(File file) {
		((MiRnaSeqData)this.dataType).setLogFile(file);
	}
}

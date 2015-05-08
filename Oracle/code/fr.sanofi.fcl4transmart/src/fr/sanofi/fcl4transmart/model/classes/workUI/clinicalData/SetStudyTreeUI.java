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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.StudyTreeController;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.SetStudyTreeListener;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.StudyContentProvider;
import fr.sanofi.fcl4transmart.model.classes.StudyTree;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;
/**
 *This class allows the creation of the composite to set study tree for clinical data
 */
public class SetStudyTreeUI implements WorkItf{
	private DataTypeItf dataType;
	private TreeViewer viewer;
	private StudyTree studyTree;
	private Text newChildField;
	private ListViewer listViewer;
	private TreeNode root;
	private boolean isSearching;
	private HashMap<String, Vector<String>> labels;
	private ListViewer operationViewer;
	private Combo chooseFile;
	private Composite scrolledComposite;
	public SetStudyTreeUI(DataTypeItf dataType){
		this.dataType=dataType;
		this.root=new TreeNode(this.dataType.getStudy().toString(), null, false);
		this.studyTree=new StudyTree(root);
	}
	@Override
	public Composite createUI(Composite parent){
		Shell shell=new Shell();
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
				root=new TreeNode(dataType.getStudy().toString(), null, false);
				studyTree=new StudyTree(root);
				if(((ClinicalData)dataType).getCMF()!=null){
					root=new StudyTreeController(root, dataType).buildTree(((ClinicalData)dataType).getCMF());
				}
				isSearching=false;
			}
		}.start();
        Display display=WorkPart.display();
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
		
		scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
		
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		gd=new GridLayout();
		gd.numColumns=2;
		body.setLayout(gd);

		
		viewer = new TreeViewer(body, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new StudyContentProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		viewer.setInput(this.studyTree);
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
		    	if(((TreeNode)element).isLabel()){
		    		return new Color(Display.getCurrent(), 212, 212, 212);
		    	}
		    	else if(((TreeNode)element).isOperation()){
		    		return new Color(Display.getCurrent(), 212, 212, 212);
		    	}
		    	return null;
		    }
		});
	
		
		Composite leftPart=new Composite(body, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=5;
		leftPart.setLayout(gd);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment=SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace=true;
		leftPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite newChildPart=new Composite(leftPart, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=2;
		newChildPart.setLayout(gd);
		Label newChildLabel=new Label(newChildPart, SWT.NONE);
		newChildLabel.setText("Free text: ");
		this.newChildField=new Text(newChildPart, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint=100;
		this.newChildField.setLayoutData(gridData);
		
		Button addChild=new Button(leftPart, SWT.PUSH);
		addChild.setText("Add free text");
		gridData=new GridData();
		gridData.widthHint=150;
		addChild.setLayoutData(gridData);
		
		addChild.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
				TreeNode node;
				if(selection.iterator().hasNext()){
					node=(TreeNode)selection.iterator().next();
				}
				else{
					displayMessage("Select a node first");
					return;
				}
				if(selection.size()>1){
					displayMessage("Several nodes selected");
					return;
				}
				if(node.isLabel()){
					displayMessage("It is not possible to add a node to a label");
					return;
				}
				if(newChildField.getText().compareTo("")==0){
					displayMessage("Node name is empty");
					return;
				}
				if(node.getChild(newChildField.getText())!=null){
					displayMessage("This node already exists");
				}
				node.addChild(new TreeNode(newChildField.getText(), node, false));
				viewer.setExpandedState(node, true);
				viewer.refresh();
			}
		});
		
		this.labels=new HashMap<String, Vector<String>>();
		for(File file: ((ClinicalData)this.dataType).getRawFiles()){
			Vector<String> v=new Vector<String>();
			for(String s: FileHandler.getHeaders(file)){
		    	v.add(s);
		    }
			this.labels.put(file.getName(), v);
		}
		
		Label propertyLabel=new Label(leftPart, SWT.NONE);
		propertyLabel.setText("Properties:");
		
		Composite filePart=new Composite(leftPart, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=2;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		filePart.setLayout(gd);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment=SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace=true;
		filePart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label fileLabel=new Label(filePart, SWT.NONE);
		fileLabel.setText("Choose a file");
		
		this.chooseFile=new Combo(filePart, SWT.DROP_DOWN | SWT.BORDER );
	    this.chooseFile.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	}); 
	    this.chooseFile.addListener(SWT.Selection, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		updateFile();
	    	} 
    	}); 
	    for(String s: this.sort(this.labels.keySet())){
	    	this.chooseFile.add(s);
	    }
	    this.chooseFile.setText((String) this.labels.keySet().toArray()[0]);
		
		this.listViewer=new ListViewer(leftPart, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.listViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		this.listViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("rawtypes")
				Vector v = (Vector)inputElement;
				return v.toArray();
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});	
		this.listViewer.setInput(this.labels.get(this.chooseFile.getText()));
		
		Button addLabel=new Button(leftPart, SWT.PUSH);
		gridData=new GridData();
		gridData.widthHint=150;
		addLabel.setLayoutData(gridData);
		addLabel.setText("Add selected properties");
		addLabel.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				String[] selected=listViewer.getList().getSelection();
				IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
				TreeNode node;
				if(selection.iterator().hasNext()){
					node=(TreeNode)selection.iterator().next();
				}
				else{
					displayMessage("Select a node first");
					return;
				}
				if(selection.size()>1){
					displayMessage("Several nodes selected");
					return;
				}
				if(selected.length<1){
					displayMessage("Choose at least a property");
					return;
				}
				if(node.getParent()!=null && node.getParent().getParent()!=null && node.getParent().isLabel()){
					displayMessage("This node parent is already a property");
					return;
				}
				for(int i=0; i<selected.length; i++){
					if(node.getChild(chooseFile.getText()+" - "+selected[i])!=null){
						displayMessage("The property '"+chooseFile.getText()+" - "+selected[i]+"' already exists");
						return;
					}
				}
				for(int i=0; i<selected.length; i++){
					node.addChild(new TreeNode(chooseFile.getText()+" - "+selected[i], node, true));
				}
				viewer.setExpandedState(node, true);
				viewer.refresh();
			}
		});
		
		this.operationViewer=new ListViewer(leftPart, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		this.operationViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		this.operationViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				return (String[])inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});	
		String[] operationsArray={"MEAN", "MIN", "MAX"};
		this.operationViewer.setInput(operationsArray);
		
		Button addOp=new Button(leftPart, SWT.PUSH);
		addOp.setText("Add selected operation");
		gridData=new GridData();
		gridData.widthHint=150;
		addOp.setLayoutData(gridData);
		addOp.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				String[] selected=operationViewer.getList().getSelection();
				String[] propertySelected=listViewer.getList().getSelection();
				IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
				TreeNode node;
				if(selection.iterator().hasNext()){
					node=(TreeNode)selection.iterator().next();
				}
				else{
					displayMessage("Select a node first");
					return;
				}
				if(selection.size()>1){
					displayMessage("Several nodes selected");
					return;
				}
				if(node.isLabel()){
					displayMessage("An operation parent can not be a property");
					return;
				}
				if(selected.length<1){
					displayMessage("Choose at least an operation");
					return;
				}
				if(propertySelected.length<1){
					displayMessage("Choose at least one property");
					return;
				}
				Vector<String> names=new Vector<String>();
				for(int i=0; i<selected.length; i++){
					for(int j=0; j<propertySelected.length; j++){
						String rawFileName=chooseFile.getText();
						String header=propertySelected[j];
						File rawFile=new File(dataType.getPath()+File.separator+rawFileName);
						int columnNumber=FileHandler.getHeaderNumber(rawFile, header);
						if(columnNumber==-1){
							displayMessage("This property does not exist");
							return;
						}
						if(!FileHandler.isColumnNumerical(rawFile, ((ClinicalData)dataType).getWMF(), columnNumber)){
							displayMessage("The property '"+propertySelected[j]+"' is not numerical.\nIf it should be, please check the 'Set terms' step.");
							return;
						}
						String s=selected[i]+": "+chooseFile.getText()+" - "+propertySelected[j];
						if(node.getChild(s)!=null){
							displayMessage("The operation '"+selected[i]+"' for already exists for the property '"+propertySelected[j]+"'");
							return;
						}
						names.add(s);
					}
				}
				for(String s:names){
					TreeNode child=new TreeNode(s, node, false);
					child.setIsOperation(true);
					node.addChild(child);
				}
				viewer.setExpandedState(node, true);
				viewer.refresh();
			}
		});
		
		@SuppressWarnings("unused")
		Label spacer=new Label(leftPart, SWT.NONE);
		
		Button remove=new Button(leftPart,SWT.PUSH);
		gridData=new GridData();
		gridData.widthHint=150;
		remove.setLayoutData(gridData);
		remove.setText("Remove selected items");
		remove.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
				Vector<TreeNode> nodes=new Vector<TreeNode>();	
				@SuppressWarnings("rawtypes")
				Iterator it=selection.iterator();
				while(it.hasNext()){
					nodes.add((TreeNode)it.next());
				}
				if(nodes.size()<1){
					displayMessage("Select a node first");
					return;
				}
				for(TreeNode node: nodes){
					if(node.getParent()==null){
						displayMessage("You can not remove the root of the study");
						return;
					}
				}
				for(TreeNode node: nodes){
					node.getParent().removeChild(node);
				}
				viewer.refresh();
			}
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new SetStudyTreeListener(this, this.dataType));
		

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		return composite;
	}
	private void updateFile(){
		this.listViewer.setInput(this.labels.get(this.chooseFile.getText()));
		this.scrolledComposite.layout(true, true);	
		this.scrolledComposite.getParent().layout(true, true);
		this.scrolledComposite.setSize(this.scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public TreeNode getRoot(){
		return this.root;
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
	
	public Vector<String> sort(Set<String> sort) {
        Vector<String> v = new Vector<String>();
        for(String s: sort) {
            int i = 0;
            for (i = 0; i < v.size(); i++) {
                int c = s.compareTo((String) v.elementAt(i));
                if (c < 0) {
                    v.insertElementAt(s, i);
                    break;
                } else if (c == 0) {
                    break;
                }
            }
            if (i >= v.size()) {
                v.addElement(s);
            }
        }
        return v;
    }
}

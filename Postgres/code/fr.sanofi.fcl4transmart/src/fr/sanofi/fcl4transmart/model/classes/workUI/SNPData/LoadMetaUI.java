package fr.sanofi.fcl4transmart.model.classes.workUI.SNPData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.StudyContentProvider;
import fr.sanofi.fcl4transmart.controllers.listeners.snpData.LoadMetaListener;
import fr.sanofi.fcl4transmart.model.classes.StudyTree;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class LoadMetaUI implements WorkItf {
	private DataTypeItf dataType;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Button b6;
	private Button b7;
	private Button b8;
	private boolean skipConceptDim;
	private boolean skipPatientDim;
	private boolean skipI2b2;
	private boolean skipI2b2Secure;
	private boolean skipObsFact;
	private boolean skipConceptCounts;
	private boolean skipSubjectSample;
	private boolean useServer;
	private TreeViewer viewer;
	private StudyTree studyTree;
	private Text newTextField;
	private TreeNode root;
	private String message;
	private boolean isLoading;
	private Display display;
	private Shell loadingShell;
	public LoadMetaUI(DataTypeItf dataType){
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
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing=20;
		scrolledComposite.setLayout(layout);
		
		Group treePart=new Group(scrolledComposite, SWT.SHADOW_ETCHED_IN);
		treePart.setText("Data tree");
		GridData gridData=new GridData();
		gridData.horizontalAlignment=GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		treePart.setLayoutData(gridData);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		gridLayout.horizontalSpacing=10;
		gridLayout.verticalSpacing=20;
		treePart.setLayout(gridLayout);
		
		viewer = new TreeViewer(treePart, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new StudyContentProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		viewer.setInput(this.studyTree);
		viewer.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		        return element.toString();
		    }

		    @Override
		    public Color getBackground(Object element) {
		    	if(((TreeNode)element).isLabel()){
		    		return new Color(Display.getCurrent(), 237, 91, 67);
		    	}
		    	return null;
		    }
		});
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment=SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace=true;
		gridData.heightHint=300;
		gridData.widthHint=250;
		this.viewer.getControl().setLayoutData(gridData);
		
		Composite leftPart=new Composite(treePart, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=1;
		leftPart.setLayout(gd);
		leftPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite freePart=new Composite(leftPart, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=2;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		freePart.setLayout(gd);
		freePart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label newTextLabel=new Label(freePart, SWT.NONE);
		newTextLabel.setText("Free text");
		newTextLabel.setLayoutData(new GridData());
		
		this.newTextField=new Text(freePart, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint=100;
		this.newTextField.setLayoutData(gridData);
		
		Button addText=new Button(leftPart, SWT.PUSH);
		addText.setText("Add node");
		gridData = new GridData();
		gridData.widthHint=150;
		addText.setLayoutData(gridData);
		
		addText.addListener(SWT.Selection, new Listener(){
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
				if(node.hasChildren()){
					displayMessage("A node can only have one child");
					return;
				}
				if(newTextField.getText().compareTo("")==0){
					displayMessage("Choose a node name first");
					return;
				}if(node.isLabel()){
					displayMessage("You can not add nodes below SNP node");
					return;
				}
				node.addChild(new TreeNode(newTextField.getText(), node, false));
				viewer.setExpandedState(node, true);
				viewer.refresh();
			}
		});
		
		@SuppressWarnings("unused")
		Label space=new Label(leftPart, SWT.NONE);
			
		Button addSnp=new Button(leftPart, SWT.PUSH);
		addSnp.setText("Add SNP node");
		gridData = new GridData();
		gridData.widthHint=150;
		addSnp.setLayoutData(gridData);
		
		addSnp.addListener(SWT.Selection, new Listener(){
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
				if(node.hasChildren()){
					displayMessage("A node can only have one child");
					return;
				}
				node.addChild(new TreeNode("SNP", node, true));
				viewer.setExpandedState(node, true);
				viewer.refresh();
			}
		});
		
		@SuppressWarnings("unused")
		Label space2=new Label(leftPart, SWT.NONE);
		
		Button remove=new Button(leftPart,SWT.PUSH);
		remove.setText("Remove a node");
		gridData = new GridData();
		gridData.widthHint=150;
		remove.setLayoutData(gridData);
		remove.addListener(SWT.Selection, new Listener(){
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
				if(node.getParent()==null){
					displayMessage("Root can not be removed");
					return;
				}
				node.getParent().removeChild(node);
				viewer.refresh();
			}
		});
		
		
		Group detailsPart=new Group(scrolledComposite, SWT.SHADOW_ETCHED_IN);
		detailsPart.setText("Loading details");
		gridData=new GridData();
		gridData.horizontalAlignment=GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		detailsPart.setLayoutData(gridData);
		gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		gridLayout.horizontalSpacing=10;
		gridLayout.verticalSpacing=5;
		detailsPart.setLayout(gridLayout);
		
		this.b1=new Button(detailsPart, SWT.CHECK);
		this.b1.setText("Skip Concept Dimension");
		this.b1.setSelection(this.skipConceptDim);
		this.b1.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipConceptDim=b1.getSelection();
			}
		});
		
		this.b2=new Button(detailsPart, SWT.CHECK);
		this.b2.setText("Skip Patient Dimension");
		this.b2.setSelection(this.skipPatientDim);
		this.b2.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipPatientDim=b2.getSelection();
			}
		});
		
		this.b3=new Button(detailsPart, SWT.CHECK);
		this.b3.setText("Skip I2b2");
		this.b3.setSelection(this.skipI2b2);
		this.b3.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipI2b2=b3.getSelection();
			}
		});
		
		this.b4=new Button(detailsPart, SWT.CHECK);
		this.b4.setText("Skip I2b2 Secure");
		this.b4.setSelection(this.skipI2b2Secure);
		this.b4.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipI2b2Secure=b4.getSelection();
			}
		});
		
		this.b5=new Button(detailsPart, SWT.CHECK);
		this.b5.setText("Skip Observation fact");
		this.b5.setSelection(this.skipObsFact);
		this.b5.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipObsFact=b5.getSelection();
			}
		});
		
		this.b6=new Button(detailsPart, SWT.CHECK);
		this.b6.setText("Skip Concept Counts");
		this.b6.setSelection(this.skipConceptCounts);
		this.b6.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipConceptCounts=b6.getSelection();
			}
		});
		
		this.b7=new Button(detailsPart, SWT.CHECK);
		this.b7.setText("Skip Subject to Sample Mapping");
		this.b7.setSelection(this.skipSubjectSample);
		this.b7.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipSubjectSample=b7.getSelection();
			}
		});
		
		
		this.b8=new Button(scrolledComposite, SWT.CHECK);
		this.b8.setText("Use ETL server");
		this.b8.setSelection(this.useServer);
		this.b8.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				useServer=b8.getSelection();
			}
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new LoadMetaListener(this.dataType, this));
		gridData = new GridData();
		gridData.widthHint = 50;
		ok.setLayoutData(gridData);

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public void initiate(){
		this.skipConceptCounts=false;
		this.skipConceptDim=false;
		this.skipI2b2=false;
		this.skipI2b2Secure=false;
		this.skipPatientDim=false;
		this.skipSubjectSample=false;
		this.skipObsFact=false;
		this.useServer=false;
		
		this.root=new TreeNode(this.dataType.getStudy().toString(), null, false);
		this.studyTree=new StudyTree(root);
		if(((SnpData)dataType).getMetaTablesProps()!=null){
			String baseNode="";
			try{
				BufferedReader br = new BufferedReader(new FileReader(((SnpData)this.dataType).getMetaTablesProps()));
				String line;
				HashMap<String, Boolean> mapBool=new HashMap<String, Boolean>();
				mapBool.put("yes", true);
				mapBool.put("no", false);
				mapBool.put("", false);
				while((line=br.readLine())!=null){
					if(line.indexOf("snp_base_node=")==0){
						baseNode=line.split("=", 2)[1];
					}else if(line.indexOf("skip_concept_dimension=")==0){
						this.skipConceptDim=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_patient_dimension=")==0){
						this.skipPatientDim=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_i2b2=")==0){
						this.skipI2b2=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_i2b2_secure=")==0){
						this.skipI2b2Secure=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_observation_fact=")==0){
						this.skipObsFact=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_concept_counts=")==0){
						this.skipConceptCounts=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_de_subject_sample_mapping=")==0){
						this.skipSubjectSample=mapBool.get(line.split("=", 2)[1]);
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return ;
			}
			TreeNode parent=root;
			int cnt=0;//don't take programs and studies names for SNP node path
			for (String s: baseNode.split("/", -1)){
				if(s.compareTo("")!=0){
					if(cnt>1){
						TreeNode node=new TreeNode(s, parent, false);
						parent.addChild(node);
						parent=node;
					}
					cnt++;
				}
			}
			parent.addChild(new TreeNode("SNP", parent, true));
		}
		
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
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public TreeNode getRoot(){
		return this.root;
	}

	public boolean isUseServer() {
		return useServer;
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
	public boolean isSkipConceptDim() {
		return skipConceptDim;
	}
	public boolean isSkipPatientDim() {
		return skipPatientDim;
	}
	public boolean isSkipI2b2() {
		return skipI2b2;
	}
	public boolean isSkipI2b2Secure() {
		return skipI2b2Secure;
	}
	public boolean isSkipObsFact() {
		return skipObsFact;
	}
	public boolean isSkipConceptCounts() {
		return skipConceptCounts;
	}
	public boolean isSkipSubjectSample() {
		return skipSubjectSample;
	}

}

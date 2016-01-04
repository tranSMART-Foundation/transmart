package fr.sanofi.fcl4transmart.model.classes.workUI.SNPData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import fr.sanofi.fcl4transmart.controllers.listeners.snpData.LoadDataListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class LoadDataUI implements WorkItf {
	private DataTypeItf dataType;
	private String startChr;
	private String endChr;
	private Text t1;
	private Text t2;
	private boolean skipDataset;
	private boolean skipProbeSort;
	private boolean skipDataByPatient;
	private boolean skipDataByProbe;
	private boolean skipSubjectSorted;
	private boolean useEtlServer;
	private boolean skipCallsByGsm;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Button b6;
	private Button b7;
	private Shell loadingShell;
	private boolean isLoading;
	private Display display;
	private String message;
	
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
		layout.numColumns = 1;
		layout.horizontalSpacing=20;
		layout.verticalSpacing=20;
		scrolledComposite.setLayout(layout);
		
		/////
		Group infoPart=new Group(scrolledComposite, SWT.SHADOW_ETCHED_IN);
		infoPart.setText("Parameters");
		GridData gridData=new GridData();
		gridData.horizontalAlignment=GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		infoPart.setLayoutData(gridData);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		gridLayout.horizontalSpacing=10;
		gridLayout.verticalSpacing=5;
		infoPart.setLayout(gridLayout);
		
		Label label1=new Label(infoPart, SWT.NONE);
		label1.setText("Start chromosome number");
		this.t1=new Text(infoPart, SWT.BORDER);
		this.t1.setText(this.startChr);
		this.t1.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				startChr=t1.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.t1.setLayoutData(gridData);
		
		Label label2=new Label(infoPart, SWT.NONE);
		label2.setText("End chromosome number");
		this.t2=new Text(infoPart, SWT.BORDER);
		this.t2.setText(this.endChr);
		this.t2.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				endChr=t2.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.t2.setLayoutData(gridData);
		
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
		this.b1.setText("Skip SNP_DATASET");
		this.b1.setSelection(this.skipDataset);
		this.b1.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipDataset=b1.getSelection();
			}
		});
		
		this.b2=new Button(detailsPart, SWT.CHECK);
		this.b2.setText("Skip SNP_PROBE_SORTED_DEF");
		this.b2.setSelection(this.skipProbeSort);
		this.b2.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipProbeSort=b2.getSelection();
			}
		});
		
		this.b3=new Button(detailsPart, SWT.CHECK);
		this.b3.setText("Skip SNP_DATA_BY_PATIENT");
		this.b3.setSelection(this.skipDataByPatient);
		this.b3.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipDataByPatient=b3.getSelection();
			}
		});
		
		this.b4=new Button(detailsPart, SWT.CHECK);
		this.b4.setText("Skip SNP_DATA_BY_PROBE");
		this.b4.setSelection(this.skipDataByProbe);
		this.b4.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipDataByProbe=b4.getSelection();
			}
		});
		
		this.b5=new Button(detailsPart, SWT.CHECK);
		this.b5.setText("Skip SNP_SUBJECT_SORTED_DEF");
		this.b5.setSelection(this.skipSubjectSorted);
		this.b5.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipSubjectSorted=b5.getSelection();
			}
		});
		
		this.b7=new Button(detailsPart, SWT.CHECK);
		this.b7.setText("Skip DE_SNP_CALLS_BY_GSM");
		this.b7.setSelection(this.skipCallsByGsm);
		this.b7.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipCallsByGsm=b7.getSelection();
			}
		});

		this.b6=new Button(scrolledComposite, SWT.CHECK);
		this.b6.setText("Use ETL server");
		this.b6.setSelection(this.useEtlServer);
		this.b6.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				useEtlServer=b6.getSelection();
			}
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		gridData = new GridData();
		gridData.widthHint = 50;
		ok.setLayoutData(gridData);
		ok.addListener(SWT.Selection, new LoadDataListener(dataType, this));
		/////

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public void initiate(){
		this.startChr="";
		this.endChr="";
		this.skipDataByPatient=false;
		this.skipDataByProbe=false;
		this.skipDataset=false;
		this.skipProbeSort=false;
		this.skipSubjectSorted=false;
		this.skipCallsByGsm=false;
		this.useEtlServer=false;
		
		if(((SnpData)this.dataType).getDataProp()!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(((SnpData)this.dataType).getDataProp()));
				String line;
				HashMap<String, Boolean> mapBool=new HashMap<String, Boolean>();
				mapBool.put("yes", true);
				mapBool.put("no", false);
				mapBool.put("", false);
				while((line=br.readLine())!=null){
					if(line.indexOf("start_chr=")==0){
						this.startChr=line.split("=", 2)[1];
					}else if(line.indexOf("end_chr=")==0){
						this.endChr=line.split("=", 2)[1];
					}else if(line.indexOf("skip_snp_dataset=")==0){
						this.skipDataset=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_snp_probe_sorted_def=")==0){
						this.skipProbeSort=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_snp_data_by_patient=")==0){
						this.skipDataByPatient=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_snp_data_by_probe=")==0){
						this.skipDataByProbe=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_snp_subject_sorted_def=")==0){
						this.skipSubjectSorted=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_snp_calls_by_gsm=")==0){
						this.skipCallsByGsm=mapBool.get(line.split("=", 2)[1]);
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return ;
			}
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
	public String getStartChr() {
		return startChr;
	}
	public String getEndChr() {
		return endChr;
	}
	public boolean isSkipDataset() {
		return skipDataset;
	}
	public boolean isSkipProbeSort() {
		return skipProbeSort;
	}
	public boolean isSkipDataByPatient() {
		return skipDataByPatient;
	}
	public boolean isSkipDataByProbe() {
		return skipDataByProbe;
	}
	public boolean isSkipSubjectSorted() {
		return skipSubjectSorted;
	}
	public boolean isUseEtlServer() {
		return useEtlServer;
	}
	public LoadDataUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	public boolean isSkipCallsByGsm(){
		return this.skipCallsByGsm;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}public void openLoadingShell(){
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
}

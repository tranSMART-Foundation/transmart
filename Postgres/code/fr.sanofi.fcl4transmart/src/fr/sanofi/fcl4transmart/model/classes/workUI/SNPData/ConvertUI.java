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

import fr.sanofi.fcl4transmart.controllers.listeners.snpData.ConvertListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class ConvertUI implements WorkItf {
	private DataTypeItf dataType;
	private String path;
	private boolean skipGenotype;
	private boolean skipLgen;
	private boolean skipFam;
	private boolean skipPlink;
	private boolean useEtlServer;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Text textPlink;
	private Shell loadingShell;
	private boolean isLoading;
	private Display display;
	private String message;
	
	public ConvertUI(DataTypeItf dataType){
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
		label1.setText("Plink executable path");
		this.textPlink=new Text(infoPart, SWT.BORDER);
		this.textPlink.setText(this.path);
		this.textPlink.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				path=textPlink.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.textPlink.setLayoutData(gridData);
		
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
		this.b1.setText("Skip genotype format");
		this.b1.setSelection(this.skipGenotype);
		this.b1.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipGenotype=b1.getSelection();
			}
		});
		
		this.b2=new Button(detailsPart, SWT.CHECK);
		this.b2.setText("Skip lgen format");
		this.b2.setSelection(this.skipLgen);
		this.b2.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipLgen=b2.getSelection();
			}
		});
		
		this.b3=new Button(detailsPart, SWT.CHECK);
		this.b3.setText("Skip fam format");
		this.b3.setSelection(this.skipFam);
		this.b3.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipFam=b3.getSelection();
			}
		});
		
		this.b4=new Button(detailsPart, SWT.CHECK);
		this.b4.setText("Skip Plink files creation");
		this.b4.setSelection(this.skipPlink);
		this.b4.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipPlink=b4.getSelection();
			}
		});
		
		this.b5=new Button(scrolledComposite, SWT.CHECK);
		this.b5.setText("Use ETL server");
		this.b5.setSelection(this.useEtlServer);
		this.b5.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				useEtlServer=b5.getSelection();
			}
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		gridData = new GridData();
		gridData.widthHint = 50;
		ok.setLayoutData(gridData);
		ok.addListener(SWT.Selection, new ConvertListener(dataType, this));

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public String getPath() {
		return path;
	}
	public boolean isSkipGenotype() {
		return skipGenotype;
	}
	public boolean isSkipLgen() {
		return skipLgen;
	}
	public boolean isSkipFam() {
		return skipFam;
	}
	public boolean isSkipPlink() {
		return skipPlink;
	}
	public boolean isUseEtlServer() {
		return useEtlServer;
	}
	public void initiate(){
		this.skipFam=false;
		this.skipGenotype=false;
		this.skipLgen=false;
		this.skipPlink=false;
		this.useEtlServer=false;
		this.path="";
		
		if(((SnpData)this.dataType).getConversionProps()!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(((SnpData)this.dataType).getConversionProps()));
				String line;
				HashMap<String, Boolean> mapBool=new HashMap<String, Boolean>();
				mapBool.put("yes", true);
				mapBool.put("no", false);
				mapBool.put("", false);
				while((line=br.readLine())!=null){
					if(line.indexOf("plink=")==0){
						this.path=line.split("=", 2)[1];
					}else if(line.indexOf("skip_format_genotype=")==0){
						this.skipGenotype=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_format_lgen=")==0){
						this.skipLgen=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_create_fam=")==0){
						this.skipFam=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_plink_file_creation=")==0){
						this.skipPlink=mapBool.get(line.split("=", 2)[1]);
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
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

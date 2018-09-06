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
import org.eclipse.swt.widgets.Combo;
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

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.snpData.LoadAnnotationListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.SnpData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class LoadAnnotationUI implements WorkItf {
	private DataTypeItf dataType;
	private String platformId;
	private String platformTitle;
	private String platformType;
	private String platformOrganism;
	private Text textpId;
	private Text textpTitle;
	private Text textpType;
	private Text textpOrganism;
	private Combo cSnpId;
	private Combo cRsId;
	private Combo cChr;
	private Combo cPos;
	private Combo cGene;
	private String snpId;
	private String rsId;
	private String chr;
	private String pos;
	private String gene;
	private Vector<String> columns;
	private boolean skipGplLoader;
	private boolean skipGplInfo;
	private boolean skipSnpInfo;
	private boolean skipSnpProbe;
	private boolean skipGeneMap;
	private boolean useServer;
	private Button b1;
	private Button b2;
	private Button b3;
	private Button b4;
	private Button b5;
	private Button b6;
	private String message;
	private Shell loadingShell;
	private boolean isLoading;
	private Display display;
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
		
		Group infoPart=new Group(scrolledComposite, SWT.SHADOW_ETCHED_IN);
		infoPart.setText("Platform information");
		GridData gridData=new GridData();
		gridData.horizontalAlignment=GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		infoPart.setLayoutData(gridData);
		GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		gridLayout.horizontalSpacing=10;
		gridLayout.verticalSpacing=20;
		infoPart.setLayout(gridLayout);
		
		Label label1=new Label(infoPart, SWT.NONE);
		label1.setText("Platform identifier");
		this.textpId=new Text(infoPart, SWT.BORDER);
		this.textpId.setText(this.platformId);
		this.textpId.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				platformId=textpId.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.textpId.setLayoutData(gridData);
		
		Label label2=new Label(infoPart, SWT.NONE);
		label2.setText("Platform title");
		this.textpTitle=new Text(infoPart, SWT.BORDER);
		this.textpTitle.setText(this.platformTitle);
		this.textpTitle.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				platformTitle=textpTitle.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.textpTitle.setLayoutData(gridData);
		
		Label label3=new Label(infoPart, SWT.NONE);
		label3.setText("Platform marker type");
		this.textpType=new Text(infoPart, SWT.BORDER);
		this.textpType.setText(this.platformType);
		this.textpType.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				platformType=textpType.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.textpType.setLayoutData(gridData);
		
		Label label4=new Label(infoPart, SWT.NONE);
		label4.setText("Platform organism");
		this.textpOrganism=new Text(infoPart, SWT.BORDER);
		this.textpOrganism.setText(this.platformOrganism);
		this.textpOrganism.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				platformOrganism=textpOrganism.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=150;
		this.textpOrganism.setLayoutData(gridData);

		Group columnsPart=new Group(scrolledComposite, SWT.SHADOW_ETCHED_IN);
		columnsPart.setText("File columns description");
		gridData=new GridData();
		gridData.horizontalAlignment=GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		columnsPart.setLayoutData(gridData);
		gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		gridLayout.horizontalSpacing=10;
		gridLayout.verticalSpacing=5;
		columnsPart.setLayout(gridLayout);
		
		Label label5=new Label(columnsPart, SWT.NONE);
		label5.setText("SNP identifiers");
		
		this.cSnpId=new Combo(columnsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		this.cSnpId.setText(snpId);
		this.cSnpId.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 

		this.cSnpId.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				snpId=cSnpId.getText();
			}
		});
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.cSnpId.setLayoutData(gridData);
		
		for(String s: columns){
			this.cSnpId.add(s);
	    }
		
		Label label6=new Label(columnsPart, SWT.NONE);
		label6.setText("RS identifiers");
		
		this.cRsId=new Combo(columnsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		this.cRsId.setText(rsId);
		this.cRsId.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 

		this.cRsId.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				rsId=cRsId.getText();
			}
		});
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.cRsId.setLayoutData(gridData);
		
		for(String s: columns){
			this.cRsId.add(s);
	    }
		
		Label label7=new Label(columnsPart, SWT.NONE);
		label7.setText("Chromosome number");
		
		this.cChr=new Combo(columnsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		this.cChr.setText(chr);
		this.cChr.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 

		this.cChr.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				chr=cChr.getText();
			}
		});
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.cChr.setLayoutData(gridData);
		
		for(String s: columns){
			this.cChr.add(s);
	    }
		
		Label label8=new Label(columnsPart, SWT.NONE);
		label8.setText("SNP position");
		
		this.cPos=new Combo(columnsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		this.cPos.setText(pos);
		this.cPos.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 

		this.cPos.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				pos=cPos.getText();
			}
		});
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.cPos.setLayoutData(gridData);
		
		for(String s: columns){
			this.cPos.add(s);
	    }
		
		Label label9=new Label(columnsPart, SWT.NONE);
		label9.setText("Gene identifier (optional)");
		
		this.cGene=new Combo(columnsPart, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		this.cGene.setText(gene);
		this.cGene.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 

		this.cGene.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e){
				gene=cGene.getText();
			}
		});
	    gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.cGene.setLayoutData(gridData);
		
		this.cGene.add("");
		for(String s: columns){
			this.cGene.add(s);
	    }
		
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
		this.b1.setText("Skip GPL annotation loader");
		this.b1.setSelection(this.skipGplLoader);
		this.b1.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipGplLoader=b1.getSelection();
			}
		});
		
		this.b2=new Button(detailsPart, SWT.CHECK);
		this.b2.setText("Skip DE_GPL_INFO");
		this.b2.setSelection(this.skipGplInfo);
		this.b2.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipGplInfo=b2.getSelection();
			}
		});
		
		this.b3=new Button(detailsPart, SWT.CHECK);
		this.b3.setText("Skip DE_SNP_INFO");
		this.b3.setSelection(this.skipSnpInfo);
		this.b3.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipSnpInfo=b3.getSelection();
			}
		});
		
		this.b4=new Button(detailsPart, SWT.CHECK);
		this.b4.setText("Skip DE_SNP_PROBE");
		this.b4.setSelection(this.skipSnpProbe);
		this.b4.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipSnpProbe=b4.getSelection();
			}
		});
		
		this.b6=new Button(detailsPart, SWT.CHECK);
		this.b6.setText("Skip DE_SNP_GENEMAP");
		this.b6.setSelection(this.skipGeneMap);
		this.b6.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				skipGeneMap=b6.getSelection();
			}
		});
		
		this.b5=new Button(scrolledComposite, SWT.CHECK);
		this.b5.setText("Use ETL server");
		this.b5.setSelection(this.useServer);
		this.b5.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				useServer=b5.getSelection();
			}
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		gridData = new GridData();
		gridData.widthHint = 50;
		ok.setLayoutData(gridData);
		ok.addListener(SWT.Selection, new LoadAnnotationListener(dataType, this));

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public void initiate(){
		this.platformId="";
		this.platformOrganism="";
		this.platformTitle="";
		this.platformType="";
		this.snpId="";
		this.rsId="";
		this.chr="";
		this.pos="";
		this.gene="";
		this.skipGplInfo=false;
		this.skipSnpInfo=false;
		this.skipSnpProbe=false;
		this.skipGplLoader=false;
		this.skipGeneMap=false;
		this.useServer=false;
		
		this.columns=FileHandler.getHeadersSnpAnnotation(((SnpData)this.dataType).getAnnotationFile());
		
		if(((SnpData)this.dataType).getAnnotationProps()!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(((SnpData)this.dataType).getAnnotationProps()));
				String line;
				HashMap<String, Boolean> mapBool=new HashMap<String, Boolean>();
				mapBool.put("yes", true);
				mapBool.put("no", false);
				mapBool.put("", false);
				while((line=br.readLine())!=null){
					if(line.indexOf("platform=")==0){
						this.platformId=line.split("=", 2)[1];
					}else if(line.indexOf("title=")==0){
						this.platformTitle=line.split("=", 2)[1];
					}else if(line.indexOf("marker_type=")==0){
						this.platformType=line.split("=", 2)[1];
					}else if(line.indexOf("organism=")==0){
						this.platformOrganism=line.split("=", 2)[1];
					}else if(line.indexOf("snp_id=")==0){
						this.snpId=line.split("=", 2)[1];
					}else if(line.indexOf("rsId=")==0){
						this.rsId=line.split("=", 2)[1];
					}else if(line.indexOf("chr=")==0){
						this.chr=line.split("=", 2)[1];
					}else if(line.indexOf("pos=")==0){
						this.pos=line.split("=", 2)[1];
					}else if(line.indexOf("gene=")==0){
						this.gene=line.split("=", 2)[1];
					}else if(line.indexOf("skip_gpl_annotation_loader=")==0){
						this.skipGplLoader=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_de_gpl_info=")==0){
						this.skipGplInfo=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_de_snp_info=")==0){
						this.skipSnpInfo=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_de_snp_probe=")==0){
						this.skipSnpProbe=mapBool.get(line.split("=", 2)[1]);
					}else if(line.indexOf("skip_de_snp_gene_map=")==0){
						this.skipGeneMap=mapBool.get(line.split("=", 2)[1]);
					}
				}
				br.close();
			}catch (Exception e){
				e.printStackTrace();
				return ;
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
	public String getPlatformId() {
		return platformId;
	}
	public String getPlatformTitle() {
		return platformTitle;
	}
	public String getPlatformType() {
		return platformType;
	}
	public String getPlatformOrganism() {
		return platformOrganism;
	}
	public LoadAnnotationUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	public String getSnpId() {
		return snpId;
	}
	public String getRsId() {
		return rsId;
	}
	public String getChr(){
		return chr;
	}
	public String getPos() {
		return pos;
	}
	public String getGene() {
		return gene;
	}
	public boolean isSkipGplLoader() {
		return skipGplLoader;
	}
	public boolean isSkipGplInfo() {
		return skipGplInfo;
	}
	public boolean isSkipSnpInfo() {
		return skipSnpInfo;
	}
	public boolean isSkipSnpProbe() {
		return skipSnpProbe;
	}
	public boolean isSkipGeneMap() {
		return skipGeneMap;
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
}

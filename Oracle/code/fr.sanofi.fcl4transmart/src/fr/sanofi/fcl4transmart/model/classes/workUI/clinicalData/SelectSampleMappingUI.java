package fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.SelectSampleMappingListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SelectSampleMappingUI implements WorkItf {
	private DataTypeItf dataType;
	private Vector<String> paths;
	private Vector<Text> pathsText;
	private Vector<Button> browseButtons;

	private boolean isLoading;
	private Shell loadingShell;
	private Display display;
	private String message;
	public SelectSampleMappingUI(DataTypeItf dataType){
		this.dataType=dataType;
	}
	@Override
	public Composite createUI(Composite parent) {
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
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.initiate();
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=3;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		body.setLayout(gd);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label l1=new Label(body, SWT.NONE);
		l1.setText("Raw file name");
		
		Label l2=new Label(body, SWT.NONE);
		l2.setText("Mapping file path");
		
		Label l3=new Label(body, SWT.NONE);
		l3.setText("");
		
		Vector<String> rawFileName=((ClinicalData)this.dataType).getRawFilesNames();
		for(int i=0; i<rawFileName.size(); i++){
			Text fileName=new Text(body, SWT.BORDER);
			fileName.setText(rawFileName.get(i));
			fileName.setEditable(false);
			GridData gridData = new GridData();
			gridData.widthHint=100;
			fileName.setLayoutData(gridData);
			
			Text t2=new Text(body,SWT.BORDER);
			t2.setText(this.paths.get(i));
			t2.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=pathsText.indexOf(e.getSource());
					paths.setElementAt(pathsText.elementAt(n).getText(), n);
				}
			});
			gridData = new GridData();
			gridData.widthHint=100;
			t2.setLayoutData(gridData);
			this.pathsText.add(t2);
			
			Button browse=new Button(body, SWT.PUSH);
			browse.setText("Browse");
			browse.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fd=new FileDialog(new Shell(), SWT.SINGLE);
					fd.open();
					String filename=fd.getFileName();
					String filterPath=fd.getFilterPath(); 
					String path="";
					if(filterPath!=null && filterPath.trim().length()>0){
						path+=filterPath+File.separator;
					}
					path+=filename;
					int n=browseButtons.indexOf((Button)e.getSource());
					pathsText.get(n).setText(path);
					paths.set(n, path);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// nothing to do
				}
			});
			browseButtons.add(browse);
		}
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new SelectSampleMappingListener(this.dataType, this));
		
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	private void initiate(){
		this.paths=new Vector<String>();
		this.pathsText=new Vector<Text>();
		this.browseButtons=new Vector<Button>();
		for(String s: ((ClinicalData)this.dataType).getRawFilesNames()){
			if(((ClinicalData)this.dataType).getMappingFiles().containsKey(s)){
				this.paths.add(((ClinicalData)this.dataType).getMappingFiles().get(s).getAbsolutePath());
			}else{
				this.paths.add("");
			}
		}
	}
	public Vector<String> getPathsToMapping(){
		return this.paths;
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
		//nothing to do
	}

	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		//nothing to do
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
		searching.setText("Checking files...");
		this.loadingShell.open();
	}
	public void waitForThread(){
		this.display=WorkPart.display();
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
}

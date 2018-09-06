package fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer;

import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import fr.sanofi.fcl4transmart.model.classes.TransferProcess;
import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;


public class TransferMonitoringUI implements WorkItf {
	private Vector<TransferProcess> filesToTransfer;
	private DataTypeItf dataType;
	public TransferMonitoringUI(DataTypeItf dataType){
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
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
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
		
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		gd=new GridLayout();
		gd.numColumns=1;
		body.setLayout(gd);
		
		Vector<TransferProcess> processes=((FilesTransfer)this.dataType).getProcesses();
		this.filesToTransfer=processes;
		
		if(this.filesToTransfer.size()<1){
			Label noProcess=new Label(body, SWT.NONE);
			noProcess.setText("There is no file transfer");
		}
		else{
			int cn=0;
			for(TransferProcess process: this.filesToTransfer){
				if(process.getRemotePath().compareTo(((FilesTransfer)this.dataType).getRemotePath())==0){
					cn++;
					Composite line=new Composite(body, SWT.NONE);
					layout = new GridLayout();
					layout.numColumns = 4;
					line.setLayout(layout);
					gridData = new GridData();
					gridData.horizontalAlignment = SWT.FILL;
					gridData.grabExcessHorizontalSpace = true;
					line.setLayoutData(gridData);
					
					Text name=new Text(line, SWT.BORDER);
					name.setText(process.toString());
					name.setEditable(false);
					gridData=new GridData();
					gridData.widthHint=150;
					name.setLayoutData(gridData);
					
					ProgressBar bar=new ProgressBar(line, SWT.SMOOTH);
					gridData=new GridData();
					gridData.widthHint=250;
					bar.setLayoutData(gridData);
					bar.setMinimum(0);
					bar.setMaximum(100);
					process.setBar(bar);
					
					Label progressLabel=new Label(line, SWT.NONE);
					process.setProgressLabel(progressLabel);
					gridData=new GridData();
					gridData.widthHint=30;
					progressLabel.setLayoutData(gridData);
					
					Button cancel=new Button(line, SWT.PUSH);
					process.setButton(cancel);
					gridData=new GridData();
					gridData.widthHint=50;
					cancel.setLayoutData(gridData);
					
					@SuppressWarnings("unused")
					Label space1=new Label(line, SWT.NONE);
					
					Label status=new Label(line, SWT.NONE);
					gridData = new GridData();
					gridData.horizontalAlignment = SWT.FILL;
					gridData.grabExcessHorizontalSpace = true;
					status.setLayoutData(gridData);
					process.setStatusLabel(status);
					
					@SuppressWarnings("unused")
					Label space2=new Label(line, SWT.NONE);
					@SuppressWarnings("unused")
					Label space3=new Label(line, SWT.NONE);
				}
			}
			if(cn==0){
				Label noProcess=new Label(body, SWT.NONE);
				noProcess.setText("There is no file transfer");
			}
		}
	    
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
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
}

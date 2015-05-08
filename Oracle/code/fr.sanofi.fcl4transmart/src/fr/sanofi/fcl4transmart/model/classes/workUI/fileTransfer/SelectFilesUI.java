package fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer;

import java.io.File;
import java.util.Vector;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import fr.sanofi.fcl4transmart.model.classes.TransferProcess;
import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class SelectFilesUI implements WorkItf {
	private DataTypeItf dataType;
	private ListViewer viewer;
	private Text pathField;
	private String path;
	public SelectFilesUI(DataTypeItf dataType){
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
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);
		
		ScrolledComposite scroller=new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		gd=new GridLayout();
		gd.numColumns=1;
		gd.horizontalSpacing=0;
		gd.verticalSpacing=0;
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		scroller.setLayoutData(gridData);
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		scrolledComposite.setLayoutData(gridData);
		
		if(((FilesTransfer)this.dataType).getRemotePath()==null || ((FilesTransfer)this.dataType).getRemotePath().compareTo("")==0){
			return composite;
		}

		Composite pathPart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		pathPart.setLayout(layout);
		pathPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label pathLabel=new Label(pathPart, SWT.NONE);
		pathLabel.setText("Path: ");
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		pathLabel.setLayoutData(gridData);
		this.pathField=new Text(pathPart, SWT.BORDER);
		this.pathField.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				path=pathField.getText();
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint=150;
		gridData.grabExcessHorizontalSpace = true;
		this.pathField.setLayoutData(gridData);
		Button browse=new Button(pathPart, SWT.PUSH);
		browse.setText("Browse");
		browse.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				FileDialog fd=new FileDialog(new Shell(), SWT.MULTI);
				fd.open();
				String[] filenames=fd.getFileNames();
				String filterPath=fd.getFilterPath(); 
				path="";
				for(int i=0; i<filenames.length; i++){
					if(path.compareTo("")==0){
						if(filterPath!=null && filterPath.trim().length()>0){
							path+=filterPath+File.separator+filenames[i];
						}
						else{
							path+=filenames[i];
						}
					}
					else{
						if(filterPath!=null && filterPath.trim().length()>0){
							path+="?"+filterPath+File.separator+filenames[i];
						}
						else{
							path+="?"+filenames[i];
						}
					}
				}
				pathField.setText(path);
			}		
		});
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		browse.setLayoutData(gridData);
						
		Button add=new Button(scrolledComposite, SWT.PUSH);
		add.setText("Add files");
		add.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {
				String[] paths=pathField.getText().split("\\?");
				for(int i=0; i<paths.length; i++){
					if(paths[i].compareTo("")!=0){
						File file=new File(paths[i]);
						if(file.exists()){
							for(File f: ((FilesTransfer)dataType).getFiles()){
								if(f.getName().compareTo(file.getName())==0){
									displayMessage("A file named "+file.getName()+" has already been added in the list.");
									return;
								}
							}
							((FilesTransfer)dataType).addFile(file);
						}
						else{
							displayMessage("The file "+paths[i]+" does not exist.");
						}
					}
				}
				viewer.setInput(((FilesTransfer)dataType).getFiles());
				displayNames();
			}
		});
		
		Label filesLabel=new Label(scrolledComposite, SWT.NONE);
		filesLabel.setText("Files to transfer:");
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		filesLabel.setLayoutData(gridData);
		
		Composite filesPart=new Composite(scrolledComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		filesPart.setLayout(layout);
		filesPart.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.viewer=new ListViewer(filesPart);
		this.viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		this.viewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("rawtypes")
				Vector v = (Vector)inputElement;
				return v.toArray();
			}
			public void dispose() {
				// nothing to do

			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// nothing to do
				
			}
		});	
		this.viewer.setInput(((FilesTransfer)this.dataType).getFiles());
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		gridData.heightHint=125;
		this.viewer.getControl().setLayoutData(gridData);
		this.displayNames();
		
		Button remove=new Button(filesPart, SWT.PUSH);
		remove.setText("Remove selected files");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		remove.setLayoutData(gridData);
		remove.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				String[] paths=viewer.getList().getSelection();
				for(int i=0; i<paths.length; i++){
					((FilesTransfer)dataType).removeFile(new File(paths[i]));
				}
				viewer.setInput(((FilesTransfer)dataType).getFiles());
				displayNames();
			}
			
		});
		
		Button load=new Button(scrolledComposite, SWT.PUSH);
		load.setText("Load files");
		gridData=new GridData(SWT.CENTER);
		load.setLayoutData(gridData);
		
		load.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				if(((FilesTransfer)dataType).getRemotePath()==null || ((FilesTransfer)dataType).getRemotePath().compareTo("")==0){
					displayMessage("Please select an analyses first");
					return;
				}
				if(!TransferProcess.testConnection()) return;
				((FilesTransfer)dataType).setProcesses();
				((FilesTransfer)dataType).runProcesses();
			}
		});
			    
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
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public void displayNames(){
		for(int i=0; i<this.viewer.getList().getItemCount(); i++){
			this.viewer.getList().setItem(i, ((File)this.viewer.getElementAt(i)).getName());
		}
	}

}

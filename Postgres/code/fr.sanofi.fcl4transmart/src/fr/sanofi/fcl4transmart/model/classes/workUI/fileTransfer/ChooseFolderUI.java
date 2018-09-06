package fr.sanofi.fcl4transmart.model.classes.workUI.fileTransfer;

import java.util.Vector;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
import fr.sanofi.fcl4transmart.controllers.fileTransfer.FolderContentProvider;
import fr.sanofi.fcl4transmart.model.classes.FolderNode;
import fr.sanofi.fcl4transmart.model.classes.FoldersTree;
import fr.sanofi.fcl4transmart.model.classes.dataType.FilesTransfer;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class ChooseFolderUI implements WorkItf {
	private DataTypeItf dataType;
	private Display display;
	private boolean isSearching;
	private TreeViewer viewer;
	private FoldersTree folders;
	boolean treeBuilt;
	boolean began;
	public ChooseFolderUI(DataTypeItf dataType){
		this.dataType=dataType;
		this.treeBuilt=false;
		this.began=false;
	}
	@Override
	public Composite createUI(Composite parent) {
		if(this.began) return null;
		this.began=true;
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
				folders=new FoldersTree();
				treeBuilt=RetrieveFm.buildTree(folders, true);
				isSearching=false;
			}
        }.start();
        while(this.isSearching){
        	if (!display.readAndDispatch()) {
                display.sleep();
              }	
        }
		shell.close();
		String message=RetrieveFm.getMessage();
		
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
		
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		gd=new GridLayout();
		gd.numColumns=1;
		body.setLayout(gd);
		
		if(!this.treeBuilt){
			if(message!=null && message.compareTo("")!=0){
				Label label=new Label(body, SWT.NONE);
				label.setText(message);
			}
			scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			return composite;
		}
		
		//display name instead of id
		String folderName=((FilesTransfer)this.dataType).getFolderName();
		if(folderName==null || folderName.compareTo("")==0){
			Label label=new Label(body, SWT.NONE);
			label.setText("No selected folder");
		}else{
			Label label=new Label(body, SWT.NONE);
			label.setText("Selected folder: "+folderName);
		}
		
		viewer = new TreeViewer(body, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new FolderContentProvider());
		viewer.setAutoExpandLevel(0);

		viewer.setInput(this.folders);

		String path=((FilesTransfer)this.dataType).getRemotePath();
		if(path!=null && path.compareTo("")!=0){
			Object elements[]=((FolderContentProvider)viewer.getContentProvider()).getElement(String.valueOf(RetrieveFm.getStudyId()));
			if(elements!=null) viewer.setExpandedElements(elements);
		}
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
		    	if(((FilesTransfer)dataType).getRemotePath()!=null && String.valueOf(((FolderNode)element).getId()).compareTo(((FilesTransfer)dataType).getRemotePath())==0){
		    		return new Color(Display.getCurrent(), 237, 91, 67);
		    	}
		    	else if(((FolderNode)element).getType().compareTo("FOLDER")==0){
		    		return new Color(Display.getCurrent(), 192, 192, 192);
		    	}
		    	return null;
		    }
		});
		
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("Choose selected folder");
		ok.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
				FolderNode node=null;
				if(selection.iterator().hasNext()){
					node=(FolderNode)selection.iterator().next();
				}else{
					displayMessage("Please first select a folder");
					return;
				}if(((FolderNode)node).getType().compareTo("FOLDER")!=0){
					displayMessage("This node is not a folder");
					return;
				}
				((FilesTransfer)dataType).setRemotePath(String.valueOf(((FolderNode)node).getId()));
				((FilesTransfer)dataType).setFolderName(String.valueOf(((FolderNode)node).getName()));
				displayMessage("Folder "+((FolderNode)node).getName()+" has been chosen");
				viewer.setInput(folders);
				viewer.setSelection(StructuredSelection.EMPTY);
				WorkPart.updateSteps();
			}
		});
		gridData=new GridData(SWT.CENTER);
		ok.setLayoutData(gridData);
	   	    
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		this.began=false;
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
		if(message==null){
			return;
		}
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

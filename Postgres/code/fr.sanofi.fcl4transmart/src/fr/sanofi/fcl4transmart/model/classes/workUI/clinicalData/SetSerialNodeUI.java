package fr.sanofi.fcl4transmart.model.classes.workUI.clinicalData;

import java.io.BufferedReader;
import java.io.FileReader;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.clinicalData.SerialNodeListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.ClinicalData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
import fr.sanofi.fcl4transmart.ui.parts.WorkPart;

public class SetSerialNodeUI implements WorkItf {
	private DataTypeItf dataType;
	private Vector<String> categories;
	private Vector<String> labels;
	private Vector<String> values;
	private Vector<String> units;
	private Vector<Text> valuesText;
	private Vector<Text> unitsText;
	private boolean isSearching;
	public SetSerialNodeUI(DataTypeItf dataType){
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
		scroller.setLayout(gd);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
				initiate();
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
			
		Composite body=new Composite(scrolledComposite, SWT.NONE);
		gd=new GridLayout();
		gd.numColumns=4;
		gd.horizontalSpacing=5;
		gd.verticalSpacing=5;
		body.setLayout(gd);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label l1=new Label(body, SWT.NONE);
		l1.setText("Category code");
		Label l2=new Label(body, SWT.NONE);
		l2.setText("Label");
		Label l3=new Label(body, SWT.NONE);
		l3.setText("Value");
		Label l4=new Label(body, SWT.NONE);
		l4.setText("Units");
		
		for(int i=0; i<this.categories.size(); i++){
			Text t1=new Text(body, SWT.BORDER);
			t1.setEditable(false);
			t1.setText(this.categories.get(i));
			GridData gridData = new GridData();
			gridData.widthHint=200;
			t1.setLayoutData(gridData);
			
			Text t2=new Text(body, SWT.BORDER);
			t2.setEditable(false);
			t2.setText(this.labels.get(i));
			gridData = new GridData();
			gridData.widthHint=200;
			t2.setLayoutData(gridData);
			
			Text t3=new Text(body,SWT.BORDER);
			t3.setText(this.values.get(i));
			t3.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=valuesText.indexOf(e.getSource());
					values.setElementAt(valuesText.elementAt(n).getText(), n);
				}
			});
			gridData = new GridData();
			gridData.widthHint=100;
			t3.setLayoutData(gridData);
			this.valuesText.add(t3);
			
			Text t4=new Text(body,SWT.BORDER);
			t4.setText(this.units.get(i));
			t4.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=unitsText.indexOf(e.getSource());
					units.setElementAt(unitsText.elementAt(n).getText(), n);
				}
			});
			gridData = new GridData();
			gridData.widthHint=100;
			t4.setLayoutData(gridData);
			this.unitsText.add(t4);
			
		}
		Button ok=new Button(scrolledComposite, SWT.PUSH);
		ok.setText("OK");
		ok.addListener(SWT.Selection, new SerialNodeListener(this, this.dataType));
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}

	private void initiate(){
		this.categories=new Vector<String>();
		this.valuesText=new Vector<Text>();
		this.unitsText=new Vector<Text>();
		this.values=new Vector<String>();
		this.units=new Vector<String>();
		this.labels=new Vector<String>();
		Vector<String[]> cat=FileHandler.getFullClinicalCategoryCode(((ClinicalData)dataType).getCMF(), ((ClinicalData)dataType).getRawFiles());
		for(int i=0; i<cat.size(); i++){
			this.categories.add(cat.get(i)[0]);
			this.labels.add(cat.get(i)[1]);
			this.values.add("");
			this.units.add("");
		}
		
		if(((ClinicalData)this.dataType).getDimFile()!=null){
			try{
				BufferedReader br = new BufferedReader(new FileReader(((ClinicalData)dataType).getDimFile()));
				String line=br.readLine();
				while ((line=br.readLine())!=null){
					String[] fields=line.split("\t", -1);
					for(int i=0; i<cat.size(); i++){
						if(cat.get(i)[0].compareTo(fields[0])==0 && cat.get(i)[1].compareTo(fields[2])==0){
							this.values.set(i, fields[1]);
							this.units.set(i,fields[3]);
							break;
						}
					}
				}
				br.close();
			}catch (Exception e){
				displayMessage("Error: "+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public boolean canPaste() {
		return true;
	}

	@Override
	public Vector<Vector<String>> copy() {
		Vector<Vector<String>> v=new Vector<Vector<String>>();
		v.add(this.categories);
		v.add(this.labels);
		v.add(this.values);
		v.add(this.units);
		return v;
	}

	@Override
	public void paste(Vector<Vector<String>> data) {
		if(data.size()<1) return;
		int l=values.size();
		if(data.get(0).size()<l) l=data.get(0).size();
		for(int i=0; i<l; i++){
			this.values.set(i, data.get(0).get(i));
			this.valuesText.get(i).setText(data.get(0).get(i));
			if(data.size()>1 && data.get(1).size()>i){
				this.units.set(i, data.get(1).get(i));
				this.unitsText.get(i).setText(data.get(1).get(i));
			}
		}		
	}

	@Override
	public void mapFromClipboard(Vector<Vector<String>> data) {
		if(data.size()<2) return;
		for(int i=0; i<data.get(0).size(); i++){
			int index=-1;
			for(int j=0; j<this.categories.size(); j++){
				if(data.get(0).get(i).compareTo(this.categories.get(j))==0 && data.get(2).get(i).compareTo(this.labels.get(j))==0){
					index=j;
					System.out.println("index: "+index);
					break;
				}
			}
			if(index!=-1){
				if(data.get(2).size()>i){
					this.values.set(index, data.get(1).get(i));
					this.valuesText.get(index).setText(data.get(1).get(i));
				}
				if(data.size()>1 && data.get(3).size()>i){
					this.units.set(index, data.get(3).get(i));
					this.unitsText.get(index).setText(data.get(3).get(i));
				}
			}
		}
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public Vector<String> getCategoriesCode(){
		return this.categories;
	}
	public Vector<String> getLabels(){
		return this.labels;
	}
	public Vector<String> getValues(){
		return this.values;
	}
	public Vector<String> getUnits(){
		return this.units;
	}
}

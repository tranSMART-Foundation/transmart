package fr.sanofi.fcl4transmart.model.classes.workUI.metabolomicsData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import fr.sanofi.fcl4transmart.controllers.FileHandler;
import fr.sanofi.fcl4transmart.controllers.listeners.metabolomicsData.MetabolomicsColumnMappingListener;
import fr.sanofi.fcl4transmart.model.classes.dataType.MetabolomicsData;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;

public class MetabolomicsSetColumnMappingUI implements WorkItf{
	private DataTypeItf dataType;
	private Vector<Vector<String>> headers;
	private Vector<Combo> peptideIdCombos;
	private Vector<Combo> valuesStartCombos;
	private Vector<Combo> valuesEndCombos;
	private Vector<String> peptideId;
	private Vector<String> valuesStart;
	private Vector<String> valuesEnd;
	public MetabolomicsSetColumnMappingUI(DataTypeItf dataType){
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
		scrolledComposite.setLayout(layout);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Vector<File> files=((MetabolomicsData)this.dataType).getRawFiles();
		for(int i=0; i<files.size(); i++){
			Label fileName=new Label(scrolledComposite, SWT.NONE);
			fileName.setText("File: "+files.get(i).getName());
			
			Composite lines=new Composite(scrolledComposite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.horizontalSpacing=5;
			layout.verticalSpacing=5;
			layout.marginLeft=20;
			lines.setLayout(layout);
			lines.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label label1=new Label(lines, SWT.NONE);
			label1.setText("Biochemical name");
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			label1.setLayoutData(gridData);
			
			Combo peptideCombo=new Combo(lines, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
			peptideCombo.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 
			peptideCombo.setText(this.peptideId.elementAt(i));
		    this.peptideIdCombos.add(peptideCombo);
		    peptideCombo.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=peptideIdCombos.indexOf(e.getSource());
					peptideId.setElementAt(peptideIdCombos.elementAt(n).getText(), n);
				}
			});
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			peptideCombo.setLayoutData(gridData);
			

			Label label2=new Label(lines, SWT.NONE);
			label2.setText("Intensity value start");
			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			label2.setLayoutData(gridData);
			
			Combo valuesStartCombo=new Combo(lines, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
			valuesStartCombo.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 
			valuesStartCombo.setText(this.valuesStart.elementAt(i));
		    this.valuesStartCombos.add(valuesStartCombo);
		    valuesStartCombo.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=valuesStartCombos.indexOf(e.getSource());
					valuesStart.setElementAt(valuesStartCombos.elementAt(n).getText(), n);
				}
			});
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			valuesStartCombo.setLayoutData(gridData);
			
			Label label3=new Label(lines, SWT.NONE);
			label3.setText("Intensity value end");
			gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			label3.setLayoutData(gridData);
			
			Combo valuesEndCombo=new Combo(lines, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
			valuesEndCombo.addListener(SWT.KeyDown, new Listener(){ 
		    	public void handleEvent(Event event) { 
		    		event.doit = false; 
		    	} 
	    	}); 
			valuesEndCombo.setText(this.valuesEnd.elementAt(i));
		    this.valuesEndCombos.add(valuesEndCombo);
		    valuesEndCombo.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					int n=valuesEndCombos.indexOf(e.getSource());
					valuesEnd.setElementAt(valuesEndCombos.elementAt(n).getText(), n);
				}
			});
		    gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.widthHint=100;
			gridData.grabExcessHorizontalSpace = true;
			valuesEndCombo.setLayoutData(gridData);
			
			for(String s: FileHandler.getHeaders(files.elementAt(i))){
		    	this.peptideIdCombos.elementAt(i).add(s);
		    	this.valuesStartCombos.elementAt(i).add(s);
		    	this.valuesEndCombos.elementAt(i).add(s);
		    }
			
		}
		Button button=new Button(scrolledComposite, SWT.PUSH);
		button.setText("Ok");
		button.addListener(SWT.Selection, new MetabolomicsColumnMappingListener(this.dataType, this));
		
		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public void initiate(){
		this.peptideId=new Vector<String>();
		this.valuesStart=new Vector<String>();
		this.valuesEnd=new Vector<String>();
		this.peptideIdCombos=new Vector<Combo>();
		this.valuesStartCombos=new Vector<Combo>();
		this.valuesEndCombos=new Vector<Combo>();
		
		this.headers=new Vector<Vector<String>>();
		Vector<File> files=((MetabolomicsData)this.dataType).getRawFiles();
		for(File file: files){
			this.headers.add(FileHandler.getHeaders(file));
		}
		if(((MetabolomicsData)this.dataType).getColumnMappingFile()==null){
			for(@SuppressWarnings("unused") File file: files){
				this.peptideId.add("");
				this.valuesStart.add("");
				this.valuesEnd.add("");
			}
		}else{
			int i=0;
			for(File file: files){
				boolean found=false;
				try{
					BufferedReader br=new BufferedReader(new FileReader(((MetabolomicsData)this.dataType).getColumnMappingFile()));
					String line=br.readLine();
					while ((line=br.readLine())!=null){
						String[] s=line.split("\t", -1);
						if(s[0].compareTo(file.getName())==0){
							this.peptideId.add(headers.get(i).get(Integer.valueOf(s[1])-1));
							this.valuesStart.add(headers.get(i).get(Integer.valueOf(s[2])));
							this.valuesEnd.add(headers.get(i).get(Integer.valueOf(s[3])));
							found=true;
							break;
						}
					}
					if(!found){
						this.peptideId.add("");
						this.valuesStart.add("");
						this.valuesEnd.add("");
					}
					br.close();
				}catch(Exception e){
					this.displayMessage("Error reading column mapping file");
				}
				i++;
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
	public Vector<String> getPeptideId(){
		return this.peptideId;
	}
	public Vector<String> getValuesStart(){
		return this.valuesStart;
	}
	public Vector<String> getValuesEnd(){
		return this.valuesEnd;
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
}

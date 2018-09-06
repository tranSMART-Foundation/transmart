/*******************************************************************************
 * Copyright (c) 2012 Sanofi-Aventis Recherche et Developpement.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *    Sanofi-Aventis Recherche et Developpement - initial API and implementation
 ******************************************************************************/
package fr.sanofi.fcl4transmart.handlers;

import java.util.HashMap;
import java.util.Vector;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import fr.sanofi.fcl4transmart.controllers.RetrieveFm;
/**
 *This class controls the new study menu item
 */	
@SuppressWarnings("restriction")
public class NewStudyHandler {
	private Shell shell;
	private Combo combo;
	private HashMap<String, String>  experiments;
	@Inject private static IEventBroker eventBroker;
	@Execute
	public void execute(Display display) {
		//eventBroker.send("newStudy/syncEvent", "new study");
		experiments=RetrieveFm.getExperiments();
		if(experiments==null){
			displayMessage(RetrieveFm.getMessage());
			return;
		}
		if(experiments.size()==0){
			displayMessage("There is no study in tranSMART");
			return;
		}
		this.shell=new Shell(SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX | SWT.RESIZE);
	    this.shell.setMinimumSize(250,250);
	    this.shell.setText("Add a study from tranSMART");
	    GridLayout gridLayout=new GridLayout();
		gridLayout.numColumns=1;
		this.shell.setLayout(gridLayout);
						
		Composite body=new Composite(shell, SWT.NONE);
		gridLayout=new GridLayout();
		gridLayout.numColumns=2;
		body.setLayout(gridLayout);
		GridData gridData=new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace=true;
		body.setLayoutData(gridData);
		
		Label label=new Label(body, SWT.NONE);
		label.setText("Choose a study");
		
		this.combo=new Combo(body, SWT.DROP_DOWN | SWT.BORDER | SWT.WRAP);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint=100;
		this.combo.setLayoutData(gridData);
		
		this.combo.addListener(SWT.KeyDown, new Listener(){ 
	    	public void handleEvent(Event event) { 
	    		event.doit = false; 
	    	} 
    	}); 
		
		for(String s: this.sort(experiments.keySet().toArray())){
			this.combo.add(s);
		}
		
		Composite buttonPart=new Composite(this.shell, SWT.NONE);
		GridLayout gl=new GridLayout();
		gl.numColumns=2;
		gl.horizontalSpacing=10;
		buttonPart.setLayout(gl);
		gridData=new GridData(GridData.FILL_BOTH);
		buttonPart.setLayoutData(gridData);
		
		Button ok=new Button(buttonPart, SWT.PUSH);
		ok.setText("OK");
		GridData gd=new GridData();
		gd.widthHint=100;
		ok.setLayoutData(gd);
		ok.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				eventBroker.send("addStudyFromTransmart/syncEvent", experiments.get(combo.getText()));
				shell.close();
			}
		});
        
		Button cancel=new Button(buttonPart, SWT.PUSH);
		cancel.setText("Cancel");
		gd=new GridData();
		gd.widthHint=100;
		cancel.setLayoutData(gd);
		cancel.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event) {
				shell.close();
			}
		});
		
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		this.shell.open();
		while(!shell.isDisposed()){
	    	if (!display.readAndDispatch()) {
	            display.sleep();
	          }
	    }
	}
	public void displayMessage(String message){
	    int style = SWT.ICON_INFORMATION | SWT.OK;
	    MessageBox messageBox = new MessageBox(new Shell(), style);
	    messageBox.setMessage(message);
	    messageBox.open();
	}
	public Vector<String> sort(Object[] sort) {
        Vector<String> v = new Vector<String>();
        for(int count = 0; count < sort.length; count++) {
            String s = sort[count].toString();
            int i = 0;
            for (i = 0; i < v.size(); i++) {
                int c = s.compareTo((String) v.elementAt(i));
                if (c < 0) {
                    v.insertElementAt(s, i);
                    break;
                } else if (c == 0) {
                    break;
                }
            }
            if (i >= v.size()) {
                v.addElement(s);
            }
        }
        return v;
    }
}

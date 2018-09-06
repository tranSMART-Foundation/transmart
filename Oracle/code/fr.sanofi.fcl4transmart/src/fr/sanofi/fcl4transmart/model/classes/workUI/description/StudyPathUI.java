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
package fr.sanofi.fcl4transmart.model.classes.workUI.description;

import java.util.Vector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import fr.sanofi.fcl4transmart.model.interfaces.StudyItf;
import fr.sanofi.fcl4transmart.model.interfaces.WorkItf;
/**
 *This class allows the creation of the composite to change the study name
 */
public class StudyPathUI implements WorkItf{
	private StudyItf study;
	private Text nameField;
	private String name;
	public StudyPathUI(StudyItf study){
		this.study=study;
		this.name=this.study.getPath().getAbsolutePath();
	}
	@Override
	public Composite createUI(Composite parent){
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
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scroller.setLayoutData(gridData);
		
		Composite scrolledComposite=new Composite(scroller, SWT.NONE);
		scroller.setContent(scrolledComposite); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		scrolledComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		scrolledComposite.setLayoutData(gridData);
		
		Label lab=new Label(scrolledComposite, SWT.NONE);
		lab.setText("Path for "+this.study.toString()+": ");
		
		this.nameField=new Text(scrolledComposite, SWT.BORDER);
		this.nameField.setEditable(false);
		this.nameField.setText(this.name);
		gridData = new GridData();
		gridData.widthHint=250;
		this.nameField.setLayoutData(gridData);

		scrolledComposite.setSize(scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return composite;
	}
	public String getName(){
		return this.nameField.getText();
	}
	public void displayMessage(String message){
		int style = SWT.ICON_WARNING | SWT.OK;
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
}

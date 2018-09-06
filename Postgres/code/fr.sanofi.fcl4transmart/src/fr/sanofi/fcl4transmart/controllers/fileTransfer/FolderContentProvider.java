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
package fr.sanofi.fcl4transmart.controllers.fileTransfer;

import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.sanofi.fcl4transmart.model.classes.FolderNode;
import fr.sanofi.fcl4transmart.model.classes.FoldersTree;
/**
 *This class represents a tree node
 */	
public class FolderContentProvider implements ITreeContentProvider{
	private FoldersTree foldersTree;
	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.foldersTree=(FoldersTree)newInput;	
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return this.foldersTree.getRootToArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return ((FolderNode)parentElement).getChildren().toArray();
	}

	@Override
	public Object getParent(Object element) {
		return ((FolderNode)element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		if(((FolderNode)element).getChildren().size()>0){
			return true;
		}
		return false;
	}
	public Object[] getElement(String id){
		FolderNode target=null;
		Vector<Object> elements=new Vector<Object>();
		for(FolderNode root: this.foldersTree.getRoots()){
			target=this.getElementRecursive(root, id);
			if(target!=null){
				getPath(target, elements);
			}
		}
		if(elements.size()<1) return null;
		return elements.toArray();
	}
	public FolderNode getElementRecursive(FolderNode node, String id){
		if(String.valueOf(node.getId()).compareTo(id)==0){
			return node;
		}
		FolderNode target=null;
		for(FolderNode child: node.getChildren()){
			target=getElementRecursive(child, id);
			if(target!=null) return target;
		}
		return target;
	}
	public void getPath(FolderNode node, Vector<Object> elements){
		elements.add(node);
		if(node.getParent()!=null){
			getPath(node.getParent(), elements);
		}
	}
}

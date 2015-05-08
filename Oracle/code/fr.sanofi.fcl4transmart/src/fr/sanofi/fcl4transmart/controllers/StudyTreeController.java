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
package fr.sanofi.fcl4transmart.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import fr.sanofi.fcl4transmart.model.classes.TreeNode;
import fr.sanofi.fcl4transmart.model.interfaces.DataTypeItf;
/**
 *Controls a study tree item for clinical data
 */	
public class StudyTreeController {
	private TreeNode root;
	private DataTypeItf dataType;
	public StudyTreeController(TreeNode root, DataTypeItf dataType){
		this.root=root;
		this.dataType=dataType;
	}
	/**
	 *Creates the tree from a column mapping file
	 */	
	public TreeNode buildTree(File file){
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line=br.readLine();
			while ((line=br.readLine())!=null){
				String[] s=line.split("\t", -1);
				File rawFile=new File(this.dataType.getPath()+File.separator+s[0]);
				String header=FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[2]));
				String name=s[0]+" - "+header;
				if(s[3].compareTo("\\")==0){
					String sourceName=rawFile.getName()+" - "+FileHandler.getColumnByNumber(rawFile, Integer.parseInt(s[4].substring(0, 1)));
					this.buildNode(this.root, s[1], name, sourceName, false);
				}
				else if(s[4].compareTo("MIN")==0 || s[4].compareTo("MAX")==0 || s[4].compareTo("MEAN")==0){
					name=s[4]+": "+name;
					this.buildNode(this.root, s[1], name, "", true);
				}
				else if(s[3].compareTo("OMIT")!=0 && s[3].compareTo("SUBJ_ID")!=0 && s[3].compareTo("VISIT_NAME")!=0 && s[3].compareTo("VISIT_NAME_2")!=0 && s[3].compareTo("SITE_ID")!=0 && s[3].compareTo("UNITS")!=0&& s[3].compareTo("VISIT_DATE")!=0 && s[3].compareTo("ENROLL_DATE")!=0){
					this.buildNode(this.root, s[1], name, "", false);
				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return this.root;
	}
	/**
	 *Creates a study node (recursive method)
	 */	
	private void buildNode(TreeNode node, String path, String label, String dataLabelSource, boolean isOperation){
		String[] splitedPath=path.split("\\+", 2);
		TreeNode child=node.getChild(splitedPath[0].replace('_', ' '));
		if(child==null){
			child=new TreeNode(splitedPath[0].replace('_', ' '), node, false);
			node.addChild(child);
		}
		if(splitedPath.length>1){
			this.buildNode(child, splitedPath[1], label, dataLabelSource, isOperation);
		}
		else{
			if(dataLabelSource.compareTo("")==0){
				if(child.getChild(label)==null){
					TreeNode newNode=new TreeNode(label, child, true);
					newNode.setIsOperation(isOperation);
					child.addChild(newNode);
				}
			}
			else{
				if(child.getChild(dataLabelSource)==null){
					child.addChild(new TreeNode(dataLabelSource, child, true));
				}
				if(child.getChild(dataLabelSource).getChild(label)==null){
					child.getChild(dataLabelSource).addChild(new TreeNode(label, child.getChild(dataLabelSource), true));
				}
			}
		}
	}
}

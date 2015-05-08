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
package fr.sanofi.fcl4transmart.controllers.listeners.rnaSeqData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.sanofi.fcl4transmart.handlers.PreferencesHandler;
import fr.sanofi.fcl4transmart.model.classes.workUI.rnaSeqData.LoadAnnotationUI;

/**
 *This class controls the platform annotation checking step
 */	
public class CheckAnnotationListener implements Listener{
	private LoadAnnotationUI ui;
	private String platformId;
	private boolean isLoaded;
	public CheckAnnotationListener(LoadAnnotationUI ui){
		this.ui=ui;
	}
	@Override
	public void handleEvent(Event event) {
		this.platformId=this.ui.getPlatformId();
		this.ui.openSearchingShell();
		new Thread(){
			public void run() {
				try{
					Class.forName("oracle.jdbc.driver.OracleDriver");
					String connectionString="jdbc:oracle:thin:@"+PreferencesHandler.getDbServer()+":"+PreferencesHandler.getDbPort()+":"+PreferencesHandler.getDbName();
					Connection con = DriverManager.getConnection(connectionString, PreferencesHandler.getDeappUser(), PreferencesHandler.getDeappPwd());
					Statement stmt = con.createStatement();
				    ResultSet rs = stmt.executeQuery("SELECT distinct platform from de_gpl_info where platform='"+platformId+"'");
		
				    if(rs.next()){
				    	isLoaded=true;
				    }
				    else{
				    	isLoaded=false;
				    }			
				}catch(SQLException sqle){
					ui.displayMessage("SQL error: "+sqle.getLocalizedMessage());
					sqle.printStackTrace();
					isLoaded=false;
				}
				catch(ClassNotFoundException cnfe){
					ui.displayMessage("Java error: Class not found exception");
					cnfe.printStackTrace();
					isLoaded=false;
				}
				ui.setIsSearching(false);
			}
		}.start();
		this.ui.waitForSearchingThread();
		if(this.isLoaded){
	    	ui.displayLoaded();
		}
		else{
	    	ui.addLoadPart(true);	
		}
	}
	

}

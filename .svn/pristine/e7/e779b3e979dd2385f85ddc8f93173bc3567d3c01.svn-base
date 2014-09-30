package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.userpref.UserPreferences;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.modelselection.ModelSelectionPanel;
import com.pfizer.mrbt.genomics.state.State;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author henstockpv
 */
public class Singleton {
    private static State state;
    private static DataModel dataModel;
    private static UserPreferences userPref;
    private static MainPanel mainPanel;
    private static ModelSelectionPanel modelSelectionPanel;
    
    public static State getState() {
        if(state == null) {
            state = new State();
        }
        return state;
    }

    
    public static DataModel getDataModel() {
        if(dataModel == null) {
            dataModel = new DataModel();
        }
        return dataModel;
    }
    
    public static UserPreferences getUserPreferences() {
        if(userPref == null) {
            userPref = new UserPreferences();
        }
        return userPref;
    }
    
    /**
     * Using this reluctantly as cannot figure how to reference an option pnae
     * within another option pane
     * @return 
     */
    public static MainPanel getMainPanel() {
        if(mainPanel == null) {
            mainPanel = new MainPanel();
        }
        return mainPanel;
    }
    
    /**
     * Using this reluctantly as need the table in the main panel to access 
     * the selected rows of th emodel selection panel
     * @return 
     */
    public static ModelSelectionPanel getModelSelectionPanel() {
        if(modelSelectionPanel == null) {
            modelSelectionPanel = new ModelSelectionPanel();
        }
        return modelSelectionPanel;
    }
    
}

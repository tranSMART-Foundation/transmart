/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.query.QueryPanel;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 *
 * @author henstock
 */
public class TabbedPanel extends JTabbedPane {

    private JComponent queryPanel;
    private JComponent mainPanel;

    public TabbedPanel() {
        super();
        addTab("Query", null, getQueryPanel(), "Provides queries into the database");
        addTab("Results", null, getMainPanel(), "Shows the Manhattan Plot results");

    }

    protected JComponent getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new MainPanel();
        }
        return mainPanel;
    }

    protected JComponent getQueryPanel() {
        if (queryPanel == null) {
            queryPanel = new QueryPanel();
        }
        return queryPanel;
    }
}

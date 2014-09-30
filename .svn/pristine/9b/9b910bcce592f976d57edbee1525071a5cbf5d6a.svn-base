/*
 * Main panel for viewing PlotPanels
 */
package com.pfizer.mrbt.genomics;


import com.pfizer.mrbt.genomics.resultstable.ResultsTablePanel;
import com.pfizer.mrbt.genomics.modelselection.ModelSelectionPanel;
import com.pfizer.mrbt.genomics.query.QueryPanel;
import com.pfizer.mrbt.genomics.state.StateListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author henstockpv
 */
public class MainPanel extends JComponent {

    private DisplayPlotControlPanel displayPlotControlPanel;
    private ModelSelectionPanel modelSelectionPanel = null;
    private JTabbedPane tabbedPanel;
    private QueryPanel queryPanel;
    private ResultsTablePanel resultsTablePanel;

    public MainPanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(getTabbedPanel(), gbc);

        gbc.gridx = 20;
        gbc.gridy = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        //add(getModelSelectionPanel(), gbc);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
    }

    protected JComponent getTabbedPanel() {
        if (tabbedPanel == null) {
            tabbedPanel = new JTabbedPane();
            Font currFont = tabbedPanel.getFont();
            tabbedPanel.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize() + 4));
            tabbedPanel.addTab("Query", null, getQueryPanel(), "Provides queries into the database");
            tabbedPanel.addTab("Results", null, getDisplayPlotControlPanel(), "Shows the Manhattan Plot results");
            tabbedPanel.addTab("Table", null, getResultsTablePanel(), "Shows all the loaded results");
            tabbedPanel.setSelectedIndex(0);
            tabbedPanel.setForegroundAt(tabbedPanel.getSelectedIndex(), Color.RED);
            tabbedPanel.setBackgroundAt(tabbedPanel.getSelectedIndex(), Color.WHITE);
            tabbedPanel.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    for (int i = 0; i < 3; i++) {
                        tabbedPanel.setForegroundAt(i, Color.BLACK);
                        tabbedPanel.setBackgroundAt(i, Color.LIGHT_GRAY);
                    }
                    tabbedPanel.setForegroundAt(tabbedPanel.getSelectedIndex(), Color.RED);
                    tabbedPanel.setBackgroundAt(tabbedPanel.getSelectedIndex(), Color.WHITE);
                }
            });

        }
        return tabbedPanel;
    }

    protected JComponent getQueryPanel() {
        if (queryPanel == null) {
            queryPanel = new QueryPanel();
        }
        return queryPanel;
    }

    protected JComponent getResultsTablePanel() {
        if (resultsTablePanel == null) {
            resultsTablePanel = new ResultsTablePanel();
        }
        return resultsTablePanel;
    }

    protected DisplayPlotControlPanel getDisplayPlotControlPanel() {
        if (displayPlotControlPanel == null) {
            displayPlotControlPanel = new DisplayPlotControlPanel();
        }
        return displayPlotControlPanel;
    }

    /**
     * Copies the annotation panel and the main plot to the clipboard
     */
    public void capturePlotPanelToClipboard() {
        displayPlotControlPanel.capturePlotPanelToClipboard();
    }

    /**
     * Writes the annotation panel and the main plot to a jpg file
     */
    public void writePlotPanelToFile(File file) {
        displayPlotControlPanel.writePlotPanelToFile(file);
    }
    
    public class StateController implements StateListener {
        @Override
        public void currentChanged(ChangeEvent ce) {
            repaint();
        }

        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            tabbedPanel.setSelectedIndex(1);
        }

        @Override
        public void thumbnailsChanged(ChangeEvent ce) {
        }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) {
        }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) {
        }

        @Override
        public void averagingWindowChanged(ChangeEvent ce) {
        }
        
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { 
        
        }        
      public void heatmapChanged(ChangeEvent ce) { }
        
    }
}

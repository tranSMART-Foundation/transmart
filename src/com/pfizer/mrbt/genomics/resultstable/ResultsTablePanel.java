/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.resultstable;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.modelselection.ModelSelectionTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author henstock
 */
public class ResultsTablePanel extends JComponent {

    private JComponent controlPanel;
    private JTable resultsTable;
    private AbstractButton exportButton;
    private AbstractButton exportSelectionButton;
    private AbstractButton selectAllButton;
    private AbstractButton includeSelectedGeneModelsButton;
    private AbstractButton includeAllGeneModelsButton;
    private AbstractButton includeUnselectedGeneModelsButton;
    private AbstractButton removeSelectedRowsButton;
    private AbstractButton keepSelectedRowsButton;
    private AbstractTableModel resultsTableModel;
    private JComponent dataImportControlPanel;
    private JComponent modifyControlPanel;
    private JComponent exportPanel;
    private JTextField pvalThresholdTextField;
    private AbstractButton removeBelowThresholdButton;

    public ResultsTablePanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        JScrollPane tableScrollPane = new JScrollPane(getTableResults());
        Dimension dim = tableScrollPane.getPreferredSize();
        dim.setSize(150, dim.getHeight());
        tableScrollPane.setPreferredSize(dim);
        add(tableScrollPane, gbc);

        gbc.gridx = 20;
        gbc.gridy = 10;
        gbc.weighty = 0.1;
        gbc.weightx = 0.1;
        add(getControlPanel(), gbc);

    }

    protected JComponent getControlPanel() {
        if (controlPanel == null) {
            controlPanel = new JPanel();
            controlPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(5,5,5,5);
            controlPanel.add(getDataImportControlPanel(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            controlPanel.add(getModifyControlPanel(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 30;
            controlPanel.add(getExportPanel(), gbc);

        }
        return controlPanel;
    }

    /**
     * This panel deals with moving data in from the gene/model table
     * @return 
     */
    protected JComponent getDataImportControlPanel() {
        if (dataImportControlPanel == null)  {
            dataImportControlPanel = new JPanel();
            dataImportControlPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.insets = new Insets(5,5,5,5);
            gbc.fill = GridBagConstraints.BOTH;
  
            dataImportControlPanel.add(getIncludeAlGenelModelsButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            dataImportControlPanel.add(getIncludeSelectedGeneModelsButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 30;
            dataImportControlPanel.add(getIncludeUnselectedGeneModelsButton(), gbc);
            dataImportControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Data Import"));
        }
        return dataImportControlPanel;
    }

    /**
     * Panel that deals with the editing of an existing table to exclude rows
     * @return 
     */
    protected JComponent getModifyControlPanel() {
        if (modifyControlPanel == null)  {
            modifyControlPanel = new JPanel();
            modifyControlPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.insets = new Insets(5,5,5,5);
            gbc.gridwidth = 19;
            modifyControlPanel.add(getRemoveSelectedRowsButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            modifyControlPanel.add(getKeepSelectedRowsButton(), gbc);
            
            gbc.gridx = 10;
            gbc.gridy = 30;
            gbc.anchor = GridBagConstraints.SOUTH;
            modifyControlPanel.add(getRemoveBelowThresholdButton(), gbc);
            
            gbc.gridx = 10;
            gbc.gridy = 40;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            modifyControlPanel.add(new JLabel("-10log(P-value) Thresh:"), gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 40;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            pvalThresholdTextField = new JTextField(5);
            pvalThresholdTextField.setHorizontalAlignment(JTextField.CENTER);
            pvalThresholdTextField.setText("0.0");
            
            modifyControlPanel.add(pvalThresholdTextField, gbc);

            modifyControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Data Modification"));
}
        return modifyControlPanel;
    }
    
    
    /**
     * Panel that deals with the editing of an existing table to exclude rows
     * @return 
     */
    protected JComponent getExportPanel() {
        if (exportPanel == null)  {
            exportPanel = new JPanel();
            exportPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.insets = new Insets(5,5,5,5);
            exportPanel.add(getExportButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            exportPanel.add(getExportSelectionButton(), gbc);
            exportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Data Export"));

            gbc.gridx = 10;
            gbc.gridy = 30;
            exportPanel.add(getSelectAllButton(), gbc);
            exportPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Data Export"));
        }
        return exportPanel;
    }


    protected AbstractButton getExportButton() {
        if (exportButton == null) {
            exportButton = new JButton("Export All");
            exportButton.setToolTipText("Saves the table to a tab-delimited file");
            exportButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    TxtFileFilter txtFileFilter = new TxtFileFilter();
                    File file = getFilename();
                    if (file != null) {
                        exportData(file);
                    }
                }
            });
        }
        return exportButton;
    }

    /**
     * Returns a button with the ability to write out the selected rows in the
     * table to a specified file.
     *
     * @return
     */
    protected AbstractButton getExportSelectionButton() {
        if (exportSelectionButton == null) {
            exportSelectionButton = new JButton("Export Selected Rows");
            exportSelectionButton.setToolTipText("Export selected rows to tab-delimited file");
            exportSelectionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedRows = resultsTable.getSelectedRows();
                    if (selectedRows.length > 0) {
                        TxtFileFilter txtFileFilter = new TxtFileFilter();
                        File file = getFilename();
                        if (file != null) {
                            exportSelectedData(file);
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(ResultsTablePanel.this),
                                "No rows selected for export.  Use Export instead.",
                                "Export Error",
                                JOptionPane.WARNING_MESSAGE);

                    }
                }
            });
        }
        return exportSelectionButton;
    }

    /**
     * Returns a button with the ability to write out the selected rows in the
     * table to a specified file.
     *
     * @return
     */
    protected AbstractButton getSelectAllButton() {
        if (selectAllButton == null) {
            selectAllButton = new JButton("Select All");
            selectAllButton.setToolTipText("Selects table data so you can copy and paste into Excel");
            selectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    resultsTable.selectAll();
                }
            });
        }
        return selectAllButton;
    }

    /**
     * Asks the user for a filename for the particular file
     *
     * @return
     */
    public File getFilename() {
        JFileChooser fc = new JFileChooser(".");
        TxtFileFilter txtFileFilter = new TxtFileFilter();
        fc.setFileFilter(txtFileFilter);

        String path = Singleton.getUserPreferences().getFilePath();
        if (path != null && path.trim().length() > 0) {
            fc.setCurrentDirectory(new File(path));
        }
        int returnVal = fc.showSaveDialog(SwingUtilities.getWindowAncestor(this));

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String filename = file.getAbsolutePath();
            if (!filename.toLowerCase().endsWith(txtFileFilter.getSuffix())) {
                file = new File(filename + txtFileFilter.getSuffix());
            }
            Singleton.getUserPreferences().setFilePath(file.getParent());
            exportData(file);
            return file;
        } else {
            System.out.println("Export command cancelled by user.\n");
            return null;
        }
    }



    /**
     * Writes out the current table to a file
     *
     * @param file
     */
    protected void exportData(File file) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (int coli = 0; coli < resultsTable.getColumnCount(); coli++) {
                bw.write(((ResultsTableModel) resultsTableModel).getHeader(coli) + "\t");
            }
            bw.write("\n");
            for (int rowi = 0; rowi < resultsTable.getRowCount(); rowi++) {
                for (int coli = 0; coli < resultsTable.getColumnCount(); coli++) {
                    if (resultsTable.getColumnClass(coli) == Double.class) {
                        bw.write(decimalFormat.format(resultsTable.getValueAt(rowi, coli)) + "\t");
                    } else {
                        bw.write(resultsTable.getValueAt(rowi, coli) + "\t");
                    }
                }
                bw.write("\n");
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "IO Exception in writing the file.",
                    "I/O Write Error",
                    JOptionPane.WARNING_MESSAGE);

        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        "IO Exception in writing the file.",
                        "I/O Write Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Writes out subset of selected rows of the current table to a file
     *
     * @param file
     */
    protected void exportSelectedData(File file) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (int coli = 0; coli < resultsTable.getColumnCount(); coli++) {
                bw.write(((ResultsTableModel) resultsTableModel).getHeader(coli) + "\t");
            }
            bw.write("\n");
            int[] selectedRows = resultsTable.getSelectedRows();
            int numSelectedRows = selectedRows.length;
            for (int rowi = 0; rowi < numSelectedRows; rowi++) {
                int selectedRowi = selectedRows[rowi];
                for (int coli = 0; coli < resultsTable.getColumnCount(); coli++) {
                    if (resultsTable.getColumnClass(coli) == Double.class) {
                        bw.write(decimalFormat.format(resultsTable.getValueAt(selectedRowi, coli)) + "\t");
                    } else {
                        bw.write(resultsTable.getValueAt(selectedRowi, coli) + "\t");
                    }
                }
                bw.write("\n");
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "IO Exception in writing the file.",
                    "I/O Write Error",
                    JOptionPane.WARNING_MESSAGE);

        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this),
                        "IO Exception in writing the file.",
                        "I/O Write Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    protected AbstractButton getIncludeSelectedGeneModelsButton() {
        if (includeSelectedGeneModelsButton == null) {
            includeSelectedGeneModelsButton = new JButton("<html><center>Include Selected<br/>Gene Models</center></html>");
            includeSelectedGeneModelsButton.setToolTipText("Adds selected gene/models from Gene/Color/Model window to table");
            includeSelectedGeneModelsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JTable table = Singleton.getModelSelectionPanel().getModelSelectionTable();
                    int[] selectedRows = table.getSelectedRows();
                    int numSelectedRows = selectedRows.length;
                    for (int rowi = 0; rowi < numSelectedRows; rowi++) {
                        addSelectedRowToTableModel(selectedRows[rowi]);
                    }
                }
            });
        }
        return includeSelectedGeneModelsButton;
    }

    protected AbstractButton getIncludeAlGenelModelsButton() {
        if (includeAllGeneModelsButton == null) {
            includeAllGeneModelsButton = new JButton("<html><center>Include All<br/>Gene Models</center></html>");
            includeAllGeneModelsButton.setToolTipText("Adds all gene/models from Gene/Color/Model window to table");
            includeAllGeneModelsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JTable table = Singleton.getModelSelectionPanel().getModelSelectionTable();
                    int numRows = table.getRowCount();
                    for (int rowi = 0; rowi < numRows; rowi++) {
                        addSelectedRowToTableModel(rowi);
                    }
                }
            });
        }
        return includeAllGeneModelsButton;
    }

    protected AbstractButton getIncludeUnselectedGeneModelsButton() {
        if (includeUnselectedGeneModelsButton == null) {
            includeUnselectedGeneModelsButton = new JButton("<html><center>Include Unselected<br/>Gene Models</center></html>");
            includeUnselectedGeneModelsButton.setToolTipText("Add all data to the table from Gene Color Model window except selected rows");
            includeUnselectedGeneModelsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JTable table = Singleton.getModelSelectionPanel().getModelSelectionTable();
                    int numRows = table.getRowCount();
                    int selectedRowsIndex = 0;
                    int[] selectedRows = table.getSelectedRows();
                    int numSelectedRows = selectedRows.length;
                    for (int rowi = 0; rowi < numRows; rowi++) {
                        boolean found = foundInArray(rowi, selectedRows);
                        if (!found) {
                            addSelectedRowToTableModel(rowi);
                        }
                    }
                }
            });
        }
        return includeUnselectedGeneModelsButton;
    }

    /**
     * Returns true if value is in the int[] array else returns false.
     *
     * @param value
     * @param array
     * @return
     */
    private boolean foundInArray(int value, int[] array) {
        boolean found = false;
        int len = array.length;
        for (int i = 0; i < len; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Takes the particular row of the table and adds it to the
     * resultsTableModel if it's not already present.
     *
     * @param row
     */
    protected void addSelectedRowToTableModel(int row) {
        JTable table = Singleton.getModelSelectionPanel().getModelSelectionTable();
        String gene = (String) table.getValueAt(row, ModelSelectionTableModel.GENE_COL);
        String model = (String) table.getValueAt(row, ModelSelectionTableModel.MODEL_COL);
        DataSet dataSet = Singleton.getDataModel().getDataSet(gene);
        for (Model oneModel : dataSet.getModels()) {
            String shorterOneModel = oneModel.toString().replaceAll("\\(-log10 P-Value\\)", "");
            System.out.println("shorterOneModel: [" + shorterOneModel + "] vs [" + model + "]");
            if (shorterOneModel.equals(model)) {
                ((ResultsTableModel) resultsTableModel).addIfNotPresent(gene, oneModel);
            }
        }
    }

    protected AbstractButton getRemoveSelectedRowsButton() {
        if (removeSelectedRowsButton == null) {
            removeSelectedRowsButton = new JButton("Remove Selected Rows");
            removeSelectedRowsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    ((ResultsTableModel) resultsTableModel).removeSelectedRows(resultsTable.getSelectedRows());
                }
            });
        }
        return removeSelectedRowsButton;
    }
    
    protected AbstractButton getRemoveBelowThresholdButton() {
        if(removeBelowThresholdButton == null) {
            removeBelowThresholdButton = new JButton("Remove Rows > -10log(P-value)");
            removeBelowThresholdButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        Double threshold = Double.parseDouble(pvalThresholdTextField.getText());
                        ((ResultsTableModel) resultsTableModel).removeRowsBelowPvalThreshold(threshold);
                    } catch(NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(ResultsTablePanel.this),
                                "-log10(P-value) threshold should be a number",
                                "Value Error",
                                JOptionPane.WARNING_MESSAGE);
                        
                    }
                }
            });
        }
        return removeBelowThresholdButton;
    }

    protected AbstractButton getKeepSelectedRowsButton() {
        if (keepSelectedRowsButton == null) {
            keepSelectedRowsButton = new JButton("Keep Selected Rows");
            keepSelectedRowsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    ((ResultsTableModel) resultsTableModel).keepSelectedRows(resultsTable.getSelectedRows());
                }
            });
        }
        return keepSelectedRowsButton;
    }

    protected JTable getTableResults() {
        if (resultsTable == null) {
            resultsTableModel = new ResultsTableModel();
            resultsTable = new JTable(resultsTableModel);
            resultsTable.setAutoCreateRowSorter(true);
            resultsTable.setAutoResizeMode(WIDTH);
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.INDEX_COL).setPreferredWidth(25);
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.INDEX_COL).setMaxWidth(30);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.INDEX_COL).setMinWidth(25);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.INDEX_COL).setMaxWidth(70);
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.GENE_COL).setPreferredWidth(40);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.GENE_COL).setMinWidth(35);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.GENE_COL).setMaxWidth(70);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.GENE_COL).setCellRenderer(new MyRenderer());
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.MODEL_COL).setPreferredWidth(150);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.MODEL_COL).setMaxWidth(550);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.MODEL_COL).setCellRenderer(new MyRenderer());        

            resultsTable.getColumnModel().getColumn(ResultsTableModel.CHR_COL).setMaxWidth(30);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.CHR_COL).setCellRenderer(new MyRenderer());        

            //resultsTable.getColumnModel().getColumn(ResultsTableModel.RSID_COL).setPreferredWidth(50);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.RSID_COL).setMinWidth(60);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.RSID_COL).setMaxWidth(100);
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.LOC_COL).setPreferredWidth(50);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.LOC_COL).setMinWidth(70);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.LOC_COL).setMaxWidth(100);
            //resultsTable.getColumnModel().getColumn(ResultsTableModel.PVAL_COL).setPreferredWidth(45);
            resultsTable.getColumnModel().getColumn(ResultsTableModel.PVAL_COL).setMinWidth(70);
           resultsTable.getColumnModel().getColumn(ResultsTableModel.PVAL_COL).setMaxWidth(120);

        }
        return resultsTable;
    }
    
    public class MyRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
}

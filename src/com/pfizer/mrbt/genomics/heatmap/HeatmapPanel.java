/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.heatmap;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.resultstable.TxtFileFilter;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.ViewData;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 *
 * @author henstockpv
 */
public class HeatmapPanel extends JComponent {
    private JTable heatmapTable;
    private AbstractTableModel heatmapTableModel;
    private JComponent buttonPanel;
    private JComboBox topComboBox;
    private JComboBox methodComboBox;
    private JTextField radiusField;
    private AbstractButton updateButton;
    private AbstractButton exportButton;
    
    public HeatmapPanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(getHeatmapTable()), gbc);
        
        gbc.gridy = 20;
        gbc.weighty  = 0.0;
        add(getButtonPanel(), gbc);
        
        MouseController mouseController = new MouseController();
        heatmapTable.addMouseListener(mouseController);
    }
    
    public JTable getHeatmapTable() {
        if(heatmapTable == null) {
            heatmapTableModel = new HeatmapTableModel();
            heatmapTable = new JTable(heatmapTableModel);
            heatmapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            TableCellRenderer renderer = heatmapTable.getDefaultRenderer(Object.class);
            heatmapTable.setDefaultRenderer(Object.class, new HeatmapColoringRenderer(renderer));
            
            TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
            Enumeration columns = heatmapTable.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                ((TableColumn) columns.nextElement()).setHeaderRenderer(headerRenderer);
            }
        }
        return heatmapTable;
    }

    /**
     * Returns the heatmap table model associated with the table in this panel
     */
    public HeatmapTableModel getHeatmapTableModel() {
        return (HeatmapTableModel) heatmapTableModel;
    }
    
    protected void updateColumnWidths() {
        heatmapTable.getColumnModel().getColumn(HeatmapTableModel.MODEL_COL).setPreferredWidth(30);
        for (int i = 1; i < heatmapTable.getColumnCount(); i++) {
            heatmapTable.getColumnModel().getColumn(HeatmapTableModel.MODEL_COL).setPreferredWidth(8);
        }
    }
    
    protected JComponent getButtonPanel() {
        if(buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 5;
            gbc.gridy = 10;
            gbc.insets = new Insets(2,5,2,3);
            buttonPanel.add(getExportButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 10;
            buttonPanel.add(getMethodComboBox(), gbc);

            gbc.gridx = 20;
            gbc.gridy = 10;
            buttonPanel.add(getTopComboBox(), gbc);
            
            gbc.gridx = 30;
            gbc.insets = new Insets(2,5,0,3);
            JLabel radiusBpLabel = new JLabel("Radius (bp)");
            radiusBpLabel.setToolTipText("<html>Maximum radius around gene/SNP to consider.<br/>" + 
                    "Note values larger than the retrieved range are rounded down.</html>");
            buttonPanel.add(radiusBpLabel, gbc);
            gbc.gridx = 40;
            gbc.insets = new Insets(2,0,2,3);
            radiusField = new JTextField(8);
            int radiusVal = Singleton.getState().getHeatmapParameters().getRadius();
            radiusField.setText(radiusVal + "");
            radiusField.setToolTipText("<html>Maximum radius around gene/SNP to consider.<br/>" + 
                    "Note values larger than the retrieved range are rounded down.</html>");
            buttonPanel.add(radiusField, gbc);
            gbc.gridx = 50;
            gbc.insets = new Insets(2,5,2,5);
            buttonPanel.add(getUpdateButton(), gbc);
            
            buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return buttonPanel;
    }
    
    protected JComboBox getMethodComboBox() {
        if(methodComboBox == null) {
            methodComboBox = new JComboBox(HeatmapParameters.METHOD_OPTIONS);
            methodComboBox.setToolTipText("Computes value from TopN SNPs within radius.  Min/Max refer to -log10 p-val.");
            int selectedIndex = Singleton.getState().getHeatmapParameters().getFunction();
            methodComboBox.setSelectedIndex(selectedIndex);
        }
        return methodComboBox;
    }

    
    protected JComboBox getTopComboBox() {
        if(topComboBox == null) {
            topComboBox = new JComboBox(HeatmapParameters.TOP_OPTIONS);
            int selectedIndex = Singleton.getState().getHeatmapParameters().getTopNindex();
            topComboBox.setToolTipText("TopN refers to highest -10log p-values wihtin a given radius.");
            topComboBox.setSelectedIndex(selectedIndex);
        }
        return topComboBox;
    }

    /**
     * returns the updateButton that adjusts the parameters and creates a new
     * heatmap button
     * @return 
     */
    protected AbstractButton getUpdateButton() {
        if (updateButton == null) {
            updateButton = new JButton("Update");
            updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {
                        int radiusValue = Integer.MAX_VALUE/10;
                        if(! radiusField.getText().trim().isEmpty()) {
                            radiusValue = Integer.parseInt(radiusField.getText());
                            radiusValue = Math.abs(radiusValue);
                        }
                        System.out.println("Updating button with radius " + radiusValue);
                        HeatmapParameters heatmapParameters =
                                          new HeatmapParameters(
                                methodComboBox.getSelectedIndex(),
                                topComboBox.getSelectedIndex(),
                                radiusValue);
                        Singleton.getState().setHeatmapParameters(heatmapParameters);
                        Singleton.getState().fireHeatmapChanged();
                    } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(updateButton),
                                "Radius should be an integer value.",
                                "Invalidd value",
                                    JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
        return updateButton;
    }
    
    
    protected AbstractButton getExportButton() {
        if(exportButton == null) {
            exportButton = new JButton("Export");
            exportButton.setToolTipText("Saves the table to a tab-delimited file");
            exportButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
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
            for (int coli = 0; coli < heatmapTable.getColumnCount(); coli++) {
                bw.write(((HeatmapTableModel) heatmapTableModel).getColumnName(coli) + "\t");
            }
            bw.write("\n");
            for (int rowi = 0; rowi < heatmapTable.getRowCount(); rowi++) {
                for (int coli = 0; coli < heatmapTable.getColumnCount(); coli++) {
                    if (heatmapTable.getColumnClass(coli) == Double.class) {
                        if((Double) heatmapTable.getValueAt(rowi,coli) < 0.0) {
                            bw.write("\t");
                        } else {
                            bw.write(decimalFormat.format(heatmapTable.getValueAt(rowi, coli)) + "\t");
                        }
                    } else {
                        bw.write(heatmapTable.getValueAt(rowi, coli) + "\t");
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
     * Selects the row/column of the heatmap table and updates the main plot
     * for this particular cell.  It does this by creating a newViewData with
     * from the geneName and adding the selected model to it.  If there is
     * no match (shouldn't happen) it does no selection.
     */
    protected void selectPlotForDisplay(int row, int col) {
        String selectedGeneName = heatmapTableModel.getColumnName(col);
        String selectedModelName = (String) heatmapTableModel.getValueAt(row, 0);
        DataSet dataSet = Singleton.getDataModel().getDataSet(selectedGeneName);
        ViewData newViewData = new ViewData(dataSet);
        
        ArrayList<ViewData> viewDatas = Singleton.getState().getHeatmapViewData();
        for(ViewData viewData : viewDatas) {
            String geneName = viewData.getDataSet().getGeneRange().getName();
            if(geneName.equals(selectedGeneName)) {
                for(Model model : viewData.getDataSet().getModels()) {
                    System.out.print("Gene " + geneName + "\tComparing models " + model.toString());
                    if(model.toString().equals(selectedModelName)) {
                        newViewData.addModel(model);
                        Singleton.getState().setMainView(newViewData);
                        System.out.println("Matched " + selectedModelName);
                        return;
                    } else {
                        System.out.println("nope");
                    }
                }
            }
        }
    }
    
    public class MouseController implements MouseListener {
        public void mouseClicked(MouseEvent me) {
            int row = heatmapTable.rowAtPoint(me.getPoint());
            int col = heatmapTable.columnAtPoint(me.getPoint());
            System.out.println("Mouse selected cell " + row + "\t" + col);
            if(me.getClickCount()> 1 && col > 0 && 
               (Double) heatmapTable.getValueAt(row, col) >= 0.0) {
                selectPlotForDisplay(row, col);
            }
        }
        public void mouseEntered(MouseEvent me) {
            
        }
        public void mouseExited(MouseEvent me) {
            
        }
        public void mousePressed(MouseEvent me) {
            System.out.println("Mouse released");
        }
        public void mouseReleased(MouseEvent me) {
            
        }
    }
}

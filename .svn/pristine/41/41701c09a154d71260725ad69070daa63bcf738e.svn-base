/*
 * Enables the modification or deletion of different markers.
 */

package com.pfizer.mrbt.genomics.hline;

import com.pfizer.mrbt.genomics.Singleton;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Peter V. Henstock
 */
public class LineModificationPane extends JComponent {
    private JTable lineTable;
    private JComponent buttonPanel;
    private LineModificationTableModel lineModificationTableModel;
    private AbstractButton editButton;
    private AbstractButton deleteButton;
    private JComponent parentComponent; // kluge for centering JOptionPane

    public LineModificationPane(JComponent parentComponent) {
        super();
        this.parentComponent = parentComponent;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        lineModificationTableModel = new LineModificationTableModel();
        lineTable = new JTable(lineModificationTableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int col) {
                TableCellRenderer renderer = super.getCellRenderer(row, col);
                if(col > 0 && col < 4) {
                    ((JLabel) renderer).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) renderer).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return renderer;
            }
        };
        lineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lineTable.setPreferredScrollableViewportSize(new Dimension(460, (int) Math.round(lineTable.getPreferredSize().getHeight())));

        add(new JScrollPane(lineTable), gbc);

         TableColumn colorCol = lineTable.getColumnModel().getColumn(LineModificationTableModel.COLOR_COL);
         colorCol.setCellRenderer(new ColorCellRenderer());
         colorCol.setPreferredWidth(35);
         colorCol.setMaxWidth(35);

         for(int col = 1; col < 4; col++) {
            TableColumn textCol = lineTable.getColumnModel().getColumn(col);
            textCol.setPreferredWidth(28);
         }

        gbc.gridy = 20;
        add(getButtonPanel(), gbc);

        //StateController sc = new StateController();
        //Singleton.getState().addListnener(sc);
    }

    protected JComponent getButtonPanel() {
        if(buttonPanel==null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.insets = new Insets(20,20,20,20);
            buttonPanel.add(getEditButton(), gbc);

            gbc.gridx = 20;
            buttonPanel.add(getDeleteButton(), gbc);
        }
        return buttonPanel;
    }

    protected AbstractButton getEditButton() {
        if(editButton == null) {
            editButton = new JButton("Edit Selected");
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    int index = lineTable.getSelectedRow();
                    if(index >= 0) {
                        HLine currLine = Singleton.getState().getLines().get(index);
                        LineCreationPane lineCreationPane = new LineCreationPane();
                        lineCreationPane.updateFields(currLine);
                        int linePaneReturn = 0;
                        boolean validMarker = true;
                        do {
                            linePaneReturn = JOptionPane.showConfirmDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(parentComponent),
                                lineCreationPane, "Edit Line", JOptionPane.OK_OPTION);
                            if(linePaneReturn == JOptionPane.OK_OPTION) {
                                validMarker = lineCreationPane.validateLine();
                            }
                        } while (linePaneReturn == JOptionPane.OK_OPTION && !validMarker);
                        if (linePaneReturn == JOptionPane.OK_OPTION) {
                            String name = lineCreationPane.getLineName();
                            int lineStyle = lineCreationPane.getLineStyle();
                            int lineScope = lineCreationPane.getLineScopeIndex();
                            String logPValStr = lineCreationPane.getLogPValue();
                            float logPVal = 0.0f;
                            try {
                                logPVal = Float.parseFloat(logPValStr);
                            } catch(NumberFormatException nfe) {
                                // already validated earlier
                            }
                            HLine hLine = new HLine(name, logPVal, lineCreationPane.getLineColor(), lineStyle, lineScope);
                            Singleton.getState().replaceHorizontalLine(index, hLine);
                            lineModificationTableModel.fireTableDataChanged();
                        }
                    }
                }
            });
        }
        return editButton;
    }

    protected AbstractButton getDeleteButton() {
        if (deleteButton == null) {
            deleteButton = new JButton("Delete Selected");
            deleteButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    int index = lineTable.getSelectedRow();
                    if (index >= 0) {
                        Singleton.getState().removeHorizontalLine(index);
                        lineModificationTableModel.fireTableDataChanged();
                    } else {
                        JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(parentComponent),
                                "Select a line then click delete.",
                                "Delete Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }
        return deleteButton;
    }

    /*public class StateController implements StateChangeListener {
        public void axisChanged(ChangeEvent ce) {}
        public void currentLocationChanged(ChangeEvent ce) {}
        public void dragChanged(ChangeEvent ce) {}
        public void linesChanged(ChangeEvent ce) {
            lineModificationTableModel.fireTableDataChanged();
        }
        public void ceffRangeRecorded(ChangeEvent ce) {}
        public void namesChanged(ChangeEvent ce) {}
    }*/

    public class ColorCellRenderer extends JLabel implements TableCellRenderer {
      public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
         setOpaque(true);
         HLine oneLine = Singleton.getState().getLines().get(rowIndex);
         setBackground(oneLine.getLineColor());
         return this;
      }
   }

    public class CenteredCellRenderer extends JLabel implements TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderedLabel = (JLabel) getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);
            renderedLabel.setHorizontalAlignment(JLabel.CENTER);
            return renderedLabel;
        }
    }

}

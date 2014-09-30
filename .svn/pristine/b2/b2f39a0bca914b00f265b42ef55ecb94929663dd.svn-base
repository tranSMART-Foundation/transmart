/*
 * Full panel that contains the display, the table, and the edit capability
 */
package com.pfizer.mrbt.genomics.legend;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.modelselection.ColorSquareRenderer;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.utils.ImageSelection;
import com.pfizer.mrbt.genomics.utils.JpgFilter;
import com.pfizer.mrbt.genomics.utils.SuffixFileFilter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstock
 */
public class LegendPanel extends JComponent {
    public static final int PREFERRED_WIDTH = 500;
    public static final int PREFERRED_HEIGHT = 500;
    private LegendDisplay legendDisplayPanel;
    private JComponent bottomPanel;
    private JTable legendTable;
    private AbstractTableModel legendTableModel;
    private JComponent modificationPanel;
    private JTextField originalField;
    //private JTextArea displayArea;
    private JTextField displayField;
    private AbstractButton updateButton;
    private AbstractButton copyToClipboardButton;
    private AbstractButton saveLegendButton;
    public LegendPanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        gbc.weightx = 1.0;
        gbc.weighty = 0.75;
        add(new JScrollPane(getLegendDisplayPanel()), gbc);
        
        gbc.gridy = 20;
        gbc.weighty = 0.3;
        add(new JScrollPane(getLegendTable()), gbc);
        
        gbc.gridy = 30;
        gbc.weighty = 0.1;
        add(getModificationPanel(), gbc);
        
        gbc.gridy = 40;
        gbc.weighty = 0.0;
        add(getBottomPanel(), gbc);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
    }
    
    /*@Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }*/
    
    protected JComponent getLegendDisplayPanel() {
        if(legendDisplayPanel == null) {
            legendDisplayPanel = new LegendDisplay();
            legendDisplayPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return legendDisplayPanel;
    }
    
    protected JTable getLegendTable() {
        if(legendTable == null) {
            legendTableModel = new LegendTableModel();
            legendTable = new JTable(legendTableModel);
            legendTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            legendTable.setColumnSelectionAllowed(false);
            legendTable.setRowSelectionAllowed(true);
            legendTable.setCellSelectionEnabled(false);
            //legendTable.setPreferredSize(new Dimension(350,100));
            //legendTable.setMaximumSize(new Dimension(1200,1200));
            legendTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent lse) {
                    Singleton.getState().setLegendSelectedRow(lse.getFirstIndex());
                }
            });
            legendTable.setAutoResizeMode(WIDTH);
            legendTable.getColumnModel().getColumn(LegendTableModel.COLOR_COL).setCellRenderer(new ColorSquareRenderer());
            legendTable.getColumnModel().getColumn(LegendTableModel.COLOR_COL).setMaxWidth(20);
            
        }
        return legendTable;
    }
    
    protected JComponent getModificationPanel() {
        if(modificationPanel == null) {
            modificationPanel = new JPanel();
            modificationPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.gridx = 10;
            gbc.gridy = 5;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridwidth = 30;
            JLabel instructionsLabel = new JLabel("Click on one of the rows of the table above to modify the legend text here.");
            modificationPanel.add(instructionsLabel, gbc);
            
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2,2,2,2);
            JLabel originalLabel = new JLabel("Original:");
            modificationPanel.add(originalLabel, gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 10;
            originalField = new JTextField(70);
            originalField.setMinimumSize(new Dimension(270,20));
            originalField.setText("");
            originalField.setEditable(false);
            modificationPanel.add(originalField, gbc);
            
            gbc.gridx = 10;
            gbc.gridy = 20;
            JLabel displayLabel = new JLabel("Display:");
            modificationPanel.add(displayLabel, gbc);

            gbc.gridx = 20;
            gbc.gridy = 20;
            /*displayArea = new JTextArea(1,70);
            displayArea.setText("");
            displayArea.setMinimumSize(new Dimension(270,20));
            modificationPanel.add(displayArea, gbc);*/
            displayField = new JTextField(70);
            displayField.setText("");
            displayField.setMinimumSize(new Dimension(270,20));
            displayField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    int keyCode = ke.getKeyCode();
                    if (keyCode == KeyEvent.VK_ENTER) {
                        getUpdateButton().doClick();                        
                    }
                }
            });            
            modificationPanel.add(displayField, gbc);
            
            
            gbc.gridx = 30;
            gbc.gridy = 20;
            modificationPanel.add(getUpdateButton(), gbc);
            modificationPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            
        }
        return modificationPanel;
    }
    
    protected JComponent getBottomPanel() {
        if(bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(5,5,5,5);
            bottomPanel.add(getCopyToClipboardButton(), gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 10;
            bottomPanel.add(getSaveLegendButton(), gbc);
            
        }
        return bottomPanel;
    }
    
    protected AbstractButton getUpdateButton() {
        if(updateButton == null) {
            updateButton = new JButton("Update");
            updateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int selectedRow = Singleton.getState().getLegendSelectedRow();
                    if(selectedRow == State.UNSELECTED_ROW || selectedRow < 0) {
                        JOptionPane.showMessageDialog((JFrame) SwingUtilities.getWindowAncestor(LegendPanel.this),
                        "Select one of the rows from the table in the middle of this view.",
                        "No Selected Data",
                        JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Model model = Singleton.getState().getMainView().getModel(selectedRow);
                    Singleton.getState().assignModel2Legend(model, displayField.getText().trim());
                    legendTableModel.fireTableDataChanged();
                    getLegendDisplayPanel().repaint();
                }
            });
        }
        return updateButton;
    }

    protected AbstractButton getCopyToClipboardButton() {
        if(copyToClipboardButton == null) {
            copyToClipboardButton = new JButton("Copy to Clipboard");
            copyToClipboardButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    captureLegendPanelToClipboard();
                    System.out.println("Update button clicked");
                }
            });
        }
        return copyToClipboardButton;
    }

    protected AbstractButton getSaveLegendButton() {
        if(saveLegendButton == null) {
            saveLegendButton = new JButton("Save Legend to File");
            saveLegendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JpgFilter jpgFilter = new JpgFilter();
                    File file = getFilename(jpgFilter);
                    if(file != null) {
                      ((LegendPanel) LegendPanel.this).writeLegendPanelToFile(file);
                     }
                    System.out.println("Update button clicked");
                }
            });
        }
        return saveLegendButton;
    }
    
    
        
   /**
    * Asks the user for a filename for the particular file
    * @return
    */
   public File getFilename(SuffixFileFilter fileFilter) {
      JFileChooser fc = new JFileChooser();
      fc.addChoosableFileFilter((FileFilter) fileFilter);
      
      String path = Singleton.getUserPreferences().getFilePath();
      if(path!=null && path.trim().length() > 0) {
        fc.setCurrentDirectory(new File(path));
      }
      int returnVal = fc.showSaveDialog(SwingUtilities.getWindowAncestor(LegendPanel.this));

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         String filename = file.getAbsolutePath();
         if(! filename.toLowerCase().endsWith(fileFilter.getSuffix())) {
            file = new File(filename + fileFilter.getSuffix());
         }
         Singleton.getUserPreferences().setFilePath(file.getParent());
         return file;
      } else {
         System.out.println("Export command cancelled by user.\n");
         return null;
      }
   }
    
        /**
     * Captures the axes and plot and annotation panel to clipboard and
     * throws a dialog error warning on failure
     */
    public void captureLegendPanelToClipboard() {
        BufferedImage panelImage = new BufferedImage(legendDisplayPanel.getWidth(), legendDisplayPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        legendDisplayPanel.paint(panelImage.getGraphics());
        int width  = legendDisplayPanel.getRightmostDisplay();
        int height = legendDisplayPanel.getLowestDisplay();
        BufferedImage cropImage = panelImage.getSubimage(2, 2, width, height);
        ImageSelection imgSel = new ImageSelection(cropImage);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }

    /**
     * Writes out the legend panel
     * throws a dialog error warning on failure
     *
     * @param file
     */
    public void writeLegendPanelToFile(File file) {
        BufferedImage img = new BufferedImage((int) legendDisplayPanel.getWidth(), (int) legendDisplayPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        legendDisplayPanel.paint(img.getGraphics());
        BufferedImage cropImage = img.getSubimage(2, 2, 
                legendDisplayPanel.getRightmostDisplay(), 
                legendDisplayPanel.getLowestDisplay());
        try {
            ImageIO.write(cropImage, "jpg", file);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "Failed to save file " + file.getAbsolutePath(),
                    "File Write Error",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("Invalid search term list");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    "Failed to save file " + file.getAbsolutePath(),
                    "File Write Error",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("Invalid search term list");
        }
    }

 


    public class StateController implements StateListener {
        @Override
        public void currentChanged(ChangeEvent ce) {
            repaint();
        }

        @Override
        public void mainPlotChanged(ChangeEvent ce) {
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
            int selectedIndex = Singleton.getState().getLegendSelectedRow();
            if(selectedIndex == State.UNSELECTED_ROW) {
                originalField.setText("");
                displayField.setText("");
            } else {
                Model model = Singleton.getState().getMainView().getModel(selectedIndex);
                originalField.setText(model.toString());
                displayField.setText(Singleton.getState().getLegendFromModel(model));
            }
        }        
        
        @Override
        public void heatmapChanged(ChangeEvent ce) { }
        
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.query;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.History;
//import com.pfizer.mrbt.genomics.TransmartClient.TransmartDataLoaderWithThreads;
//import com.pfizer.mrbt.genomics.bioservices.DataLoaderWithThreads;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import java.awt.Color;
//import com.pfizer.mrbt.genomics.bioservices.SNPDataFetchByGene;
//import com.pfizer.mrbt.genomics.state.State;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author henstock
 */
public class QueryPanel extends JComponent {

    private JTextArea geneSnpTextArea;

    private AbstractButton retrieveButton;
    private Collection availableModelsList;
    private Collection selectedModelsList;
    private DefaultListModel geneSnpQueryListModel = new DefaultListModel();
    private PingPongBufferPane modelPanel;
    private JComponent geneSearchPanel;
    private JComponent newGeneSearchPanel;
    private JComponent searchHistoryPanel;
    private JComponent historyButtonPanel;
    private JComponent buttonPanel;
    private JPanel queryPanel;
    
    private JComponent filePanel;
    private JTextField fileNameField;
    private JCheckBox saveByGeneCheckBox;
    private JCheckBox saveByStudyModelCheckBox;
    private AbstractButton fileSelectButton;
    private AbstractButton clearGenesButton;
    
    private AbstractButton clearHistoryButton;
    private AbstractButton updateHistoryButton;
    
    public final static int GENE_SEARCH_TAB = 0;
    public final static int SEARCH_HISTORY_TAB = 1;
    
    private JComboBox snpAnnotationComboBox;
    private JComboBox geneAnnotationComboBox;
    private JList geneSnpQueryIdList;
    private JTextField basePairRadiusField;
    private List<ModelOption> modelOptions = new ArrayList<ModelOption>();
    private JTable historyTable;
    
    public final static boolean VERBOSE = false;
    
    public QueryPanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 10;
        gbc.gridy = 10;
        add(getQueryPanel(), gbc);
        
        gbc.gridy = 40;
        gbc.weighty = 4.0;
        add(getSearchHistoryPanel(), gbc);

    }
    
    protected JPanel getQueryPanel() {
        if (queryPanel == null) {
            queryPanel = new JPanel();
            queryPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.weighty = 1.0;
            gbc.weightx = 0.50;
            queryPanel.add(getModelPanel(), gbc);

            gbc.gridy = 20;
            gbc.weightx = 1.0;
            gbc.weighty = 0.25;
            //gbc.insets = new Insets(5, 10, 2, 5);
            queryPanel.add(getGeneSearchPanel(), gbc);

            //gbc.insets = new Insets(2, 10, 2, 10);
            gbc.weighty = 0.0;
            gbc.gridy = 30;
            queryPanel.add(getButtonPanel(), gbc);
            queryPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return queryPanel;
    }

    protected PingPongBufferPane getModelPanel() {

        if (modelPanel == null) {
            Object[] models;
            try {
                availableModelsList = getAvailableModelsList();
                selectedModelsList = new ArrayList();

                models = availableModelsList.toArray();
            } catch (RetrievalException rex) {
                showLoadInitializationFailure(rex);
                System.exit(1);
                models = new Object[0];
            }
            String dialogName = "dialogName";
            String leftToRightStr = "Include";
            String rightToLeftStr = "Exclude";
            String leftToRightAllStr = "Include All";
            String rightToLeftAllStr = "Exclude All";
            modelPanel = new PingPongBufferPane(selectedModelsList, availableModelsList, dialogName, leftToRightStr, rightToLeftStr, leftToRightAllStr, rightToLeftAllStr);
            //modelPanel.setBorder(BorderFactory.createLineBorder(Color.yellow));
        }

        return modelPanel;
    }

    /**
     * Shows an error message and prints out the reason to stderr
     * @param rex 
     */
    protected void showLoadInitializationFailure(RetrievalException rex) {
        JOptionPane.showMessageDialog(
            (JFrame) SwingUtilities.getWindowAncestor(QueryPanel.this),
            rex.getMessage(),
            "Initialization failed: " + rex.getRetrievalMethod().toString(),
            JOptionPane.ERROR_MESSAGE);
        System.err.println("Initialization failed");
        System.err.println(rex.toString());
    }


    /**
     * If dataMode == DEMO_MODE then it fills and returns availableModelsList.
     * If dataMode == BIOSERVICES_MODE, it pulls the list from bioservices
     * and keeps the list in modelOptions list class vairable
     * @throws RetrievalException if service fails to return result
     * @return 
     */
    protected Collection getAvailableModelsList() throws RetrievalException {
        if (availableModelsList == null) {
            availableModelsList = new ArrayList();
            modelOptions = Singleton.getDataModel().getWebServices().fetchModelOptions();
            for(ModelOption modelOption : modelOptions) {
                availableModelsList.add(modelOption.toString());
            } 
            /*int dataMode = Singleton.getState().getDataMode();
            if(dataMode == State.DEMO_MODE) {
                availableModelsList.add("DGI/BROAD_LODL/Additive");
                availableModelsList.add("DGI/Broad_Trig/Additive");
                availableModelsList.add("WTCCC/WTCCC_CAD/Additive");
                availableModelsList.add("WTCCC/WTCCC_Hypertension/Additive");
                availableModelsList.add("WTCCC/WTCCC_RA/Additive");
            } else if(dataMode == State.TRANSMART_SERVICES_MODE ||
                      dataMode == State.TRANSMART_DEV_SERVICES_MODE) { // kluge todo removed?
                TransmartQueryParameterFetch tqpf = new TransmartQueryParameterFetch();
                modelOptions = tqpf.fetchModelOptions();
                for(ModelOption modelOption : modelOptions) {
                    if(VERBOSE) {
                        System.out.println("Loaded model option " + modelOption.toVerboseString());
                    }
                    availableModelsList.add(modelOption.toString());
                }
            } else if(dataMode == State.BIOSERVICES_MODE) {
                QueryParameterFetch qpf = new QueryParameterFetch();
                modelOptions = qpf.fetchModelOptions();
                for(ModelOption modelOption : modelOptions) {
                    availableModelsList.add(modelOption.toString());
                }
            }*/
        }
        return availableModelsList;
    }

    /*protected JComponent getNewGeneSearchPanel() {
        if(newGeneSearchPanel == null) {
            newGeneSearchPanel = new JPanel();
            Font currFont = newGeneSearchPanel.getFont();
            newGeneSearchPanel.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+4));
            newGeneSearchPanel.add("Gene Search", getGeneSearchPanel());
            newGeneSearchPanel.add("Search History", getSearchHistoryPanel());
            newGeneSearchPanel.setForegroundAt(newGeneSearchPanel.getSelectedIndex(), Color.RED);
            newGeneSearchPanel.setBackgroundAt(newGeneSearchPanel.getSelectedIndex(), Color.WHITE);
            newGeneSearchPanel.addChangeListener(new ChangeListener() {
                @Override
               public void stateChanged(ChangeEvent ce) {
                   newGeneSearchPanel.setForegroundAt(0, Color.BLACK);
                   newGeneSearchPanel.setForegroundAt(1, Color.BLACK);
                   newGeneSearchPanel.setBackgroundAt(0, Color.LIGHT_GRAY);
                   newGeneSearchPanel.setBackgroundAt(1, Color.LIGHT_GRAY);
                   newGeneSearchPanel.setForegroundAt(newGeneSearchPanel.getSelectedIndex(), Color.RED);
                   newGeneSearchPanel.setBackgroundAt(newGeneSearchPanel.getSelectedIndex(), Color.WHITE);
               }
            });
        }
        return newGeneSearchPanel;
    }

    protected JComponent getOldGeneSearchPanel() {
        if(newGeneSearchPanel == null) {
            newGeneSearchPanel = new JPanel();
            Font currFont = newGeneSearchPanel.getFont();
            newGeneSearchPanel.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+4));
            newGeneSearchPanel.add("Gene Search", getGeneSearchPanel());
            newGeneSearchPanel.add("Search History", getSearchHistoryPanel());
            newGeneSearchPanel.setForegroundAt(newGeneSearchPanel.getSelectedIndex(), Color.RED);
            newGeneSearchPanel.setBackgroundAt(newGeneSearchPanel.getSelectedIndex(), Color.WHITE);
            newGeneSearchPanel.addChangeListener(new ChangeListener() {
                @Override
               public void stateChanged(ChangeEvent ce) {
                   newGeneSearchPanel.setForegroundAt(0, Color.BLACK);
                   newGeneSearchPanel.setForegroundAt(1, Color.BLACK);
                   newGeneSearchPanel.setBackgroundAt(0, Color.LIGHT_GRAY);
                   newGeneSearchPanel.setBackgroundAt(1, Color.LIGHT_GRAY);
                   newGeneSearchPanel.setForegroundAt(newGeneSearchPanel.getSelectedIndex(), Color.RED);
                   newGeneSearchPanel.setBackgroundAt(newGeneSearchPanel.getSelectedIndex(), Color.WHITE);
               }
            });
        }
        return newGeneSearchPanel;
    }*/

    /**
     * Panel that contains the snp/gene annotation sources, gene requests, and radius
     * @return 
     */
    protected JComponent getOldRealGeneSearchPanel() {
        if (geneSearchPanel == null) {
            geneSearchPanel = new JPanel();
            geneSearchPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 20;
            gbc.gridy = 10;
            JLabel titleLabel = new JLabel("Gene Search by Name");
            titleLabel.setToolTipText("<html>Type or copy a delimited list of gene names such as: TNF IL6<br/>Genes with lower case (C10orf11) should be enclosed in double quotes.</html>");
            Font currFont2 = titleLabel.getFont();
            Font labelFont2 = new Font(currFont2.getName(), Font.BOLD, currFont2.getSize()+4);
            titleLabel.setFont(labelFont2);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            gbc.anchor = GridBagConstraints.CENTER;
            geneSearchPanel.add(titleLabel, gbc);
            
            Font currFont = titleLabel.getFont();
            Font labelFont = new Font(currFont.getName(), Font.PLAIN, 13);
            
            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.gridheight = 40;
            //gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2,0,0,30);
            geneSnpTextArea = new JTextArea(11, 30);
            geneSnpTextArea.setText("");
            geneSnpTextArea.setToolTipText("<html>Type or copy a delimited list of gene names such as: TNF IL6<br/>Genes with lower case (C10orf11) should be enclosed in double quotes</html>.");
            
            gbc.weightx = 10.0;
            gbc.weighty = 1.0;
            geneSnpTextArea.setText("A2M TNF il6");
            geneSnpTextArea.setText("");
            geneSnpTextArea.setWrapStyleWord(true);
            geneSearchPanel.add(new JScrollPane(geneSnpTextArea), gbc);


            
            
            gbc.insets = new Insets(5, 2, 5, 2);
            gbc.gridx = 40;
            gbc.gridy = 20;
            gbc.gridheight = 1;
            gbc.gridwidth = 1;            
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            JLabel snpLabel = new JLabel("<html><p align=right>SNP Annotation<br/>Source:</p></html>");
            snpLabel.setHorizontalAlignment(JLabel.RIGHT);
            snpLabel.setFont(labelFont);
            geneSearchPanel.add(snpLabel, gbc);

            gbc.gridx = 50;
            gbc.gridy = 20;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            //geneSearchPanel.add(getSnpAnnotationComboBox(), gbc);

            gbc.gridx = 40;
            gbc.gridy = 30;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel geneLabel = new JLabel("<html><p align=right>Gene Annotation<br/>Source:</p></html>");
            geneLabel.setHorizontalAlignment(JLabel.RIGHT);
            geneLabel.setFont(labelFont);
            geneSearchPanel.add(geneLabel, gbc);

            gbc.gridx = 50;
            gbc.gridy = 30;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            //geneSearchPanel.add(getGeneAnnotationComboBox(), gbc);

            gbc.gridx = 40;
            gbc.gridy = 40;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel basePairRadiusLabel = new JLabel("<html><p align=right>Up-/Downstream<br/>(+/- base-pairs):</p></html>");
            basePairRadiusLabel.setFont(labelFont);
            geneSearchPanel.add(basePairRadiusLabel, gbc);

            gbc.gridx = 50;
            gbc.gridy = 40;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            geneSearchPanel.add(getBasePairRadiusField(), gbc);
            
            gbc.gridx = 40;
            gbc.gridy = 50;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            geneSearchPanel.add(getClearGenesButton(), gbc);


            //geneSearchPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return geneSearchPanel;
    }
    
    /**
     * Panel that contains the snp/gene annotation sources, gene requests, and radius
     * @return 
     */
    protected JComponent getGeneSearchPanel() {
        if (geneSearchPanel == null) {
            geneSearchPanel = new JPanel();
            geneSearchPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 10;
            gbc.gridy = 10;
            JLabel titleLabel = new JLabel("Gene/SNP:");
            titleLabel.setToolTipText("<html>Type or copy a delimited list of gene names such as: TNF IL6<br/>Genes with lower case (C10orf11) should be enclosed in double quotes.</html>");
            Font currFont2 = titleLabel.getFont();
            Font labelFont2 = new Font(currFont2.getName(), Font.BOLD, currFont2.getSize()+4);
            titleLabel.setFont(labelFont2);
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            gbc.anchor = GridBagConstraints.CENTER;
            geneSearchPanel.add(titleLabel, gbc);
            
            Font currFont = titleLabel.getFont();
            Font labelFont = new Font(currFont.getName(), Font.PLAIN, 13);

            gbc.gridx = 10;
            gbc.gridy = 20;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            geneSearchPanel.add(getClearGenesButton(), gbc);

            // gene or snp search field
            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.gridheight = 20;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(4,0,0,0);
            geneSnpTextArea = new JTextArea(1, 25);
            Font textAreaFont = geneSnpTextArea.getFont();
            geneSnpTextArea.setFont(textAreaFont.deriveFont(12f));
            geneSnpTextArea.setText("");
            geneSnpTextArea.setToolTipText("<html>Type or copy a delimited list of gene names such as: TNF IL6<br/>Genes with lower case (C10orf11) should be enclosed in double quotes</html>.");
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            geneSnpTextArea.setWrapStyleWord(true);
            geneSearchPanel.add(new JScrollPane(geneSnpTextArea), gbc);


            gbc.insets = new Insets(5, 2, 5, 2);
            gbc.gridx = 40;
            gbc.gridy = 20;
            gbc.gridheight = 1;
            gbc.gridwidth = 1;            
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;

            gbc.gridx = 30;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel basePairRadiusLabel = new JLabel("<html><p align=right>Up-/Downstream<br/>(+/- base-pairs):</p></html>");
            //basePairRadiusLabel.setHorizontalAlignment(JLabel.RIGHT);
            basePairRadiusLabel.setFont(labelFont);
            geneSearchPanel.add(basePairRadiusLabel, gbc);

            gbc.gridx = 40;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            geneSearchPanel.add(getBasePairRadiusField(), gbc);
            

            //geneSearchPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return geneSearchPanel;
    }
    
    protected JComponent getSearchHistoryPanel() {
        if(searchHistoryPanel == null) {
            searchHistoryPanel = new JPanel();
            searchHistoryPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 50;
            gbc.gridy = 20;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.CENTER;
            JLabel historyLabel = new JLabel("Data Retrieval History");
            Font font = historyLabel.getFont();
            historyLabel.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize() + 2));
            historyLabel.setHorizontalAlignment(JLabel.CENTER);
            searchHistoryPanel.add(historyLabel, gbc);
            
            gbc.gridx = 50;
            gbc.gridy = 30;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            searchHistoryPanel.add(new JScrollPane(getRetrievalHistoryTable()), gbc);
            
            gbc.gridx = 50;
            gbc.gridy = 40;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            //searchHistoryPanel.add(getHistoryButtonPanel(), gbc);
        }
        return searchHistoryPanel;
    }
     
    protected JComponent getHistoryButtonPanel() {
        if(historyButtonPanel == null) {
            historyButtonPanel = new JPanel();
            historyButtonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5,10,5,10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            historyButtonPanel.add(getUpdateHistoryButton(), gbc);

            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5,10,5,10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            historyButtonPanel.add(getClearHistoryButton(), gbc);
        }
        return historyButtonPanel;
    }

    protected JComponent getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.ipadx = 7;
            buttonPanel.add(getRetrieveButton(), gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.insets = new Insets(0,50,0,0);
            buttonPanel.add(getUpdateHistoryButton(), gbc);
            
            gbc.gridx = 30;
            gbc.gridy = 10;
            gbc.insets = new Insets(0,10,0,0);
            buttonPanel.add(getClearHistoryButton(), gbc);
            //buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        }
        return buttonPanel;
    }

    protected JComponent getFilePanel() {
        if (filePanel == null) {
            filePanel = new JPanel();
            filePanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0,5,0,0);
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel label = new JLabel("<html><p align=RIGHT>Optional File To Automatically<br/>Save Data When Fully Loaded</p></html>");
            filePanel.add(label, gbc);
            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.WEST;
            fileNameField = new JTextField(50);
            filePanel.add(fileNameField, gbc);
            
            gbc.gridx = 30;
            gbc.gridy = 10;
            filePanel.add(getFileSelectButton(), gbc);
            
            gbc.gridx = 40;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel saveByGeneLabel = new JLabel("Save data by gene");
            saveByGeneLabel.setToolTipText("Saves to multiple file separately by gene");
            filePanel.add(saveByGeneLabel, gbc);
            gbc.gridx = 50;
            gbc.gridy = 10;
            gbc.anchor = GridBagConstraints.WEST;
            filePanel.add(getSaveByGeneCheckBox(), gbc);

            gbc.gridx = 40;
            gbc.gridy = 20;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel saveByStudyModelLabel = new JLabel("Save data by study/model");
            saveByStudyModelLabel.setToolTipText("Saves to multiple file separately by models");
            filePanel.add(saveByStudyModelLabel, gbc);
            gbc.gridx = 50;
            gbc.gridy = 20;
            gbc.anchor = GridBagConstraints.WEST;
            filePanel.add(getSaveByStudyModelCheckBox(), gbc);
            filePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return filePanel;
    }
    
    protected AbstractButton getFileSelectButton() {
        if (fileSelectButton == null) {
            fileSelectButton = new JButton("Select");
            fileSelectButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    JFileChooser fc = new JFileChooser();
                    //fc.addChoosableFileFilter((FileFilter) fileFilter);
                    String path = Singleton.getUserPreferences().getFilePath();
                    if (path != null && path.trim().length() > 0) {
                        fc.setCurrentDirectory(new File(path));
                    }
                    int returnVal = fc.showSaveDialog(SwingUtilities.getWindowAncestor(QueryPanel.this));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String filename = file.getAbsolutePath();
                        fileNameField.setText(filename);
                    } else {
                        System.out.println("Export command cancelled by user.\n");
                        //return null;
                    }

                }
            });
        }
        return fileSelectButton;
    }
    
    protected JCheckBox getSaveByGeneCheckBox() {
        if(saveByGeneCheckBox == null) {
            saveByGeneCheckBox = new JCheckBox();
        }
        return saveByGeneCheckBox;
    }

    protected JCheckBox getSaveByStudyModelCheckBox() {
        if(saveByStudyModelCheckBox == null) {
            saveByStudyModelCheckBox = new JCheckBox();
        }
        return saveByStudyModelCheckBox;
    }

    protected JTextField getBasePairRadiusField() {
        if (basePairRadiusField == null) {
            basePairRadiusField = new JTextField(11);
            Font currFont = basePairRadiusField.getFont();
            basePairRadiusField.setFont(currFont.deriveFont(12f));
            int searchRadius = Singleton.getUserPreferences().getBasePairSearchRadius();
            basePairRadiusField.setText(searchRadius + "");
        }
        return basePairRadiusField;
    }

    protected JList getGeneSnpQueryIdList() {
        if (geneSnpQueryIdList == null) {
            geneSnpQueryIdList = new JList(geneSnpQueryListModel);
        }
        return geneSnpQueryIdList;
    }

    protected AbstractButton getRetrieveButton() {
        if (retrieveButton == null) {
            retrieveButton = new JButton("Retrieve Data");
            Font currFont = retrieveButton.getFont();
            retrieveButton.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+4));
            retrieveButton.setBackground(new Color(176, 196, 222));
            retrieveButton.setForeground(Color.RED);
            //retrieveButton.setPreferredSize(new Dimension(150, 50));
            retrieveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    //SNPDataFetchByGene snpDataFetch = new SNPDataFetchByGene();  12/6/2013 removed pvh
                    DbSnpSourceOption selectedDbSnpOption = getSelectedDbSnpOption();
                    GeneSourceOption geneSourceOption = getSelectedGeneSourceOption();
                    List<ModelOption> selectedModels = getSelectedModelOptions();
                    List<String> selectedGenes = parseGeneTextArea(geneSnpTextArea.getText().trim());
                    int radius = 0;
                    try {
                        radius = Integer.parseInt(basePairRadiusField.getText());
                        radius = Math.abs(radius);
                    } catch(NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(retrieveButton),
                                "Radius around genes should be an integer.",
                                "Invalid Up-/Downstream base pairs",
                                    JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Singleton.getUserPreferences().setBasePairSearchRadius(radius);
                    //snpDataFetch.fetchSnpData(selectedModels, selectedDbSnpOption, geneSourceOption, selectedGenes, radius);
                    System.out.println("Creating dataloader with threads");
                    if(selectedGenes.isEmpty()) {
                            JOptionPane.showMessageDialog((JFrame) SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                            "Include at least one gene in the Gene Search box.",
                            "Query error",
                            JOptionPane.ERROR_MESSAGE);
                    } else if(selectedModels.isEmpty()) {
                            JOptionPane.showMessageDialog((JFrame) SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                            "Include at least one study/set/model for the query.",
                            "Query error",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        Singleton.getDataModel().fetchModelSnpData(selectedModels, selectedDbSnpOption, geneSourceOption, selectedGenes, radius);
                        //newGeneSearchPanel.setSelectedIndex(1);  /* show search history tab */
                    }
                }
            });

        }
        return retrieveButton;
    }
    
    private DbSnpSourceOption getSelectedDbSnpOption() {
        return this.getModelPanel().getSelectedDbSnpOption();
    }
    
    private GeneSourceOption getSelectedGeneSourceOption() {
        return this.getModelPanel().getSelectedGeneSourceOption();
    }
    

    /**
     * Takes the rightmost selection list of the modelPanel with the pingpong buffer
     * and looks up (slow) each of the ModelOption for that field.  It returns
     * the selected model options.
     * @return 
     */
    protected List<ModelOption> getSelectedModelOptions() {
        ArrayList<ModelOption> selectedModelOptions = new ArrayList<ModelOption>();
        for(Object selectedModel : modelPanel.getRightList()) {
            String selectedModelStr = (String) selectedModel;
           for(ModelOption modelOption : modelOptions) {
               if(modelOption.toString().equals(selectedModelStr)) {
                   selectedModelOptions.add(modelOption);
               }
           }
        }
        return selectedModelOptions;
    }
    
    
    /**
     * Parses a multi-line or multi-space file into a list of all-caps gene names
     * in a list.  It removes duplicate names as well.
     * @param textArea
     * @return 
     */
    private List<String> parseGeneTextArea(String textArea) {
        ArrayList<String> geneNames = new ArrayList<String>();
        //String[] tokens = textArea.toUpperCase().split("[\\s\\r\\n]+");
        String[] tokens = textArea.split("[\\s\\r\\n]+");
        for(int tokeni = 0; tokeni < tokens.length; tokeni++) {
            String oneToken = tokens[tokeni].trim();
            if(! oneToken.isEmpty() && ! geneNames.contains(oneToken)) {
                if(oneToken.matches("^\".*\"$")) {
                    oneToken = oneToken.replaceAll("\"", "");
                    geneNames.add(oneToken);
                } else {
                    geneNames.add(oneToken.toUpperCase());
                }
            }
        }
        return geneNames;
    }
    
    protected AbstractButton getClearHistoryButton() {
        if(clearHistoryButton == null) {
            clearHistoryButton = new JButton("Clear History");
            clearHistoryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Singleton.getState().getHistoryTableModel().clear();
                }
            });
        }
        return clearHistoryButton;
    }
    
    protected AbstractButton getClearGenesButton() {
        if(clearGenesButton == null) {
            clearGenesButton = new JButton("Clear");
            clearGenesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    geneSnpTextArea.setText("");
                }
            });
        }
        return clearGenesButton;
    }
    
    protected AbstractButton getUpdateHistoryButton() {
        if(updateHistoryButton == null) {
            updateHistoryButton = new JButton("Update History");
            updateHistoryButton.setToolTipText("Updates the elapsed time");
            updateHistoryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    Singleton.getState().getHistoryTableModel().fireTableDataChanged();
                }
            });
        }
        return updateHistoryButton;
    }
    
    /**
     * Initializes History table as JTable and sets up the column justification
     * and column widths
     * @return 
     */
    protected JTable getRetrievalHistoryTable() {
        if(historyTable==null) {
            historyTable      = new JTable(Singleton.getState().getHistoryTableModel());
            // center all the columns
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment( JLabel.LEFT );
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment( JLabel.CENTER);
            for(int coli = 0; coli < historyTable.getColumnCount(); coli++) {
                if(coli == History.MODELS_COL) {
                    historyTable.getColumnModel().getColumn(coli).setCellRenderer( leftRenderer );
                } else {
                    historyTable.getColumnModel().getColumn(coli).setCellRenderer( centerRenderer);
                }
            }
            historyTable.getColumnModel().getColumn(History.GENE_COL).setPreferredWidth(72);
            historyTable.getColumnModel().getColumn(History.GENE_COL).setMaxWidth(72);
            historyTable.getColumnModel().getColumn(History.GENE_COL).setMinWidth(50);
            
            historyTable.getColumnModel().getColumn(History.RANGE_COL).setPreferredWidth(60);
            historyTable.getColumnModel().getColumn(History.RANGE_COL).setMaxWidth(70);
            historyTable.getColumnModel().getColumn(History.RANGE_COL).setMinWidth(60);
            
            historyTable.getColumnModel().getColumn(History.NUM_SNP_COL).setPreferredWidth(82);
            historyTable.getColumnModel().getColumn(History.NUM_SNP_COL).setMaxWidth(82);
            historyTable.getColumnModel().getColumn(History.NUM_SNP_COL).setMinWidth(62);
            
            historyTable.getColumnModel().getColumn(History.SECONDS_COL).setPreferredWidth(62);
            historyTable.getColumnModel().getColumn(History.SECONDS_COL).setPreferredWidth(62);
            historyTable.getColumnModel().getColumn(History.SECONDS_COL).setMaxWidth(62);
            
            historyTable.getColumnModel().getColumn(History.STATUS_COL).setPreferredWidth(52);
            historyTable.getColumnModel().getColumn(History.STATUS_COL).setMaxWidth(52);
            historyTable.getColumnModel().getColumn(History.STATUS_COL).setMinWidth(52);
            
            historyTable.getColumnModel().getColumn(History.MODEL_COUNT_COL).setPreferredWidth(52);
            historyTable.getColumnModel().getColumn(History.MODEL_COUNT_COL).setMaxWidth(52);
            historyTable.getColumnModel().getColumn(History.MODEL_COUNT_COL).setMinWidth(52);
            
            historyTable.getColumnModel().getColumn(History.MODELS_COL).setPreferredWidth(500);
            //historyTable.getColumnModel().getColumn(History.MODELS_COL).setMaxWidth(200);
            historyTable.getColumnModel().getColumn(History.MODELS_COL).setMaxWidth(1000);
            historyTable.setPreferredScrollableViewportSize(historyTable.getPreferredSize());

            //historyTable.setPreferredSize(new Dimension(900,200));
            //historyTable.setPreferredScrollableViewportSize(historyTable.getSize());
            historyTable.setPreferredScrollableViewportSize(Toolkit.getDefaultToolkit().getScreenSize());
            //historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        return historyTable;
    }
    
}

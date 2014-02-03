/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.query;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.TransmartClient.TransmartDataLoaderWithThreads;
import com.pfizer.mrbt.genomics.TransmartClient.TransmartQueryParameterFetch;
import com.pfizer.mrbt.genomics.bioservices.DataLoaderWithThreads;
import com.pfizer.mrbt.genomics.bioservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.bioservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.bioservices.ModelOption;
import com.pfizer.mrbt.genomics.bioservices.QueryParameterFetch;
import com.pfizer.mrbt.genomics.bioservices.SNPDataFetchByGene;
import com.pfizer.mrbt.genomics.state.State;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author henstock
 */
public class QueryPanel extends JComponent {

    private JTextArea entrezIdTextArea;

    private AbstractButton retrieveButton;
    private Collection availableModelsList;
    private Collection selectedModelsList;
    private DefaultListModel entrezIdListModel = new DefaultListModel();
    private PingPongBufferPane modelPanel;
    private JComponent geneSearchPanel;
    private JTabbedPane middlePanel;
    private JComponent searchHistoryPanel;
    private JComponent historyButtonPanel;
    private JComponent buttonPanel;
    
    private JComponent filePanel;
    private JTextField fileNameField;
    private JCheckBox saveByGeneCheckBox;
    private JCheckBox saveByStudyModelCheckBox;
    private AbstractButton fileSelectButton;
    private AbstractButton clearGenesButton;
    
    private AbstractButton clearHistoryButton;
    private AbstractButton updateHistoryButton;
    
    private JComboBox snpAnnotationComboBox;
    private JComboBox geneAnnotationComboBox;
    private JList entrezIdList;
    private JTextField basePairRadiusField;
    private List<ModelOption> modelOptions = new ArrayList<ModelOption>();
    private List<DbSnpSourceOption> dbSnpOptions = new ArrayList<DbSnpSourceOption>();
    private List<GeneSourceOption> geneSourceOptions = new ArrayList<GeneSourceOption>();
    private JTable historyTable;
    
    public QueryPanel() {
        super();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 10, 2, 10);
        //gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        add(getModelPanel(), gbc);
        //getModelPanel();

        gbc.gridy = 20;
        gbc.weighty = 0.2;
        add(getMiddlePanel(), gbc);
        //getMiddlePanel();

        gbc.gridy = 30;
        //add(getFilePanel(), gbc);

        gbc.insets = new Insets(2, 10, 2, 10);
        gbc.weighty = 0.0;
        gbc.gridy = 40;
        add(getButtonPanel(), gbc);
    }

    protected PingPongBufferPane getModelPanel() {
        if (modelPanel == null) {
            availableModelsList = getAvailableModelsList();
            selectedModelsList = new ArrayList();
            String dialogName = "dialogName";
            String leftToRightStr = "Include";
            String rightToLeftStr = "Exclude";

            Object[] models = availableModelsList.toArray();
            /*for(int i = 2; i >= 0; i--) {
                availableModelsList.add(models[i]);
                selectedModelsList.add(models[i]);
                availableModelsList.remove(models[i]);
            }*/
            modelPanel = new PingPongBufferPane(selectedModelsList, availableModelsList, dialogName, leftToRightStr, rightToLeftStr);
            modelPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }

        return modelPanel;
    }

    /**
     * If dataMode == DEMO_MODE then it fills and returns availableModelsList.
     * If dataMode == BIOSERVICES_MODE, it pulls the list from bioservices
     * and keeps the list in modelOptions list class vairable
     * @return 
     */
    protected Collection getAvailableModelsList() {
        if (availableModelsList == null) {
            availableModelsList = new ArrayList();
            int dataMode = Singleton.getState().getDataMode();
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
                    System.out.println("Loaded model option " + modelOption.toVerboseString());
                    availableModelsList.add(modelOption.toString());
                }
            } else if(dataMode == State.BIOSERVICES_MODE) {
                QueryParameterFetch qpf = new QueryParameterFetch();
                modelOptions = qpf.fetchModelOptions();
                for(ModelOption modelOption : modelOptions) {
                    availableModelsList.add(modelOption.toString());
                }
            }
        }
        return availableModelsList;
    }

    protected JTabbedPane getMiddlePanel() {
        if(middlePanel == null) {
            middlePanel = new JTabbedPane();
            Font currFont = middlePanel.getFont();
            middlePanel.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+4));
            middlePanel.add("Gene Search", getGeneSearchPanel());
            middlePanel.add("Search History", getSearchHistoryPanel());
            middlePanel.setForegroundAt(middlePanel.getSelectedIndex(), Color.RED);
            middlePanel.setBackgroundAt(middlePanel.getSelectedIndex(), Color.WHITE);
            middlePanel.addChangeListener(new ChangeListener() {
                @Override
               public void stateChanged(ChangeEvent ce) {
                   middlePanel.setForegroundAt(0, Color.BLACK);
                   middlePanel.setForegroundAt(1, Color.BLACK);
                   middlePanel.setBackgroundAt(0, Color.LIGHT_GRAY);
                   middlePanel.setBackgroundAt(1, Color.LIGHT_GRAY);
                   middlePanel.setForegroundAt(middlePanel.getSelectedIndex(), Color.RED);
                   middlePanel.setBackgroundAt(middlePanel.getSelectedIndex(), Color.WHITE);
               }
            });
        }
        return middlePanel;
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
            /*gbc.gridx = 10;
            gbc.gridy = 20;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            JLabel entrezLabel = new JLabel("Gene names:");
            Font currFont = entrezLabel.getFont();
            Font labelFont = new Font(currFont.getName(), Font.PLAIN, 13);
            entrezLabel.setFont(labelFont);
            geneSearchPanel.add(entrezLabel, gbc);*/
            
            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.gridheight = 40;
            //gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(2,0,0,30);
            entrezIdTextArea = new JTextArea(11, 30);
            entrezIdTextArea.setText("");
            //entrezIdTextArea.setToolTipText("Type or copy a delimited list of gene names such as: TNF IL6");
            entrezIdTextArea.setToolTipText("<html>Type or copy a delimited list of gene names such as: TNF IL6<br/>Genes with lower case (C10orf11) should be enclosed in double quotes</html>.");
            
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            entrezIdTextArea.setText("A2M TNF il6");
            entrezIdTextArea.setText("");
            entrezIdTextArea.setWrapStyleWord(true);
            geneSearchPanel.add(new JScrollPane(entrezIdTextArea), gbc);


            
            
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
            geneSearchPanel.add(getSnpAnnotationComboBox(), gbc);

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
            geneSearchPanel.add(getGeneAnnotationComboBox(), gbc);

            gbc.gridx = 40;
            gbc.gridy = 40;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel basePairRadiusLabel = new JLabel("<html><p align=right>Up-/Downstream<br/>(+/- base-pairs):</p></html>");
            //basePairRadiusLabel.setHorizontalAlignment(JLabel.RIGHT);
            basePairRadiusLabel.setFont(labelFont);
            geneSearchPanel.add(basePairRadiusLabel, gbc);

            gbc.gridx = 50;
            gbc.gridy = 40;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            geneSearchPanel.add(getBasePairRadiusField(), gbc);
            
            gbc.gridx = 40;
            gbc.gridy = 50;
            gbc.anchor = GridBagConstraints.LINE_END;
            geneSearchPanel.add(getClearGenesButton(), gbc);


            geneSearchPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
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
            searchHistoryPanel.add(getHistoryButtonPanel(), gbc);
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
            buttonPanel.add(getRetrieveButton(), gbc);
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

    protected JComboBox getSnpAnnotationComboBox() {
        if (snpAnnotationComboBox == null) {
            snpAnnotationComboBox = new JComboBox();
            int dataMode = Singleton.getState().getDataMode();
            if(dataMode == State.DEMO_MODE) {
                snpAnnotationComboBox.addItem("dbSNP from UCSC Golden Path hg19 (snp135)");
                snpAnnotationComboBox.addItem("dbSNP from UCSC Golden Path hg18 (snp130)");
                snpAnnotationComboBox.addItem("dbSNP from UCSC Golden Path hg18 (snp129)");
            } else if(dataMode == State.TRANSMART_SERVICES_MODE ||
                      dataMode == State.TRANSMART_DEV_SERVICES_MODE) {
                TransmartQueryParameterFetch tqpf = new TransmartQueryParameterFetch();
                dbSnpOptions = tqpf.getDbSnpSources();
                for(DbSnpSourceOption dbSnpOption : dbSnpOptions) {
                    snpAnnotationComboBox.addItem(dbSnpOption.toString());
                }
            } else if(dataMode == State.BIOSERVICES_MODE) {
                QueryParameterFetch qpf = new QueryParameterFetch();
                dbSnpOptions = qpf.getDbSnpSources();
                for(DbSnpSourceOption dbSnpOption : dbSnpOptions) {
                    snpAnnotationComboBox.addItem(dbSnpOption.toString());
                }
            }
        }
        return snpAnnotationComboBox;
    }

    protected JComboBox getGeneAnnotationComboBox() {
        if (geneAnnotationComboBox == null) {
            geneAnnotationComboBox = new JComboBox();
            int dataMode = Singleton.getState().getDataMode();
            if(dataMode == State.DEMO_MODE) {
                geneAnnotationComboBox.addItem("Human Gene data from NCBI (GRCh37) (2012-03)");
            } else if(dataMode == State.TRANSMART_SERVICES_MODE ||
                      dataMode == State.TRANSMART_DEV_SERVICES_MODE) {
                TransmartQueryParameterFetch tqpf = new TransmartQueryParameterFetch();
                geneSourceOptions = tqpf.getGeneSources();
                for(GeneSourceOption geneSourceOption : geneSourceOptions) {
                    geneAnnotationComboBox.addItem(geneSourceOption.toString());
                }
            } else if(dataMode == State.BIOSERVICES_MODE) {
                QueryParameterFetch qpf = new QueryParameterFetch();
                geneSourceOptions = qpf.getGeneSources();
                for(GeneSourceOption geneSourceOption : geneSourceOptions) {
                    geneAnnotationComboBox.addItem(geneSourceOption.toString());
                }
            }
        }
        return geneAnnotationComboBox;
    }

    protected JTextField getBasePairRadiusField() {
        if (basePairRadiusField == null) {
            basePairRadiusField = new JTextField(11);
            int searchRadius = Singleton.getUserPreferences().getBasePairSearchRadius();
            basePairRadiusField.setText(searchRadius + "");
        }
        return basePairRadiusField;
    }

    protected JList getEntrezIdList() {
        if (entrezIdList == null) {
            entrezIdList = new JList(entrezIdListModel);
        }
        return entrezIdList;
    }

    protected AbstractButton getRetrieveButton() {
        if (retrieveButton == null) {
            retrieveButton = new JButton("Retrieve Data");
            Font currFont = retrieveButton.getFont();
            retrieveButton.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+4));
            retrieveButton.setPreferredSize(new Dimension(150, 50));
            retrieveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    SNPDataFetchByGene snpDataFetch = new SNPDataFetchByGene();
                    DbSnpSourceOption selectedDbSnpOption = dbSnpOptions.get(snpAnnotationComboBox.getSelectedIndex());
                    GeneSourceOption geneSourceOption = geneSourceOptions.get(geneAnnotationComboBox.getSelectedIndex());
                    List<ModelOption> selectedModels = getSelectedModelOptions();
                    List<String> selectedGenes = parseGeneTextArea(entrezIdTextArea.getText().trim());
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
                    } else if(Singleton.getState().getDataMode() == State.BIOSERVICES_MODE) {
                        DataLoaderWithThreads dlwt = new DataLoaderWithThreads(selectedModels,
                                selectedDbSnpOption, geneSourceOption,
                                selectedGenes, radius);
                        System.out.println("finished creating dataloader with threads");
                        dlwt.fetchGeneData();
                        middlePanel.setSelectedIndex(1);  /* show search history tab */
                    } else if(Singleton.getState().getDataMode() == State.TRANSMART_SERVICES_MODE ||
                              Singleton.getState().getDataMode() == State.TRANSMART_DEV_SERVICES_MODE) {
                        TransmartDataLoaderWithThreads tdlwt = new TransmartDataLoaderWithThreads(selectedModels,
                                selectedDbSnpOption, geneSourceOption,
                                selectedGenes, radius);
                        System.out.println("finished creating dataloader with threads");
                        tdlwt.fetchGeneData();
                        middlePanel.setSelectedIndex(1);  /* show search history tab */
                    }
                    //System.out.println("Finished fetchGeneData");
                }
            });

        }
        return retrieveButton;
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
            clearGenesButton = new JButton("Clear Genes");
            clearGenesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    entrezIdTextArea.setText("");
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
    
    protected JTable getRetrievalHistoryTable() {
        if(historyTable==null) {
            historyTable      = new JTable(Singleton.getState().getHistoryTableModel());
            // center all the columns
            for(int coli = 0; coli < historyTable.getColumnCount(); coli++) {
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment( JLabel.CENTER);
                historyTable.getColumnModel().getColumn(coli).setCellRenderer( centerRenderer);
            }
            historyTable.setPreferredScrollableViewportSize(historyTable.getPreferredSize());
            //historyTable.setPreferredSize(new Dimension(200,200));
        }
        return historyTable;
    }
}

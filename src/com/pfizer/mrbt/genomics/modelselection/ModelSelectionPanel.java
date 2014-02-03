/*
 * Panel that enables the user to select the particular model they are interested
 * in looking at
 */
package com.pfizer.mrbt.genomics.modelselection;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.thumbnail.ThumbnailPanel;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.heatmap.HeatmapPanel;
import com.pfizer.mrbt.genomics.heatmap.HeatmapTableModel;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.ViewData;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class ModelSelectionPanel extends JPanel {
    private JTable modelSelectionTable;
    private AbstractTableModel modelSelectionTableModel;
    private AbstractButton modelSelectionButton;
    private AbstractButton showThumbnailsButton;
    private AbstractButton showHeatmapButton;
    private AbstractButton removeSelectionButton;
    private AbstractButton removeAllButton;
    private AbstractButton unselectAllButton;
    private AbstractButton selectAllButton;
    private JComponent topButtonPanel;
    
    private JComponent filterPanel;
    private AbstractButton filterButton;
    private AbstractButton showAllButton;
    private JTextField filterField;
    private JTextField logPThreshField;
    private AbstractButton thresholdButton;
    private JLabel showingLabel;
    //private JTextField totalField;
    
    private JComponent bottomPanel;
    private JFrame heatmapFrame;
    private JComponent heatmapPanel;
    public final static int PREFERRED_WIDTH = 400;
    public final static int PREFERRED_HEIGHT = 100;
    private HashMap<String, AxisScale> gene2xAxisScale = new HashMap<String,AxisScale>();
    public ModelSelectionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(getTopButtonPanel(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 20;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(getModelSelectionTable(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //scrollPane.setPreferredSize(new Dimension(200,1000));
        add(scrollPane, gbc);
        
        /*gbc.gridx = 10;
        gbc.gridy = 30;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        add(getModelSelectionButton(), gbc);*/
        
        gbc.gridx = 10;
        gbc.gridy = 30;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(getFilterPanel(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 40;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(getBottomPanel(), gbc);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
        ResultsTableController rtc = new ResultsTableController();
        ((ModelSelectionTableModel) modelSelectionTableModel).addListener(rtc);
    }

    /**
     * Creates a bottom panel for selecting multiple plots and creating thumbnails
     * @return 
     */
    protected JComponent getTopButtonPanel() {
        if (topButtonPanel == null) {
            topButtonPanel = new JPanel();
            topButtonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.insets = new Insets(3, 5, 3, 5);
            topButtonPanel.add(getSelectAllButton(), gbc);
            gbc.fill = GridBagConstraints.VERTICAL;
            topButtonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.VERTICAL;
            topButtonPanel.add(getUnselectAllButton(), gbc);
            topButtonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

            gbc.gridx = 10;
            gbc.gridy = 20;
            gbc.weighty = 0.0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.VERTICAL;
            topButtonPanel.add(getRemoveSelectionButton(), gbc);

            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.fill = GridBagConstraints.VERTICAL;
            topButtonPanel.add(getRemoveAllButton(), gbc);
            
            selectAllButton.setPreferredSize(removeSelectionButton.getPreferredSize());
            unselectAllButton.setPreferredSize(removeSelectionButton.getPreferredSize());
            removeAllButton.setPreferredSize(removeSelectionButton.getPreferredSize());

            topButtonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }
        return topButtonPanel;

    }
    
    /**
     * Creates a bottom panel for selecting multiple plots and creating thumbnails
     * @return 
     */
    protected JComponent getFilterPanel() {
        if (filterPanel == null) {
            filterPanel = new JPanel();
            filterPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.weightx = 0.1;
            gbc.weighty = 0.0;
            gbc.gridwidth = 1;
            gbc.gridwidth = 9;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JLabel geneModelLabel = new JLabel("Gene/Model");
            filterPanel.add(geneModelLabel, gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(3, 3, 3, 3);
            filterField = new JTextField(14);
            filterField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    int keyCode = ke.getKeyCode();
                    if (keyCode == KeyEvent.VK_ENTER) {
                        getFilterButton().doClick();
                    }
                }
            });

            
            
            filterPanel.add(filterField, gbc);

            gbc.gridx = 35;
            gbc.gridy = 10;
            gbc.gridwidth = 1;
            gbc.gridheight = 19;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            gbc.weightx = 0.1;;
            filterPanel.add(getFilterButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            filterPanel.add(new JLabel("Min -logPval:"), gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            logPThreshField = new JTextField(6);
            logPThreshField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    int keyCode = ke.getKeyCode();
                    if (keyCode == KeyEvent.VK_ENTER) {
                        getFilterButton().doClick();
                    }
                }
            });
            filterPanel.add(logPThreshField, gbc);
            
            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.gridwidth = 20;
            gbc.weightx = 0.01;
            //filterPanel.add(getThresholdButton(), gbc);
            
            gbc.gridx = 10;
            gbc.gridy = 30;
            gbc.gridwidth = 1;
            gbc.gridwidth = 19;
            showingLabel = new JLabel("Showing 0 of 0");
            filterPanel.add(showingLabel, gbc);

            gbc.gridx = 30;
            gbc.gridy = 30;
            filterPanel.add(getShowAllButton(), gbc);
            

            filterPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        return filterPanel;
    }

    protected AbstractButton getFilterButton() {
        if(filterButton == null) {
            filterButton = new JButton("Filter");
            filterButton.addActionListener(new ActionListener() {
                @Override
               public void actionPerformed(ActionEvent ae) {
                    try {
                        float threshold = -1f;
                        if(! logPThreshField.getText().trim().isEmpty()) {
                            threshold = Float.parseFloat(logPThreshField.getText());
                        }
                        String filterStr = filterField.getText().trim();
                       ((ModelSelectionTableModel) modelSelectionTableModel).filterResults(threshold, filterStr);
                       updateFilterLabel();
                    } catch(NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(ModelSelectionPanel.this),
                            "-Log10(P-value) threshold must be a decimal number.",
                            "Illegal -logPval threshold",
                            JOptionPane.ERROR_MESSAGE);
                    }
               } 
            });
        }
        return filterButton;
    }
    
    protected AbstractButton getShowAllButton() {
        if(showAllButton == null) {
            showAllButton = new JButton("Show All");
            showAllButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                   ((ModelSelectionTableModel) modelSelectionTableModel).showAllResults();
                   updateFilterLabel();
               } 
            });
        }
        return showAllButton;
    }

    
    /**
     * Creates a bottom panel for selecting multiple plots and creating thumbnails
     * @return 
     */
    protected JComponent getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.weighty = 0.0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.insets = new Insets(5, 2, 5, 2);
            bottomPanel.add(getModelSelectionButton(), gbc);

            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.VERTICAL;
            bottomPanel.add(getShowThumbnailsButton(), gbc);

            gbc.gridx = 30;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.VERTICAL;
            bottomPanel.add(getShowHeatmapButton(), gbc);
            
            //showThumbnailsButton.setPreferredSize(showThumbnailsButton.getPreferredSize());
            showHeatmapButton.setPreferredSize(modelSelectionButton.getPreferredSize());

            bottomPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }
        return bottomPanel;

    }
    
    /**
     * Creates a bottom panel for selecting multiple plots and creating thumbnails
     * @return 
     */
    protected JComponent getVerticalBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 10;
            gbc.gridy = 30;
            gbc.weighty = 0.0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            bottomPanel.add(getModelSelectionButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 50;
            gbc.fill = GridBagConstraints.VERTICAL;
            bottomPanel.add(getShowThumbnailsButton(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 60;
            gbc.fill = GridBagConstraints.VERTICAL;
            bottomPanel.add(getShowHeatmapButton(), gbc);
            
            showThumbnailsButton.setPreferredSize(modelSelectionButton.getPreferredSize());
            showHeatmapButton.setPreferredSize(modelSelectionButton.getPreferredSize());

            bottomPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        }
        return bottomPanel;

    }
    
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }
    
    public AbstractButton getModelSelectionButton() {
        if(modelSelectionButton==null) {
            //modelSelectionButton = new JButton("(Multi)-Colored Plot");
            modelSelectionButton = new JButton("<html><center>Colored<br/>Plot</center></html>");
            modelSelectionButton.setToolTipText("Uses colors to show 1+ models for a the selected gene");
            Font currFont = modelSelectionButton.getFont();
            modelSelectionButton.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+2));
            modelSelectionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedRows = modelSelectionTable.getSelectedRows();
                    int numSelectedRows = selectedRows.length;
                    for(int selectedRow = numSelectedRows-1; selectedRow >= 0; selectedRow--) {
                        String currGeneName = (String) modelSelectionTable.getValueAt(selectedRows[0], ModelSelectionTableModel.GENE_COL);
                        boolean multipleDataSetsAdded = false;
                        DataModel dataModel = Singleton.getDataModel();
                        DataSet dataSet = Singleton.getDataModel().getDataSet(currGeneName);
                        ViewData viewData = new ViewData(dataSet);
                        Singleton.getState().addViewData(viewData);
                        System.out.println("Data set in getModelSelectionButton has " + dataSet.getModels().size() + " models");
                        for(int rowi = 0; rowi < selectedRows.length; rowi++) {
                            int row = selectedRows[rowi];
                            String dataSetName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.GENE_COL);
                            if(dataSetName.equals(currGeneName)) {  // can only include one dataSet at time
                                String combinedModelName   = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.MODEL_COL);
                                for(Model model : dataSet.getModels()) {
                                    if(model.toString().startsWith(combinedModelName)) {
                                        viewData.addModel(model);
                                    }
                                }
                            } else {
                                multipleDataSetsAdded = true;
                            }
                        }
                        if(multipleDataSetsAdded) {
                            JOptionPane.showMessageDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(modelSelectionButton),
                                "Models from only one gene can be viewed at one time.  We are using the first gene and ignoring the rest.",
                                "Invliad options",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        Singleton.getState().setMainView(viewData);
                    }
                }
            });
        }
        return modelSelectionButton;
    }

    /**
     * Creates and returns the Show Thumbnails button.  When clicked, it 
     * gets the dataset (gene name) and puts it in the viewdata with the
     * appropriate mdoels.  These are added to the State.thumbnailViewDatas
     * list
     * @return 
     */
    protected AbstractButton getShowThumbnailsButton() {
        if (showThumbnailsButton == null) {
            //showThumbnailsButton = new JButton("Thumbnail Plots");
            showThumbnailsButton = new JButton("<html><center>Thumbnail<br/>Plot</center></html>");
            showThumbnailsButton.setToolTipText("Creates a small plot for each gene-model combination");
            Font currFont = showThumbnailsButton.getFont();
            showThumbnailsButton.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+2));
            showThumbnailsButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    System.out.println("Show thumbnails button");
                    gene2xAxisScale.clear();
                    double maxYvalue = Double.MIN_VALUE;
                    int[] selectedRows = modelSelectionTable.getSelectedRows();
                    if(selectedRows.length==0) {
                        selectedRows = ((ModelSelectionTableModel) modelSelectionTableModel).getAllRows();
                    }
                    Singleton.getState().clearThumbnailViewData();
                    for (int rowi = 0; rowi < selectedRows.length; rowi++) {
                        int row = selectedRows[rowi];  //pvh changed to allow filtering
                        String currGeneName = (String) modelSelectionTable.getValueAt(row, 0);
                        DataSet dataSet = Singleton.getDataModel().getDataSet(currGeneName);
                        String dataSetName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.GENE_COL);
                        System.out.println("Selected Thumbnail " + currGeneName + "\t" + dataSetName);
                        //System.out.println("DataSetName " + dataSetName + "\tRow " + row);
                        String combinedModelName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.MODEL_COL);
                        for (Model model : dataSet.getModels()) {
                            if (model.toString().startsWith(combinedModelName)) {
                                ViewData viewData = new ViewData(dataSet);
                                viewData.addModel(model);
                                maxYvalue = Math.max(maxYvalue, dataSet.getYRange(model).getMax());
                                Singleton.getState().addThumbnailViewData(viewData);
                            }
                        }
                    }
                    int numThumbs = selectedRows.length;
                    for (int thumbi = 0; thumbi < numThumbs; thumbi++) {
                        ViewData oneViewData = Singleton.getState().getThumbnailViewData(thumbi);
                        AxisScale yAxisScale = oneViewData.getYAxis();
                        yAxisScale.setDisplayMinMaxValue(0.0, maxYvalue);
                    }
                    Singleton.getState().fireThumbnailsChanged();
                }
            });
        }
        return showThumbnailsButton;
    }

    /**
     * Creates a frame that has the selected models and genes(or SNPS) in a
     * table with a way of computing the best results
     * @return 
     */
    protected AbstractButton getShowHeatmapButton() {
        if (showHeatmapButton == null) {
            //showHeatmapButton = new JButton("Heat Map");
            showHeatmapButton = new JButton("<html><center>Heat<br/>Map</center></html>");
            Font currFont = showHeatmapButton.getFont();
            showHeatmapButton.setFont(new Font(currFont.getName(), Font.BOLD, currFont.getSize()+2));
            showHeatmapButton.setToolTipText("Creates a colored heat map of all models versus genes.");
            showHeatmapButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    gene2xAxisScale.clear();
                    double maxYvalue = Double.MIN_VALUE;
                    int[] selectedRows = modelSelectionTable.getSelectedRows();
                    if(selectedRows.length==0) {
                        selectedRows = ((ModelSelectionTableModel) modelSelectionTableModel).getAllRows();
                    }
                    Singleton.getState().clearHeatmapData();
                    for (int rowi = 0; rowi < selectedRows.length; rowi++) {
                        int row = selectedRows[rowi];
                        String currGeneName = (String) modelSelectionTable.getValueAt(row, 0);
                        DataSet dataSet = Singleton.getDataModel().getDataSet(currGeneName);
                        String dataSetName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.GENE_COL);
                        //System.out.println("DataSetName " + dataSetName + "\tRow " + row);
                        String combinedModelName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.MODEL_COL);
                        for (Model model : dataSet.getModels()) {
                            if (model.toString().startsWith(combinedModelName)) {
                                ViewData viewData = new ViewData(dataSet);
                                viewData.addModel(model);
                                maxYvalue = Math.max(maxYvalue, dataSet.getYRange(model).getMax());
                                Singleton.getState().addHeatmapViewData(viewData);
                            }
                        }
                    }
                    //HeatMapPanel heatMapPanel = new HeatMapPanel()
                    //JFrame frame = new JFrame(heatMapPanel);
                    try {
                        createHeatmapFrame();
                    } catch(Exception ex) {
                        System.out.println("Failed to createHeatmapFrame ");
                        ex.printStackTrace();
                    }
                    Singleton.getState().fireHeatmapChanged();
                }
            });
        }
        return showHeatmapButton;
    }
    
    
    /**
     * Creates an invisible JFrame with the thumbnail data
     * @throws Exception 
     */
    private void createHeatmapFrame() throws Exception {
        if(heatmapFrame == null || ! heatmapFrame.isVisible()) {
            heatmapFrame = new JFrame("GWAVA Heat Map");
            heatmapPanel = new HeatmapPanel();
            heatmapFrame.setContentPane(heatmapPanel);
            heatmapFrame.setPreferredSize(new Dimension(415, 400));
            try {
                //System.out.println("Class path" + System.getProperty("java.class.path"));
                java.net.URL imgURL = this.getClass().getResource("/images/guava_16.jpg");
                heatmapFrame.setIconImage(new ImageIcon(imgURL).getImage());
            } catch (NullPointerException npe) {
                System.out.println("Failed to load in the icon.");
            }
            heatmapFrame.setLocation(800, 150);
            heatmapFrame.pack();
            heatmapFrame.setVisible(true);
        }
    }    
    
    /*protected void createHeatMapFrame() throws Exception {
        heatMapFrame = new JFrame("GWAVA Gene Model Selection");
        geneModelFrame.setContentPane(getModelSelectionPanel());
        try {
            //System.out.println("Class path" + System.getProperty("java.class.path"));
            java.net.URL imgURL = this.getClass().getResource("/images/guava_16.jpg");
            geneModelFrame.setIconImage(new ImageIcon(imgURL).getImage());
        } catch (NullPointerException npe) {
            System.out.println("Failed to load in the icon.");
        }

        //geneModelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.addWindowListener(new WindowCloseController());
        geneModelFrame.pack();
        geneModelFrame.setLocation(frame.getLocation().x + frame.getWidth(), frame.getY());
        geneModelFrame.setPreferredSize(new Dimension(250, frame.getHeight()));
        geneModelFrame.setSize(new Dimension(250, frame.getHeight()));
        geneModelFrame.setVisible(true);
        
    }*/



    /**
     * Creates a new frame if necessary to house the ThumbnailPanel
     */
    protected void launchThumbnailFrame() {
        try {
            ThumbnailPanel panel = new ThumbnailPanel();
            JFrame frame = new JFrame();
            frame.setContentPane(panel);
            //frame.setJMenuBar(new MenuBar());
            try {
                System.out.println("Class path" + System.getProperty("java.class.path"));
                java.net.URL imgURL = this.getClass().getResource("/images/antibody16.jpg");
                frame.setIconImage(new ImageIcon(imgURL).getImage());
            } catch (NullPointerException npe) {
                System.out.println("Failed to load in the icon.");
            }

            frame.setPreferredSize(new Dimension(1180, 700));
            frame.pack();
            frame.setVisible(true);


        } catch (Exception e) {
            System.out.println("Failure in the launch Thumbnail frame " + e);
            e.printStackTrace();
        }
    }

    public JTable getModelSelectionTable() {
        if (modelSelectionTable == null) {
            modelSelectionTableModel = new ModelSelectionTableModel();
            modelSelectionTable = new JTable(modelSelectionTableModel);
            modelSelectionTable.setAutoCreateRowSorter(true);
            //modelSelectionTable.setPreferredSize(new Dimension(800,1000));
            //modelSelectionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.SCORE_COL).setMaxWidth(35);
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.COLOR_COL).setCellRenderer(new ColorSquareRenderer());
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.COLOR_COL).setMaxWidth(20);
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.GENE_COL).setMinWidth(60);
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.GENE_COL).setMaxWidth(90);
            modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.MODEL_COL).setMinWidth(100);
            
            //modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.MODEL_COL).setMaxWidth(1000);
            //modelSelectionTable.getColumnModel().getColumn(ModelSelectionTableModel.MODEL_COL).setPreferredWidth(200);
            modelSelectionTable.getTableHeader().setToolTipText("<html>Click or select 1+ rows to view SNP data.<br/>Score = max SNP for given gene-model view.</html>");
            modelSelectionTable.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent me) {
                    if (me.getClickCount() == 2) {
                        getModelSelectionButton().doClick();
                    }
                }
            });
        }
        return modelSelectionTable;
    }

    /**
     * Clears the selected 
     * @return 
     */
    protected AbstractButton getRemoveSelectionButton() {
        if (removeSelectionButton == null) {
            removeSelectionButton = new JButton("Remove Selected");
            removeSelectionButton.setToolTipText("Permanently removes the selected study/set/models from the list");
            removeSelectionButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    int[] selectedRows = modelSelectionTable.getSelectedRows();
                    for (int rowi = selectedRows.length-1; rowi >= 0; rowi--) {
                        int row = selectedRows[rowi];
                        String currGeneName = (String) modelSelectionTable.getValueAt(row, 0);
                        DataSet dataSet = Singleton.getDataModel().getDataSet(currGeneName);
                        String dataSetName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.GENE_COL);
                        System.out.println("DataSetName " + dataSetName + "\tRow " + row);
                        String combinedModelName = (String) modelSelectionTable.getValueAt(row, ModelSelectionTableModel.MODEL_COL);
                        boolean removeAllModels = true;
                        ArrayList<Model> modelsToRemove = new ArrayList<Model>();
                        for (Model model : dataSet.getModels()) {
                            if (model.toString().startsWith(combinedModelName)) {
                                modelsToRemove.add(model);
                            }
                        }
                        for(Model model : modelsToRemove) {
                            dataSet.removeModel(model);
                        }
                        if(dataSet.getModels().size() == 0) {
                            Singleton.getDataModel().removeDataSet(currGeneName);
                        }
                    }
                    if(selectedRows.length > 0) {
                        modelSelectionTableModel.fireTableDataChanged();                        
                        Singleton.getDataModel().fireDataChanged();
                    }
                }
            });
        }
        return removeSelectionButton;
    }
    
    /**
     * Updates the showingLabel with the information from the table model
     */
    protected void updateFilterLabel() {
        showingLabel.setText(((ModelSelectionTableModel) modelSelectionTableModel).getFilterShowingStatusString());
    }
    


    /**
     * Clears the selected 
     * @return 
     */
    protected AbstractButton getRemoveAllButton() {
        if (removeAllButton == null) {
            removeAllButton = new JButton("Remove All");
            removeAllButton.setToolTipText("Permanently removes the all the data");
            removeAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    ArrayList<String> geneNames = new ArrayList<String>();
                    for(int rowi = 0; rowi < modelSelectionTable.getRowCount(); rowi++) {
                        String firstColGeneName = (String) modelSelectionTable.getValueAt(rowi, 0);
                        if(! geneNames.contains(modelSelectionTable.getValueAt(rowi, 0))) {
                            geneNames.add((String) modelSelectionTable.getValueAt(rowi, 0));
                        }
                    }
                    for(String geneName : geneNames) {
                        Singleton.getDataModel().removeDataSet(geneName);
                    }
                    if(geneNames.size() > 0) {
                        modelSelectionTableModel.fireTableDataChanged();
                        Singleton.getDataModel().fireDataChanged();
                    }
                }
            });
        }
        return removeAllButton;
    }

    /**
     * Clears the selected 
     * @return 
     */
    protected AbstractButton getUnselectAllButton() {
        if (unselectAllButton == null) {
            unselectAllButton = new JButton("Unselect All");
            unselectAllButton.setToolTipText("Unselects all rows of the table");
            unselectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    //modelSelectionTable.clearSelection();
                    //modelSelectionTable.getRowCount();
                    modelSelectionTableModel.fireTableDataChanged();
                }
            });
        }
        return unselectAllButton;
    }

    /**
     * Clears the selected 
     * @return 
     */
    protected AbstractButton getSelectAllButton() {
        if (selectAllButton == null) {
            selectAllButton = new JButton("Select All");
            selectAllButton.setToolTipText("Selects all rows of the table");
            selectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if(modelSelectionTable.getRowCount() > 0) {
                        modelSelectionTable.setRowSelectionInterval(0, modelSelectionTable.getRowCount()-1);
                    }
                }
            });
        }
        return selectAllButton;
    }

    public class StateController implements StateListener {
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            if(heatmapPanel != null) {
                HeatmapTableModel heatmapTableModel = ((HeatmapPanel) heatmapPanel).getHeatmapTableModel();
                heatmapTableModel.fireTableDataChanged();
            }
        }
        @Override
        public void currentChanged(ChangeEvent ce) {   }

        @Override
        public void thumbnailsChanged(ChangeEvent ce) {   }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) { }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) { }

        @Override
        public void averagingWindowChanged(ChangeEvent ce) {    }
        
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { }
        
        @Override
        public void heatmapChanged(ChangeEvent ce) { 
            if(heatmapPanel != null) {
                HeatmapTableModel heatmapTableModel = ((HeatmapPanel) heatmapPanel).getHeatmapTableModel();
                heatmapTableModel.updateData();
                JTable heatmapTable = ((HeatmapPanel) heatmapPanel).getHeatmapTable();
                //heatmapTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                //heatmapTable.setAutoCreateRowSorter(true);
                //heatmapTable.setAutoResizeMode(WIDTH);
                for(int coli = 1; coli < heatmapTableModel.getColumnCount(); coli++) {
                    heatmapTable.getColumnModel().getColumn(coli).setPreferredWidth(47);
                    heatmapTable.getColumnModel().getColumn(coli).setMinWidth(15);
                    heatmapTable.getColumnModel().getColumn(coli).setMaxWidth(65);
                    System.out.println("Setting the heatmap column widths " + coli);
                    //heatmapTable.getColumnModel().getColumn(coli).setMaxWidth(6);
                }
                heatmapTable.getColumnModel().getColumn(0).setPreferredWidth(350);
                heatmapTable.getColumnModel().getColumn(0).setMaxWidth(500);
            }
        }
        

    }
    
    public class ResultsTableController implements ResultsTableListener {
        @Override
        public void resultsUpdated(ChangeEvent ce) {
            updateFilterLabel();
        }
    }

}

/*
 * ManhattanPlot + axes plots for zooming
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.annotation.AnnotationPanelWide;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.utils.ImageSelection;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class DisplayPlotControlPanel extends JComponent {
    //private View view;

    private DataSet dataSet;
    private TopDisplayPanel topDisplayPanel;
    private JComponent xAxisRegion;
    private JComponent controlsPanel;

    private JComponent plotPanel;
    private String dataSetName;

    public DisplayPlotControlPanel() {
        super();
        /*this.dataSetName = dataSetName;
        this.dataSetName = "";
        dataSet = Singleton.getDataModel().getDataSet(dataSetName);
        ViewData viewData = new ViewData(dataSet);
        Singleton.getState().addViewData(viewData);
        if(dataSet.getModels().size() > 0) {
            viewData.addModel(dataSet.getModel(0)); 
        }
        Singleton.getState().setMainView(viewData);
        //view.addModel(dataSet.getModel(1)); //todo get rid of the view using this model here
         * 
         */

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(getTopDisplayPanel(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 20;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(getPlotPanel(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 30;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 30;
        add(getControlsPanel((PlotPanel) getPlotPanel()), gbc);


        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);

    }

    protected JComponent getPlotPanel() {
        if (plotPanel == null) {
            plotPanel = new PlotPanel();
        }
        return plotPanel;
    }

    protected TopDisplayPanel getTopDisplayPanel() {
        if (topDisplayPanel == null) {
            topDisplayPanel = new TopDisplayPanel(Singleton.getState().getMainView());
        }
        return topDisplayPanel;
    }

    protected JComponent getControlsPanel(PlotPanel plotPanel) {
        if (controlsPanel == null) {
            controlsPanel = new ControlsPanel(plotPanel);
        }
        return controlsPanel;
    }

    /**
     * Captures the axes and plot and annotation panel to clipboard and
     * throws a dialog error warning on failure
     */
    public void capturePlotPanelToClipboard() {
        BufferedImage panelImage = new BufferedImage(plotPanel.getWidth(), plotPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        //Graphics2D g = (Graphics2D) panelImage.getGraphics();
        plotPanel.paint(panelImage.getGraphics());
        ImageSelection imgSel = new ImageSelection(panelImage);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    }

    /**
     * Writes out the axes and plot and annotation panel to file and
     * throws a dialog error warning on failure
     *
     * @param file
     */
    public void writePlotPanelToFile(File file) {
        BufferedImage img = new BufferedImage((int) plotPanel.getWidth(), (int) plotPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        plotPanel.paint(img.getGraphics());

        try {
            ImageIO.write(img, "jpg", file);
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

        public void currentChanged(ChangeEvent ce) {
        }

        public void mainPlotChanged(ChangeEvent ce) {
            /*
             * View mainView =
             * Singleton.getState().getMainView();
             * manhattanPlot.setView(mainView); ((AxisRegion)
             * xAxisRegion).setView(mainView); ((AxisRegion)
             * yAxisRegion).setView(mainView); ((AxisRegion) yAxisRegionRight).setView(mainView);
             */
        }

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

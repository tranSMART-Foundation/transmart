/*
 * ManhattanPlot + axes plots for zooming
 */
package com.pfizer.mrbt.genomics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;

import com.pfizer.mrbt.genomics.annotation.AnnotationPanel;
import com.pfizer.mrbt.genomics.annotation.AnnotationPanelWide;
import com.pfizer.mrbt.genomics.annotation.LeftAnnotationCorner;
import com.pfizer.mrbt.genomics.annotation.RightAnnotationCorner;
import com.pfizer.mrbt.genomics.axisregion.AxisRegion;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionX;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYLeft;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYRight;
import com.pfizer.mrbt.genomics.axisregion.LeftCornerSquare;
import com.pfizer.mrbt.genomics.axisregion.RightCornerSquare;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.View;

/**
 *
 * @author henstockpv
 */
public class PlotPanel extends JSplitPane {
    //private View view;

    private DataSet dataSet;
    private TopDisplayPanel displayPanel;
    private ManhattanPlot manhattanPlot;
    private AnnotationPanel annotationPanel;
    private AnnotationPanelWide annotationPanelWide;
    private JComponent xAxisRegion;
    private JComponent yAxisRegion;
    private JComponent yAxisRegionRight;
    private JComponent leftCornerRegion;
    private JComponent rightCornerRegion;
    private LeftAnnotationCorner leftAnnotationCorner;
    private RightAnnotationCorner rightAnnotationCorner;
    private JComponent topPane;
    private JComponent bottomPane;
    private JScrollPane lowerPaneScrollPane;
    private JSplitPane lowerSplitPane;
    private JScrollPane annotationPanelWideScrollPane;

    public PlotPanel() {
        super(JSplitPane.VERTICAL_SPLIT);
        setTopComponent(getTopPane());
        
        lowerPaneScrollPane = new JScrollPane(getBottomPane());
        lowerPaneScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        lowerPaneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        annotationPanelWideScrollPane = new JScrollPane(getAnnotationPanelWide());
        annotationPanelWideScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        annotationPanelWideScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        annotationPanelWideScrollPane.revalidate();
        setBottomComponent(annotationPanelWideScrollPane);
        
        setResizeWeight(Singleton.getUserPreferences().getSplitPaneFraction());
        
        DividerLocationController dlc = new DividerLocationController();
        this.addPropertyChangeListener(dlc);
        //setBottomComponent(lowerPaneScrollPane);

        /* removed block from startup
         * dataSet = Singleton.getDataModel().getDataSet(dataSetName);
        ViewData viewData = new ViewData(dataSet);
        Singleton.getState().addViewData(viewData);
        viewData.addModel(dataSet.getModel(0)); //todo get rid of the view using this model here
        Singleton.getState().setMainView(viewData);
        //view.addModel(dataSet.getModel(1)); //todo get rid of the view using this model here
        */
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);

    }
    
    @Override
    public int getMinimumDividerLocation() {
        return 0;
    }
    
    @Override
    public int getMaximumDividerLocation() {
        return 10000;
    }
    
    
    /**
     * Sometimes needs refreshing when resized
     */
    public void adjustAnnotationScrollPane() {
        annotationPanelWide.validate();
        annotationPanelWideScrollPane.validate();
        annotationPanelWideScrollPane.revalidate();
    }
    
    /**
     * Returns the width of the scroll pane associated with the annotationPanelWide
     * @return 
     */
    public int getAnnotationPanelWideScrollWidth() {
        if(annotationPanelWideScrollPane == null) {
            return 0;
        } else if(annotationPanelWideScrollPane.getVerticalScrollBar().isVisible()) {
            return annotationPanelWideScrollPane.getVerticalScrollBar().getWidth();
            
        } else {
            return 0;
        }
    }
    
    protected JComponent getTopPane() {
        if (topPane == null) {
            topPane = new JPanel();
            topPane.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 10;
            gbc.gridy = 12;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;


            gbc.gridx = 20;
            gbc.gridy = 10;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            topPane.add(getManhattanPlot(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 10;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            topPane.add(getYAxisRegion(), gbc); // call after getManhattanPlot call

            gbc.gridx = 20;
            gbc.gridy = 20;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            topPane.add(getXAxisRegion(), gbc);

            gbc.gridx = 30;
            gbc.gridy = 10;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            topPane.add(getSecondYAxisRegion(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 20;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            topPane.add(getLeftCornerRegion(), gbc);

            gbc.gridx = 30;
            gbc.gridy = 20;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            topPane.add(getRightCornerRegion(), gbc);
            getYAxisRegion();
            getXAxisRegion();
            getSecondYAxisRegion();
            getLeftCornerRegion();
            getRightCornerRegion();
        }
        return topPane;
    }

    protected JComponent getBottomPane() {
        if (bottomPane == null) {
            bottomPane = new JPanel();
            bottomPane.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 20;
            gbc.gridy = 30;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            bottomPane.add(getAnnotationPanel(), gbc);

            gbc.gridx = 10;
            gbc.gridy = 30;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            bottomPane.add(getLeftAnnotationCorner(), gbc);

            gbc.gridx = 30;
            gbc.gridy = 30;
            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            bottomPane.add(getRightAnnotationCorner(), gbc);
        }
        return bottomPane;
    }

    public void oldPlotPanel() {
        //super();


        /* removed block from startup
         * dataSet = Singleton.getDataModel().getDataSet(dataSetName);
         ViewData viewData = new ViewData(dataSet);
         Singleton.getState().addViewData(viewData);
         viewData.addModel(dataSet.getModel(0)); //todo get rid of the view using this model here
         Singleton.getState().setMainView(viewData);
         //view.addModel(dataSet.getModel(1)); //todo get rid of the view using this model here
         */
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 20;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(getManhattanPlot(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        add(getYAxisRegion(), gbc); // call after getManhattanPlot call

        gbc.gridx = 20;
        gbc.gridy = 20;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(getXAxisRegion(), gbc);

        gbc.gridx = 30;
        gbc.gridy = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(getSecondYAxisRegion(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 20;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(getLeftCornerRegion(), gbc);

        gbc.gridx = 30;
        gbc.gridy = 20;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(getRightCornerRegion(), gbc);

        gbc.gridx = 20;
        gbc.gridy = 30;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(getAnnotationPanel(), gbc);

        gbc.gridx = 10;
        gbc.gridy = 30;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(getLeftAnnotationCorner(), gbc);

        gbc.gridx = 30;
        gbc.gridy = 30;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(getRightAnnotationCorner(), gbc);

        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);

    }

    protected JComponent createManhattanPlot() {
        if (manhattanPlot == null) {
            manhattanPlot = new ManhattanPlot(Singleton.getState().getMainView());
        }
        return manhattanPlot;
    }

    public ManhattanPlot getManhattanPlot() {
        //return manhattanPlot;
        if (manhattanPlot == null) {
            manhattanPlot = new ManhattanPlot(Singleton.getState().getMainView());
        }
        return manhattanPlot;
    }

    /**
     * Should be called after getManhattanPlot() else things aren't quite right.
     *
     * @return
     */
    protected JComponent getYAxisRegion() {
        if (yAxisRegion == null) {
            yAxisRegion = new AxisRegionYLeft(getManhattanPlot().getView());
        }
        return yAxisRegion;
    }

    protected JComponent getSecondYAxisRegion() {
        if (yAxisRegionRight == null) {
            yAxisRegionRight = new AxisRegionYRight(getManhattanPlot().getView());
        }
        return yAxisRegionRight;
    }

    protected JComponent getXAxisRegion() {
        if (xAxisRegion == null) {
            xAxisRegion = new AxisRegionX(getManhattanPlot().getView());
        }
        return xAxisRegion;
    }

    protected TopDisplayPanel getDisplayPanel() {
        if (displayPanel == null) {
            displayPanel = new TopDisplayPanel(Singleton.getState().getMainView());
        }
        return displayPanel;
    }

    protected AnnotationPanel getAnnotationPanel() {
        if (annotationPanel == null) {
            annotationPanel = new AnnotationPanel(Singleton.getState().getMainView());
        }
        return annotationPanel;
    }

    protected AnnotationPanelWide getAnnotationPanelWide() {
        if (annotationPanelWide == null) {
            annotationPanelWide = new AnnotationPanelWide(Singleton.getState().getMainView(), PlotPanel.this);
        }
        return annotationPanelWide;
    }

    protected JComponent getLeftCornerRegion() {
        if (leftCornerRegion == null) {
            leftCornerRegion = new LeftCornerSquare(Singleton.getState().getMainView(), (AxisRegion) xAxisRegion, (AxisRegion) yAxisRegion, (AxisRegion) yAxisRegionRight);
        }
        return leftCornerRegion;
    }

    protected JComponent getRightCornerRegion() {
        if (rightCornerRegion == null) {
            rightCornerRegion = new RightCornerSquare(Singleton.getState().getMainView(), (AxisRegion) xAxisRegion, (AxisRegion) yAxisRegion, (AxisRegion) yAxisRegionRight);
        }
        return rightCornerRegion;
    }

    protected JComponent getLeftAnnotationCorner() {
        if (leftAnnotationCorner == null) {
            leftAnnotationCorner = new LeftAnnotationCorner();
            leftAnnotationCorner.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        }
        return leftAnnotationCorner;
    }

    protected JComponent getRightAnnotationCorner() {
        if (rightAnnotationCorner == null) {
            rightAnnotationCorner = new RightAnnotationCorner(lowerPaneScrollPane);
            rightAnnotationCorner.setBorder(BorderFactory.createLineBorder(Color.PINK));
        }
        return rightAnnotationCorner;
    }
    
    /**
     * If you search for a particular gene, it may not be visible in the annotation
     * panel.  This ensures that the State's current is visible in the scroll region.
     */
    public void ensureGeneAnnotationIsVisible(String geneName) {
        //String selectedGeneName = Singleton.getState().getSelectedGeneAnnotation().getGene();
        int yLoc = annotationPanelWide.getYLoc(geneName);
        annotationPanelWideScrollPane.scrollRectToVisible(new Rectangle(0, yLoc, 2, 2));
        annotationPanelWideScrollPane.getViewport().setViewPosition(new Point(0, yLoc-20));
    }

    

    public class StateController implements StateListener {
        @Override
        public void currentChanged(ChangeEvent ce) {        }
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            View mainView = Singleton.getState().getMainView();
            annotationPanel.processMainPlotChanged();
            annotationPanel.setPreferredSize(annotationPanel.getWidth(), annotationPanel.getPreferredHeight());
            annotationPanel.setMaximumSize(new Dimension(annotationPanel.getWidth(), annotationPanel.getPreferredHeight()-AxisRegionX.PREFERRED_HEIGHT/2));
            annotationPanel.setMinimumSize(new Dimension(annotationPanel.getWidth(), annotationPanel.getPreferredHeight()));
            //annotationPanel.repaint();
            
            annotationPanelWide.processMainPlotChanged();
            annotationPanelWideScrollPane.revalidate();
            int width = lowerPaneScrollPane.getVerticalScrollBar().getWidth();
            System.out.println("Main Data Changed in plotPanel width " + width);
            
            revalidate();
            manhattanPlot.setView(mainView);
            annotationPanelWide.revalidate();
            //manhattanPlot.adjustImage();
            ((AxisRegion) xAxisRegion).setView(mainView);
            ((AxisRegion) yAxisRegion).setView(mainView);
            ((AxisRegion) yAxisRegionRight).setView(mainView);
        }
        @Override
        public void thumbnailsChanged(ChangeEvent ce) {        }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) {        }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) {        }

        @Override
        public void averagingWindowChanged(ChangeEvent ce) {        }

        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) {        }
        @Override
        public void heatmapChanged(ChangeEvent ce) { }
    }
    
    /**
     * Class intended to capture the changes to the splitPane ratio and update
     * the UserPreferences
     */
    public class DividerLocationController implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent changeEvent) {
            String propertyName = changeEvent.getPropertyName();
            if(propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                int loc = PlotPanel.this.getDividerLocation();
                int height = PlotPanel.this.getHeight();
                double ratio = (loc*1.0) / height;
                //System.out.println("Locations " + loc + "\t" + height + "\tratio " + ratio);
                Singleton.getUserPreferences().setSplitPaneFraction(ratio);
            }
        }
    }
}

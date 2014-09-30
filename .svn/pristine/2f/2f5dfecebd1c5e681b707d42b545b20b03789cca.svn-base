package com.pfizer.mrbt.genomics;


import com.pfizer.mrbt.genomics.legend.LegendPanel;
import com.pfizer.mrbt.genomics.utils.JpgFilter;
import com.pfizer.mrbt.genomics.utils.SuffixFileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileFilter;

/**
 * <p>Title: Menubar.java</p>
 *
 * <p>Description: Processes the menu bar.</p>
 *
 * <p>Copyright: Copyright (c) 2012, 2013</p>
 *
 * <p>Company: Pfizer</p>
 *
 * @author Peter V. Henstock
 * @VERSION 1.0
 */
public class MenuBar extends JMenuBar {

    protected JMenu fileMenu;
    private JMenu myHelpMenu;
    private JMenu captureMenu;
    private JMenu preferencesMenu;
    protected AbstractAction fileLoadRawDataAction, fileSaveConfigAction, fileExitAction;
    protected AbstractAction fileLoadConfigAction, fileExportRawDataAction;
    protected AbstractAction fileOpenMultiPlateTableAction,
            fileOpenMultiPlateColumnAction;
    private AbstractAction backgroundColorAction, axisTitleColorAction;
    private AbstractAction axisBackgroundColorAction, axisForegroundColorAction;
    private AbstractAction pointColorAction, selectionColorAction, currentColorAction;
    private AbstractAction meanRatioColorAction, ratioBoundsColorAction, lsColorAction;
    private AbstractAction linearFitColorAction, trueFitColorAction;
    private AbstractAction selectionBandColorAction;
    private AbstractAction frameColorAction, frameTextColorAction;
    private AbstractAction fileExportAction;
    private AbstractAction fileSaveAction, fileLoadAction;
    private AbstractAction foundSnpColorAction;
    private AbstractAction annotationColorAction, annotationTextColorAction;
    private AbstractAction currentAnnotationColorAction, selectedAnnotationColorAction;
    private AbstractAction closestAnnotationColorAction, interiorAnnotationColorAction;
    private AbstractAction recombinationColorAction;
    private AbstractAction thumbnailColorAction, thumbnailTextColorAction, thumbnailPointColorAction;
    private AbstractAction thumbnailSelectionBandColorAction, thumbnailSelectionColorAction;
    private AbstractAction thumbnailHorizontalBandColorAction;
    private AbstractAction axisDragRangeColorAction;
    private AbstractAction leftMaxVerticalAxisAction;
    private AbstractAction rightMaxVerticalAxisAction;
    private AbstractAction buildLegendAction;
    private AbstractAction legendAction;
    private JComponent mainPanel;
    private AbstractAction capturePlotAction;
    private AbstractAction savePlotAction;
    private AbstractAction captureAllAction;
    public final static int BACKGROUND_COLOR = 1001;
    public final static int POINT_COLOR = 1002;
    public final static int SELECTION_COLOR = 1003;
    public final static int CURRENT_COLOR = 1004;
    public final static int FRAME_COLOR = 1005;
    public final static int FRAME_TEXT_COLOR = 1006;
    public final static int RECOMBINATION_COLOR = 1007;
    public final static int SELECTION_BAND_COLOR = 1008;
    public final static int FOUND_SNP_COLOR = 1009;
    public final static int AXIS_DRAG_RANGE_COLOR = 1010;
    public final static int ANNOTATION_COLOR = 1101;
    public final static int ANNOTATION_TEXT_COLOR = 1102;
    public final static int CURRENT_ANNOTATION_COLOR = 1103;
    public final static int SELECTED_ANNOTATION_COLOR = 1104;
    public final static int CLOSEST_ANNOTATION_COLOR = 1105;
    public final static int INTERIOR_ANNOTATION_COLOR = 1106;
    public final static int THUMBNAIL_COLOR = 1201;
    public final static int THUMBNAIL_TEXT_COLOR = 1202;
    public final static int THUMBNAIL_POINT_COLOR = 1203;
    public final static int THUMBNAIL_SELECTION_BAND_COLOR = 1204;
    public final static int THUMBNAIL_SELECTION_COLOR = 1205;
    public final static int THUMBNAIL_HORIZONTAL_BAND_COLOR = 1206;
    private final int COLOR_ICON_SIZE = 15;
    private final float LARGE_NUMBER = 1E15f;
    private String filename;

    public MenuBar(JComponent mainPanel) {
        super();
        this.mainPanel = mainPanel;
        fileMenu = getFileMenu();
        add(fileMenu);

        captureMenu = getCaptureMenu();
        add(captureMenu);

        preferencesMenu = getPreferencesMenu();
        add(preferencesMenu);

        myHelpMenu = getMyHelpMenu();
        add(myHelpMenu);

        initializeMenuBarWithoutData();
    }

    /**
     * Creates and returns the file menu commands.
     *
     * @return JMenu
     */
    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu("File");
            fileMenu.setMnemonic(KeyEvent.VK_F);
            /*fileMenu.getAccessibleContext().setAccessibleDescription(
             "Loads data file.");

             KeyStroke loadKey = KeyStroke.getKeyStroke(KeyEvent.VK_L,
             Event.CTRL_MASK);
             fileLoadAction = new FileLoadAction("Load Data File", loadKey,
             new Integer(KeyEvent.VK_S),
             "Loads tab-delimited text file.");
             fileMenu.add(new JMenuItem(fileLoadAction));*/
            
            KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Event.CTRL_MASK);
            fileSaveAction = new FileSaveAction("Save Data", saveKey,
                    new Integer(KeyEvent.VK_S),
                    "Exports the loaded data to a file");
            fileMenu.add(new JMenuItem(fileSaveAction));


            KeyStroke exitKey = KeyStroke.getKeyStroke(KeyEvent.VK_X,
                    Event.CTRL_MASK);
            fileExitAction = new FileExitAction("Exit", exitKey,
                    new Integer(KeyEvent.VK_X),
                    "Exit application");
            fileMenu.add(new JMenuItem(fileExitAction));



        }
        return fileMenu;
    }

    /**
     * Returns a support help menu. The name was chosen since helpmenu didn't
     * work.
     *
     * @return JMenu
     */
    private JMenu getMyHelpMenu() {
        if (myHelpMenu == null) {
            myHelpMenu = new JMenu("Help");
            myHelpMenu.getAccessibleContext().setAccessibleDescription(
                    "About the program and help.");

            myHelpMenu.add(
                    new UserGuideAction("/help/quickStart.html",
                    "Quick Start Guide",
                    "Introduction on how to quickly start using "
                    + " the GWAS Visualization program."));

            myHelpMenu.add(
                    new UserGuideAction("/help/faq.html",
                    "Frequently Asked Questions",
                    "The how-to guide"));

            /*myHelpMenu.add(
             new HowToAction("howto.html",
             "How-To Guide",
             "Instructions for specific functions of Pacomo "));*/

            myHelpMenu.addSeparator();

            myHelpMenu.setMnemonic(KeyEvent.VK_H);
            myHelpMenu.add(new AboutAction());
        }
        return myHelpMenu;
    }

    public class FileExitAction extends AbstractAction {

        /**
         * Action item for the file exit that processes exit events after
         * verifying that you haven't left any unsaved modifications.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public FileExitAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        public void actionPerformed(ActionEvent e) {
            boolean doNotSave = false;
            int reply = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(mainPanel),
                    "Are you sure you want to exit?", "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                //Singleton.getUserPreferences().saveUserPreferences();
                System.exit(0);
            }
        }
    }

    public class FileExportAction extends AbstractAction {

        /**
         * Writes out all the visible data from the Results table and all the
         * selected rows from the Unmatched Reference Sequences table verifying
         * that you haven't left any unsaved modifications.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public FileExportAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        public void actionPerformed(ActionEvent e) {
            /*try {
             FileFetcher fileFetcher = new FileFetcher();
             File file = fileFetcher.getFilenameTxt();
             if (file != null) {
             Singleton.getDataModel().exportResults(file);
             }
             } catch (FileNotFoundException fnfe) {
             } catch (IOException ioe) {
             }*/
        }
    }

    public class FileSaveAction extends AbstractAction {

        /**
         * Serial dump of the data model
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public FileSaveAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileSaveDialog fileSaveDialog = new FileSaveDialog();
            java.net.URL imgURL = this.getClass().getResource("/images/guava_48.jpg");
            int optionPaneReturn = JOptionPane.showConfirmDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                    fileSaveDialog, "Save Data to File",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    new ImageIcon(imgURL));
            if (optionPaneReturn == JOptionPane.OK_OPTION) {
                //todo
                /*try {
                    String filename = fileSaveDialog.getFilename();
                    DataExporter dataExporter = new DataExporter();
                    dataExporter.exportAllData(filename);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                            "Y level should be numeric: " + fileSaveDialog.getLogPValue(),
                            "Invalid y-level",
                            JOptionPane.ERROR_MESSAGE);
                }*/
            }
        }
    }
    
    public class FileLoadAction extends AbstractAction {

        /**
         * Serial restore of the DataModel
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public FileLoadAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        public void actionPerformed(ActionEvent e) {
            File file = null;
            //file = getUserSelectedFile();
            //Singleton.getDataModel().loadDataFile(file);

        }
        /*public File getUserSelectedFile() {
         JFileChooser fc = new JFileChooser();
         TxtFilter txtFilter = new TxtFilter();
         fc.addChoosableFileFilter((FileFilter) txtFilter);

         String path = Singleton.getUserPreferences().getFilePath();
         if (path != null && path.trim().length() > 0) {
         fc.setCurrentDirectory(new File(path));
         }
         int returnVal = fc.showOpenDialog(SwingUtilities.getWindowAncestor(Singleton.getMainPanel().getTopLevelAncestor()));

         if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         String filename = file.getAbsolutePath();
         if (!filename.toLowerCase().endsWith(txtFilter.getSuffix())) {
         file = new File(filename + txtFilter.getSuffix());
         }
         Singleton.getUserPreferences().setFilePath(file.getParent());
         return file;
         } else {
         System.out.println("Export command cancelled by user.\n");
         return null;
         }
         }*/
    }

    /**
     * Process the about button that pops up a dialog box.
     */
    public class AboutAction extends AbstractAction {

        public AboutAction() {
            super("About");
            putValue(SHORT_DESCRIPTION, "About the GWAS Visualization");
        }

        /**
         * Displays the about dialog.
         *
         * @param e ActionEvent
         */
        public void actionPerformed(ActionEvent e) {
            //URL imgURL = ClassLoader.getSystemResource("grid.jpg");
            java.net.URL imgURL = this.getClass().getResource("/images/guava_48.jpg");

            ImageIcon myIcon = new ImageIcon(imgURL);
            //ImageIcon myIcon = null;
            JOptionPane.showMessageDialog(mainPanel.getTopLevelAncestor(),
                    getAboutContentPane(),
                    "About the GWAVA Program",
                    JOptionPane.PLAIN_MESSAGE, myIcon);
        }
    }

    /**
     * Initializes the menu bar to its default setting when nothing is loaded.
     */
    protected void initializeMenuBarWithoutData() {
        /*analysisMenu.setEnabled(false);
         colorMenu.setEnabled(false);
         fileSaveAction.setEnabled(false);
         toolsMenu.setEnabled(false);*/
    }

    /**
     * Updates the menubar when data is loaded so that it is funcitonal.
     */
    protected void enableMenuBarWithData() {
        /*analysisMenu.setEnabled(true);
         colorMenu.setEnabled(true);
         fileSaveAction.setEnabled(true);
         toolsMenu.setEnabled(true);*/
    }

    /**
     * Returns the about pane that is part of the about dialog box.
     *
     * @return JComponent
     */
    protected JComponent getAboutContentPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        //pane.add(getHorizCenteredBox("Phage Display Alignment"));
        pane.add(Box.createRigidArea(new Dimension(0, 10)));

        String acronymStylized =
                "<html>"
                + "<font color=\"#FF0000\" size=\"4\">G</font>enome-"
                + "<font color=\"#FF0000\" size=\"4\">W</font>ide "
                + "<font color=\"#FF0000\" size=\"4\">A</font>ssociation "
                + "<font color=\"#FF0000\" size=\"4\">V</font>isual "
                + "<font color=\"#FF0000\" size=\"4\">A</font>nalyzer"
                + "</html>";
        pane.add(getHorizCenteredBox(acronymStylized));

        pane.add(Box.createRigidArea(new Dimension(0, 15)));

        Box txtRegion = new Box(BoxLayout.LINE_AXIS);
        String description =
                "GWAVA is the visualization of genome-wide association studies\n"
                + " through multiple Manhattan plots and extended\n"
                + " user interaction.\n";

        JTextArea txt = new JTextArea(description);
        txt.setBackground(getBackground());
        txt.setEditable(false);
        txtRegion.add(Box.createHorizontalGlue());
        txtRegion.add(txt);
        txtRegion.add(Box.createHorizontalGlue());
        pane.add(txtRegion);

        pane.add(Box.createRigidArea(new Dimension(0, 10)));

        pane.add(getHorizCenteredBox("Version " + Driver.VERSION));
        pane.add(Box.createRigidArea(new Dimension(0, 5)));
        pane.add(getHorizCenteredBox("Peter V. Henstock"));
        pane.add(Box.createRigidArea(new Dimension(0, 5)));
        pane.add(getHorizCenteredBox("In collaboration with the GWAS Analysis Team"));
        pane.add(getHorizCenteredBox("Jay Bergeron"));
        pane.add(getHorizCenteredBox("Ami Khandeshi"));
        pane.add(getHorizCenteredBox("Haiyan Zhang"));
        pane.add(getHorizCenteredBox("Alexander Zak"));
        pane.add(getHorizCenteredBox("Angela Gaudette"));
        pane.add(getHorizCenteredBox("Anna Silberberg"));
        pane.add(getHorizCenteredBox("David Klatte"));

        pane.add(getHorizCenteredBox("\u00A9 Copyright 2012, 2013  Pfizer Inc."));
        pane.add(Box.createRigidArea(new Dimension(0, 10)));
        pane.setBorder(BorderFactory.createRaisedBevelBorder());
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));

        return pane;
    }

    /**
     * This is a stupid box that was used to correctly center some of the text
     * in the about box. couldn't make it work without the box.
     *
     * @param text String
     * @return JComponent
     */
    public JComponent getHorizCenteredBox(String text) {
        Box region = new Box(BoxLayout.LINE_AXIS);
        region.add(Box.createHorizontalGlue());
        region.add(new JLabel(text));
        region.add(Box.createHorizontalGlue());
        return region;
    }

    private JMenu getCaptureMenu() {
        if (captureMenu == null) {


            captureMenu = new JMenu("Capture");
            captureMenu.getAccessibleContext().setAccessibleDescription(
                    "Copy plot to clipboard.");

            KeyStroke captureBAKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                    Event.CTRL_MASK);
            capturePlotAction = new CapturePlotAction("Capture main plot to clipboard", captureBAKey,
                    new Integer(KeyEvent.VK_C),
                    "Captures the plot and gene annotation area to the clipboard");
            captureMenu.add(new JMenuItem(capturePlotAction));

            KeyStroke captureCorrKey = KeyStroke.getKeyStroke(KeyEvent.VK_J,
                    Event.CTRL_MASK);
            savePlotAction = new SavePlotAction("Save plot to file", captureCorrKey,
                    new Integer(KeyEvent.VK_J),
                    "Captures the plot and gene annotation to a jpg file.");
            captureMenu.add(new JMenuItem(savePlotAction));

            KeyStroke legendKey = KeyStroke.getKeyStroke(KeyEvent.VK_L,
                    Event.CTRL_MASK);
            legendAction = new LegendAction("Create/Save Legend", legendKey,
                    new Integer(KeyEvent.VK_L),
                    "Creates a legend for the given plot that can be saved or captured to the clipboard.");
            captureMenu.add(new JMenuItem(legendAction));


        }
        return captureMenu;
    }

    /**
     *
     * <p>Title: HTS Analysis: UserGuideAction</p> <p>Description: </p>
     * <p>Copyright: Copyright (c) 2005</p> <p>Company: Pfizer</p>
     *
     * @author Patricia Greniger modified by Peter V. Henstock
     * @VERSION 1.0
     */
    public class UserGuideAction extends AbstractAction implements Runnable {

        JEditorPane ep;
        JScrollPane sp;
        java.net.URL guide;

        /**
         * Processes the help screens to pop up and stay launched.
         *
         * @param filename String
         * @param menuName String
         * @param toolTip String
         */
        public UserGuideAction(String filename, String menuName, String toolTip) {
            super(menuName);
            putValue(SHORT_DESCRIPTION, toolTip);
            if (menuName.equals("Quick Start Guide")) {
                putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,
                        InputEvent.CTRL_MASK));
            }
            try {
                ep = new JEditorPane();
                ep.setEditable(false);
                guide = this.getClass().getResource(filename);
                //guide = new URL("http://clinux22.pfizer.com/webstart/TAN/quickStartGuide.htm");
                // todo change to a resource
                ep.setPage(guide);
                sp = new JScrollPane(ep);
                sp.setPreferredSize(new Dimension(800, 600));
                sp.setMinimumSize(new Dimension(150, 100));
            } catch (Exception e) {
                System.out.println("Failed to load help file: <" + filename + "> " + e);
            }
        }

        /**
         * Facilitates a separate thread for the user guide to run independently
         * of the program.
         */
        public void run() {
            JFrame f = new JFrame("User Guide for the GWAVA system");
            java.net.URL imgURL = this.getClass().getResource("/images/guava_16.jpg");
            f.setIconImage(new ImageIcon(imgURL).getImage());

            f.getContentPane().add(sp);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.pack();
            f.setVisible(true);
        }

        /**
         * Processes error message when cannot find user guide.
         *
         * @param e ActionEvent
         */
        public void actionPerformed(ActionEvent e) {
            if (guide == null) {
                String error = "Unable to find user guide.";
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(mainPanel),
                        error, "User Guide",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                SwingUtilities.invokeLater(this);
            }
        }
    }

    /**
     */
    public class HowToAction extends AbstractAction implements Runnable {

        JEditorPane ep;
        JScrollPane sp;
        java.net.URL guide;

        /**
         * Processes the help screens to pop up and stay launched.
         *
         * @param filename String
         * @param menuName String
         * @param toolTip String
         */
        public HowToAction(String filename, String menuName, String toolTip) {
            super(menuName);
            putValue(SHORT_DESCRIPTION, toolTip);
            try {
                ep = new JEditorPane();
                ep.setEditable(false);
                guide = this.getClass().getResource("/programImages/" + filename);
                //guide = new URL("http://clinux22.pfizer.com/webstart/TAN/quickStartGuide.htm");
                // todo change to a resource
                ep.setPage(guide);
                sp = new JScrollPane(ep);
                sp.setPreferredSize(new Dimension(800, 600));
                sp.setMinimumSize(new Dimension(150, 100));
            } catch (Exception e) {
                System.out.println("Failed to load help file: <" + filename + "> " + e);
            }
        }

        /**
         * Facilitates a separate thread for the user guide to run independently
         * of the program.
         */
        public void run() {
            JFrame f = new JFrame("How To Guide for GWAVA");
            java.net.URL imgURL = this.getClass().getResource("/programImages/gwava_48.jpg");
            f.setIconImage(new ImageIcon(imgURL).getImage());
            f.getContentPane().add(sp);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.pack();
            f.setVisible(true);
        }

        /**
         * Processes error message when cannot find user guide.
         *
         * @param e ActionEvent
         */
        public void actionPerformed(ActionEvent e) {
            if (guide == null) {
                String error = "Unable to find the how-to guide.";
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(MenuBar.this.mainPanel),
                        error, "How-Tor Guide",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                SwingUtilities.invokeLater(this);
            }
        }
    }

    /**
     * Sets the specified userPrefColorRef with the given name to the new color.
     * The name is passed in just for debugging purposes
     *
     * @param userPrefColorRef int
     * @param userPrefColorName String
     * @param currentColor Color
     */
    /*private void userSetColor(int userPrefColorRef, String userPrefColorName,
     Color currentColor) {
     String requestString = "Select desired " + userPrefColorName;
     Color returnedColor = JColorChooser.showDialog(null,
     requestString,
     currentColor);
     if (returnedColor != null) {
     userPref.setColor(userPrefColorRef, returnedColor);
     }
     }*/
    /**
     * Creates and returns the preferences menu
     *
     * @return JMenu
     */
    private JMenu getPreferencesMenu() {
        String toolTipStr;
        if (preferencesMenu == null) {
            preferencesMenu = new JMenu("Preferences");
            preferencesMenu.setMnemonic(KeyEvent.VK_P);
            preferencesMenu.getAccessibleContext().setAccessibleDescription(
                    "Sets analysis graph options.");

            backgroundColorAction = new PreferencesAction("Background Plot Color",
                    null,
                    "Background plot color of all plots",
                    BACKGROUND_COLOR);
            preferencesMenu.add(new JMenuItem(backgroundColorAction));

            pointColorAction = new PreferencesAction("Single Model/Gene Color", null,
                    "Color of the circles for data points where only one model and one gene present.",
                    POINT_COLOR);
            preferencesMenu.add(new JMenuItem(pointColorAction));

            selectionBandColorAction = new PreferencesAction("Selection Band", null,
                    "Band in main display when a gene has been selected",
                    SELECTION_BAND_COLOR);
            preferencesMenu.add(new JMenuItem(selectionBandColorAction));


            currentColorAction = new PreferencesAction("Mouse-Over Color", null,
                    "Color of mouse-over data points in the plots identifying the data point",
                    CURRENT_COLOR);
            preferencesMenu.add(new JMenuItem(currentColorAction));


            frameColorAction = new PreferencesAction("Frame background color", null,
                    "Background color behind the axes surrrounding the main plot",
                    FRAME_COLOR);
            preferencesMenu.add(new JMenuItem(frameColorAction));

            frameTextColorAction = new PreferencesAction("Title/axis text color", null,
                    "Text color of the main plot title and axis color",
                    FRAME_TEXT_COLOR);
            preferencesMenu.add(new JMenuItem(frameTextColorAction));

            recombinationColorAction = new PreferencesAction("Recombination rate color", null,
                    "Color of the recombinationr ate line on the main plot",
                    RECOMBINATION_COLOR);
            preferencesMenu.add(new JMenuItem(recombinationColorAction));

            foundSnpColorAction = new PreferencesAction("Found SNP color", null,
                    "Color of circle around SNP found from search",
                    FOUND_SNP_COLOR);
            preferencesMenu.add(new JMenuItem(foundSnpColorAction));

            axisDragRangeColorAction = new PreferencesAction("Axis drag range color", null,
                    "Color of the drag range when select a region on the x or y axes",
                    AXIS_DRAG_RANGE_COLOR);
            preferencesMenu.add(new JMenuItem(axisDragRangeColorAction));


            preferencesMenu.addSeparator();

            annotationColorAction = new PreferencesAction("Annotation background color", null,
                    "Color of the background of the annotation window",
                    ANNOTATION_COLOR);
            preferencesMenu.add(new JMenuItem(annotationColorAction));


            annotationTextColorAction = new PreferencesAction("Annotation text color", null,
                    "Color of the text of the anntoation",
                    ANNOTATION_TEXT_COLOR);
            preferencesMenu.add(new JMenuItem(annotationTextColorAction));


            currentAnnotationColorAction = new PreferencesAction("Mouse-over annotation text color", null,
                    "Closest gene when mouse-over annotation",
                    CURRENT_ANNOTATION_COLOR);
            preferencesMenu.add(new JMenuItem(currentAnnotationColorAction));


            selectedAnnotationColorAction = new PreferencesAction("Selected annotation text color", null,
                    "Color of annotation when click or search finds given gene",
                    SELECTED_ANNOTATION_COLOR);
            preferencesMenu.add(new JMenuItem(selectedAnnotationColorAction));


            closestAnnotationColorAction = new PreferencesAction("Closest nearby gene annotation color", null,
                    "Color of annotation when selected SNP is near but outside this gene",
                    CLOSEST_ANNOTATION_COLOR);
            preferencesMenu.add(new JMenuItem(closestAnnotationColorAction));


            interiorAnnotationColorAction = new PreferencesAction("Interior gene annotation Color", null,
                    "Color of annotation when selected SNP is inside this gene",
                    INTERIOR_ANNOTATION_COLOR);
            preferencesMenu.add(new JMenuItem(interiorAnnotationColorAction));


            preferencesMenu.addSeparator();

            thumbnailColorAction = new PreferencesAction("Thumbnail background color", null,
                    "Color behind the dots on the thumbnails",
                    THUMBNAIL_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailColorAction));


            thumbnailTextColorAction = new PreferencesAction("Thumbnail text/axis color", null,
                    "Ticks, labels, and text of the thumbnail views",
                    THUMBNAIL_TEXT_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailTextColorAction));


            thumbnailPointColorAction = new PreferencesAction("Thumbnail point color", null,
                    "Color of the dots on the thumbnails",
                    THUMBNAIL_POINT_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailPointColorAction));


            thumbnailSelectionBandColorAction = new PreferencesAction("Thumbnail selection band color", null,
                    "Band corresponding to a selected gene that appears in thumbnails",
                    THUMBNAIL_SELECTION_BAND_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailSelectionBandColorAction));


            thumbnailSelectionColorAction = new PreferencesAction("Thumbnail selection color", null,
                    "Clicking a thumbnail view colors it and renders it in the main larger panel",
                    THUMBNAIL_SELECTION_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailSelectionColorAction));

            thumbnailHorizontalBandColorAction = new PreferencesAction("Thumbnail horizontal band color", null,
                    "Indication for the optional alternating log P-value range bands",
                    THUMBNAIL_HORIZONTAL_BAND_COLOR);
            preferencesMenu.add(new JMenuItem(thumbnailHorizontalBandColorAction));
            
            preferencesMenu.addSeparator();

            leftMaxVerticalAxisAction = new LeftMaxVerticalAxisAction("Ideal Max -logPVal", null,
                    "Sets default left -logPVal axis to [0 Ideal] when all values < Ideal");
            preferencesMenu.add(new JMenuItem(leftMaxVerticalAxisAction));
            
            rightMaxVerticalAxisAction = new RightMaxVerticalAxisAction("Ideal Max Recombination Rate", null,
                    "Sets default right Recombination Rate axis to [0 Ideal] when all values < Ideal");
            preferencesMenu.add(new JMenuItem(rightMaxVerticalAxisAction));
            
            

        }
        return preferencesMenu;
    }

    /**
     * Creates and returns the little color box that apears on the menu item so
     * that you can easily show what colors are currently selected.
     *
     * @param prefRefIndex int
     * @return ImageIcon
     */
    public ImageIcon getColorIcon(Color color) {
        Image colorImg = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) colorImg.getGraphics();
        g2.setColor(color);
        g2.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);
        ImageIcon ic = new ImageIcon(colorImg);
        return ic;
    }

    public class PreferencesAction extends AbstractAction {

        int prefIndex;
        String prefName;

        /**
         * Processes the preferences menu that set up all the colors. It uses
         * the prefName for the menu name, keystroke as the accelerator and
         * stores the index to indicate which item was actually invoked.
         *
         * @param prefName String
         * @param keystroke KeyStroke
         * @param tooltip String
         * @param index int
         */
        public PreferencesAction(String prefName, KeyStroke keystroke,
                String tooltip, int index) {
            super(prefName);
            prefIndex = index;
            this.prefName = prefName;
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }

            switch (prefIndex) {
                case BACKGROUND_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getBackgroundColor()));
                    break;
                case POINT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getPointColor()));
                    break;
                case SELECTION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getSelectionColor()));
                    break;
                case CURRENT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getCurrentColor()));
                    break;
                case FRAME_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getFrameColor()));
                    break;
                case FRAME_TEXT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getFrameTextColor()));
                    break;
                case RECOMBINATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getRecombinationColor()));
                    break;
                case SELECTION_BAND_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getSelectionBandColor()));
                    break;
                case FOUND_SNP_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getFoundSnpColor()));
                    break;
                case AXIS_DRAG_RANGE_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getAxisDragRangeColor()));
                    break;


                case ANNOTATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getAnnotationColor()));
                    break;
                case ANNOTATION_TEXT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getAnnotationTextColor()));
                    break;
                case CURRENT_ANNOTATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getCurrentAnnotationColor()));
                    break;
                case SELECTED_ANNOTATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getSelectedAnnotationColor()));
                    break;
                case CLOSEST_ANNOTATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getClosestAnnotationColor()));
                    break;
                case INTERIOR_ANNOTATION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getInteriorAnnotationColor()));
                    break;


                case THUMBNAIL_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailColor()));
                    break;
                case THUMBNAIL_TEXT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailTextColor()));
                    break;
                case THUMBNAIL_POINT_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailPointColor()));
                    break;
                case THUMBNAIL_SELECTION_BAND_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailSelectionBandColor()));
                    break;
                case THUMBNAIL_SELECTION_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailSelectionColor()));
                    break;
                case THUMBNAIL_HORIZONTAL_BAND_COLOR:
                    putValue(SMALL_ICON, getColorIcon(Singleton.getUserPreferences().getThumbnailHorizontalBandColor()));
                    break;
                default:
                    System.out.println("Unknown color ref selected in menuBar");
            }
        }

        /**
         * Processes color change requests by setting the new color in user
         * preferences. It also updates the color on the menubar.
         *
         * @param e ActionEvent
         */
        public void actionPerformed(ActionEvent e) {
            Color initialColor;
            Color resultColor;
            switch (prefIndex) {
                case BACKGROUND_COLOR:
                    initialColor = Singleton.getUserPreferences().getBackgroundColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setBackgroundColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getBackgroundColor();
                    }
                    backgroundColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case POINT_COLOR:
                    initialColor = Singleton.getUserPreferences().getPointColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setPointColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getPointColor();
                    }
                    pointColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case SELECTION_COLOR:
                    initialColor = Singleton.getUserPreferences().getSelectionColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setSelectionColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getSelectionColor();
                    }
                    selectionColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case CURRENT_COLOR:
                    initialColor = Singleton.getUserPreferences().getCurrentColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setCurrentColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getCurrentColor();
                    }
                    currentColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case FRAME_COLOR:
                    initialColor = Singleton.getUserPreferences().getFrameColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setFrameColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getFrameColor();
                    }
                    frameColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case FRAME_TEXT_COLOR:
                    initialColor = Singleton.getUserPreferences().getFrameTextColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setFrameTextColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getFrameTextColor();
                    }
                    frameTextColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case RECOMBINATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getRecombinationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setRecombinationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getRecombinationColor();
                    }
                    recombinationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case SELECTION_BAND_COLOR:
                    initialColor = Singleton.getUserPreferences().getSelectionBandColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setSelectionBandColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getSelectionBandColor();
                    }
                    selectionBandColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case FOUND_SNP_COLOR:
                    initialColor = Singleton.getUserPreferences().getFoundSnpColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setFoundSnpColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getFoundSnpColor();
                    }
                    foundSnpColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case AXIS_DRAG_RANGE_COLOR:
                    initialColor = Singleton.getUserPreferences().getAxisDragRangeColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setAxisDragRangeColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getAxisDragRangeColor();
                    }
                    axisDragRangeColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case ANNOTATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getAnnotationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setAnnotationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getAnnotationColor();
                    }
                    annotationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case ANNOTATION_TEXT_COLOR:
                    initialColor = Singleton.getUserPreferences().getAnnotationTextColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setAnnotationTextColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getAnnotationTextColor();
                    }
                    annotationTextColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case CURRENT_ANNOTATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getCurrentAnnotationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setCurrentAnnotationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getCurrentAnnotationColor();
                    }
                    currentAnnotationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case SELECTED_ANNOTATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getSelectedAnnotationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setSelectedAnnotationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getSelectedAnnotationColor();
                    }
                    selectedAnnotationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case CLOSEST_ANNOTATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getClosestAnnotationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setClosestAnnotationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getClosestAnnotationColor();
                    }
                    closestAnnotationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case INTERIOR_ANNOTATION_COLOR:
                    initialColor = Singleton.getUserPreferences().getInteriorAnnotationColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setInteriorAnnotationColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getInteriorAnnotationColor();
                    }
                    interiorAnnotationColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailColor();
                    }
                    thumbnailColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_TEXT_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailTextColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailTextColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailTextColor();
                    }
                    thumbnailTextColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_POINT_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailPointColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailPointColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailPointColor();
                    }
                    thumbnailPointColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_SELECTION_BAND_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailSelectionBandColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailSelectionBandColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailSelectionBandColor();
                    }
                    thumbnailSelectionBandColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_SELECTION_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailSelectionColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailSelectionColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailSelectionColor();
                    }
                    thumbnailSelectionColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
                case THUMBNAIL_HORIZONTAL_BAND_COLOR:
                    initialColor = Singleton.getUserPreferences().getThumbnailHorizontalBandColor();
                    resultColor = requestColor(initialColor, prefName);
                    if (resultColor != null) {
                        Singleton.getUserPreferences().setThumbnailHorizontalBandColor(resultColor);
                    } else {
                        resultColor = Singleton.getUserPreferences().getThumbnailHorizontalBandColor();
                    }
                    thumbnailHorizontalBandColorAction.putValue(SMALL_ICON, getColorIcon(resultColor));
                    break;
            }
        }
    }

    public class GapPenaltyAction extends AbstractAction {

        /**
         * Preferences menu changes the gap and extend penalties thru dialog
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public GapPenaltyAction(String itemName, KeyStroke keystroke,
                String tooltip, int index) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    /**
     * Sets the specified userPrefColorRef with the given name to the new color.
     * The name is passed in just for debugging purposes
     *
     * @param userPrefColorRef int
     * @param userPrefColorName String
     * @param currentColor Color
     */
    private Color requestColor(Color currentColor, String userPrefColorName) {
        String requestString = "Select desired " + userPrefColorName;
        Color returnedColor = JColorChooser.showDialog(null,
                requestString,
                currentColor);
        return returnedColor;
    }

    public class CapturePlotAction extends AbstractAction {

        /**
         * Action item for the copying the bland altman plot to the clipboard.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public CapturePlotAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ((MainPanel) MenuBar.this.mainPanel).capturePlotPanelToClipboard();
        }
    }

    public class LeftMaxVerticalAxisAction extends AbstractAction {

        /**
         * Action item for the copying the bland altman plot to the clipboard.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public LeftMaxVerticalAxisAction(String itemName, KeyStroke keystroke,
                String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            /*if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }*/
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            /*JFrame frame = new JFrame("Legend Setup");
             LegendPanel legendPanel = new LegendPanel();
             frame.setContentPane(legendPanel);
             try {
             System.out.println("Class path" + System.getProperty("java.class.path"));
             java.net.URL imgURL = this.getClass().getResource("/images/guava_16.jpg");
             frame.setIconImage(new ImageIcon(imgURL).getImage());
             } catch (NullPointerException npe) {
             System.out.println("Failed to load in the icon.");
             }

             frame.pack();
             frame.setVisible(true);*/
            float prefValue = Singleton.getUserPreferences().getMinTopNegLogPvalAxis();
            IdealYAxisPane idealYAxisPane = new IdealYAxisPane(prefValue);
            int optionPaneReturn = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                    idealYAxisPane, "Preferred Y-Axis Limit", JOptionPane.OK_CANCEL_OPTION);
            if (optionPaneReturn == JOptionPane.OK_OPTION) {
                System.out.println("OK specified");
                boolean isValid = true;
                float idealYValue = 0f;
                try {
                    idealYValue = Float.parseFloat(idealYAxisPane.getValueTextField());
                } catch (NumberFormatException nfe) {
                    isValid = false;
                }
                if (!isValid) {
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                            "Number must be a positive value.",
                            "Input value error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    Singleton.getUserPreferences().setMinTopNegLogPvalAxis(idealYValue);
                    System.out.println("Returned from editor");
                }
            }

        }
    }

    public class RightMaxVerticalAxisAction extends AbstractAction {

        /**
         * Action item for the copying the bland altman plot to the clipboard.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public RightMaxVerticalAxisAction(String itemName, KeyStroke keystroke,
                                          String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            float prefValue = Singleton.getUserPreferences().getMinTopRecombinationRateAxis();
            IdealYAxisPane idealYAxisPane = new IdealYAxisPane(prefValue);
            int optionPaneReturn = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                    idealYAxisPane, "Preferred Y-Axis Limit", JOptionPane.OK_CANCEL_OPTION);
            if (optionPaneReturn == JOptionPane.OK_OPTION) {
                System.out.println("OK specified");
                boolean isValid = true;
                float idealYValue = 0f;
                try {
                    idealYValue = Float.parseFloat(idealYAxisPane.getValueTextField());
                } catch (NumberFormatException nfe) {
                    isValid = false;
                }
                if (!isValid) {
                    JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(Singleton.getMainPanel()),
                            "Number must be a positive value.",
                            "Input value error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    Singleton.getUserPreferences().setMinTopRecombinationRateAxis(idealYValue);
                    System.out.println("Returned from editor");
                }
            }

        }
    }

    public class SavePlotAction extends AbstractAction {

        /**
         * Action item for the copying the bland altman plot to the clipboard.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public SavePlotAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JpgFilter jpgFilter = new JpgFilter();
            File file = getFilename(jpgFilter);
            if (file != null) {
                ((MainPanel) MenuBar.this.mainPanel).writePlotPanelToFile(file);
            }
        }

        /**
         * Asks the user for a filename for the particular file
         *
         * @return
         */
        public File getFilename(SuffixFileFilter fileFilter) {
            JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter((FileFilter) fileFilter);

            String path = Singleton.getUserPreferences().getFilePath();
            if (path != null && path.trim().length() > 0) {
                fc.setCurrentDirectory(new File(path));
            }
            int returnVal = fc.showSaveDialog(SwingUtilities.getWindowAncestor(mainPanel));

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(fileFilter.getSuffix())) {
                    file = new File(filename + fileFilter.getSuffix());
                }
                Singleton.getUserPreferences().setFilePath(file.getParent());
                return file;
            } else {
                System.out.println("Export command cancelled by user.\n");
                return null;
            }
        }
    }

    public class CaptureAllAction extends AbstractAction {

        /**
         * Action item for the copying the bland altman plot to the clipboard.
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public CaptureAllAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //MenuBar.this.mainPanel.captureAllToClipboard();
        }
    }

    public class LegendAction extends AbstractAction {

        /**
         * Action item for creating, copying to clipboard and saving the legend
         *
         * @param itemName String
         * @param keystroke KeyStroke
         * @param mneumonic Integer
         * @param tooltip String
         */
        public LegendAction(String itemName, KeyStroke keystroke,
                Integer mneumonic, String tooltip) {
            super(itemName);
            if (keystroke != null) {
                putValue(ACCELERATOR_KEY, keystroke);
            }
            if (mneumonic != null) {
                putValue(MNEMONIC_KEY, mneumonic);
            }
            if (tooltip != null) {
                putValue(SHORT_DESCRIPTION, tooltip);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Legend Setup");
            LegendPanel legendPanel = new LegendPanel();
            frame.setContentPane(legendPanel);
            try {
                System.out.println("Class path" + System.getProperty("java.class.path"));
                java.net.URL imgURL = this.getClass().getResource("/images/guava_16.jpg");
                frame.setIconImage(new ImageIcon(imgURL).getImage());
            } catch (NullPointerException npe) {
                System.out.println("Failed to load in the icon.");
            }

            frame.pack();
            frame.setVisible(true);
        }
    }
}

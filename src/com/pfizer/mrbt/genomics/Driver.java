package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.thumbnail.ThumbnailPanel;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.heatmap.HeatmapPanel;
import com.pfizer.mrbt.genomics.modelselection.ModelSelectionPanel;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.StateListener;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;

/**
 * <p>
 * Title: Driver.java
 * </p>
 * <p>
 * Description: Runs the Pfizer Manhattan Viewer
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012, 2013
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Peter V. Henstock
 * @VERSION 1.0
 * 
 */
public class Driver implements Runnable {
	private JFrame frame;
	private JFrame thumbnailFrame;

	private JFrame geneModelFrame;
	private JFrame heatmapFrame;
	public final static String VERSION = "1.1.4";
	private String[] args;
	private Image image;
	private ThumbnailPanel thumbnailPanel;
	private HeatmapPanel heatmapPanel;
	private ModelSelectionPanel modelSelectionPanel;

	public Driver(String[] args) {
		this.args = args;

		StateController stateController = new StateController();

		if (args.length > 0) {
			/*
			 * This is the standard call for the TranSMART query returning
			 * results. Note that the args become a class variable and are
			 * initialized later
			 */
			if (args[0].equalsIgnoreCase("-services=transmart")) {
				Singleton.getState().setDataMode(State.TRANSMART_SERVICES_MODE);
			} else if (args[0].equalsIgnoreCase("-services=transmartdev")) {
				Singleton.getState().setDataMode(
						State.TRANSMART_DEV_SERVICES_MODE);
			} else {
				// default to bioservice
				Singleton.getState().setDataMode(State.BIOSERVICES_MODE);
			}
		} else {
			/*
			 * If you don't provide any parameters, it launches BIOSERVICES_MODE
			 * that is mostly defunct
			 */
			Singleton.getState().setDataMode(State.BIOSERVICES_MODE);
		}
		System.out.println("Driver mode is " + Singleton.getState().getDataServicesModeName());
		Singleton.getState().addListener(stateController);
	}

	/**
	 * Main thread that instantiates the frame and menubar
	 */
	@Override
	public void run() {
		try {
			UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			createMainFrame();
			createGeneModelFrame();
			createThumbnailFrame();
		} catch (Exception ex) {
			System.out.println("Caught exception in main run loop");
			ex.printStackTrace();
		}
	}

	/**
	 * Creates a frame for the main window. Note that the args are used here as
	 * a class variable to specify the results for the tranSMART-based query
	 * 
	 * @throws Exception
	 */
	protected void createMainFrame() throws Exception {
		String dataSrc = " using tranSMART";
		if (Singleton.getState().getDataMode() == State.BIOSERVICES_MODE) {
			dataSrc = " using AQG";
		} else if (Singleton.getState().getDataMode() == State.TRANSMART_DEV_SERVICES_MODE) {
			dataSrc = " using tranSMART Dev Instance";
		} else if (Singleton.getState().getDataMode() == State.TRANSMART_SERVICES_MODE) {
			dataSrc = " using tranSMART Stage Instance";
		}
		frame = new JFrame("GWAVA: Genome-Wide Association Visual Analyzer "
				+ VERSION + dataSrc);
		/*
		 * UserPreferences userPref = Singleton.getUserPreferences();
		 * userPref.loadUserPreferences();
		 */
		DataModel dataModel = Singleton.getDataModel();

		/**
		 * This is a key call to initialize the data and run a search from the
		 * tranSMART query
		 */
        String[] argsWithoutServices = removeServicesCallArg0(args);
		dataModel.initializeData(argsWithoutServices);

		String filename = "";
		if (filename == null) {
			System.exit(0);
		}

		MainPanel mainPanel = new MainPanel();

		frame.setContentPane(mainPanel);
		frame.setJMenuBar(new MenuBar(mainPanel));
		try {
			java.net.URL imgURL = this.getClass().getResource(
					"/images/guava_16.jpg");
			frame.setIconImage(new ImageIcon(imgURL).getImage());
		} catch (NullPointerException npe) {
			System.out.println("Failed to load in the icon.");
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowCloseController());
		frame.pack();
		frame.setVisible(true);
	}
    
    /**
     * Args might come in with a -services=something call at args[0].  
     * If so, this argument this should be removed and the rest parsed as before.  
     * This utility call removes the first argument and returns the rest if 
     * present, else returns args unchanged.
     * @param args
     * @return 
     */
    private String[] removeServicesCallArg0(String[] args) {
        if(args.length == 0) {
            return new String[0];
        } if(args[0].startsWith(("-services"))) {
            String[] remainingArgs = new String[args.length - 1];
            for(int i = 1; i < args.length; i++) {
                remainingArgs[i-1] = args[i];
            }
            return remainingArgs;
        } else {
            return args;
        }
    }

	protected void createGeneModelFrame() throws Exception {
		geneModelFrame = new JFrame("GWAVA Gene Model Selection");
		geneModelFrame.setContentPane(getModelSelectionPanel());
		try {
			java.net.URL imgURL = this.getClass().getResource(
					"/images/guava_16.jpg");
			geneModelFrame.setIconImage(new ImageIcon(imgURL).getImage());
		} catch (NullPointerException npe) {
			System.out.println("Failed to load in the icon.");
		}

		geneModelFrame.pack();
		geneModelFrame.setLocation(frame.getLocation().x + frame.getWidth(),
				frame.getY());
		geneModelFrame.setSize(new Dimension(250, frame.getHeight()));
		geneModelFrame.setVisible(true);

	}

	protected ModelSelectionPanel getModelSelectionPanel() {
		if (modelSelectionPanel == null) {
			modelSelectionPanel = Singleton.getModelSelectionPanel();
		}
		return modelSelectionPanel;
	}

	/**
	 * Creates an invisible JFrame with the thumbnail data
	 * 
	 * @throws Exception
	 */
	private void createThumbnailFrame() throws Exception {
		thumbnailFrame = new JFrame("GWAVA Thumbnails");
		thumbnailPanel = new ThumbnailPanel();
		thumbnailFrame.setContentPane(thumbnailPanel);
		thumbnailFrame.setPreferredSize(new Dimension(515, 500));
		try {
			java.net.URL imgURL = this.getClass().getResource(
					"/images/guava_16.jpg");
			thumbnailFrame.setIconImage(new ImageIcon(imgURL).getImage());
		} catch (NullPointerException npe) {
			System.out.println("Failed to load in the icon.");
		}
		thumbnailFrame.setLocation(500, 250);
		thumbnailFrame.pack();
		thumbnailFrame.setVisible(false);
	}

	private class WindowCloseController implements WindowListener {
		/**
		 * Handles the windoColosing event by verifying that they want to save
		 * changes if there have been any.
		 * 
		 * @param we
		 *            WindowEvent
		 */
		public void windowClosing(WindowEvent we) {
			Singleton.getUserPreferences().saveUserPreferences();
		}

		public void windowOpened(WindowEvent we) {
		}

		public void windowClosed(WindowEvent we) {
			System.exit(0);
		}

		public void windowActivated(WindowEvent we) {
		}

		public void windowDeactivated(WindowEvent we) {
		}

		public void windowIconified(WindowEvent we) {
		}

		public void windowDeiconified(WindowEvent we) {
		}
	}

	public class StateController implements StateListener {
		@Override
		public void mainPlotChanged(ChangeEvent ce) {
		}

		@Override
		public void currentChanged(ChangeEvent ce) {
		}

		@Override
		public void thumbnailsChanged(ChangeEvent ce) {
			if (thumbnailFrame == null) {
				try {
					createThumbnailFrame();
				} catch (Exception ex) {
					System.out
							.println("Caught exception in createThumnbnailFrame");
					ex.printStackTrace();
				}
			}
			thumbnailFrame.setVisible(true);
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

		@Override
		public void heatmapChanged(ChangeEvent ce) {
		}
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Driver(args));
	}

}

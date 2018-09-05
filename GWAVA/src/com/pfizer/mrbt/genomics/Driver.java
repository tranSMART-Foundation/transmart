package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.thumbnail.ThumbnailPanel;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.pfizer.mrbt.genomics.TransmartClient.TransmartServicesParameters;
import com.pfizer.mrbt.genomics.TransmartClient.TransmartWebServices;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.modelselection.ModelSelectionPanel;
import com.pfizer.mrbt.genomics.query.QueryPanel;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.webservices.DataRetrievalInterface;
import com.pfizer.mrbt.genomics.webservices.Environment;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
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
	public final static String VERSION = "2.1g";
    public final String GUAVA_16_ICON = "/images/guava_16.jpg";
	private final String[] args;
	private ThumbnailPanel thumbnailPanel;
	private ModelSelectionPanel modelSelectionPanel;

    /**
     * The args should contain -services=<data_retrieval_service> in args[0] that will be
     * translated into a data retrieval service engine
     * @param args 
     */
	public Driver(String[] args) {
		this.args = args;

		StateController stateController = new StateController();

        if (args.length > 0 && args[0].equalsIgnoreCase("-services=transmart")) {
            //Singleton.getState().setDataMode(State.TRANSMART_SERVICES_MODE);
            DataRetrievalInterface webServices = new TransmartWebServices(Environment.PRODUCTION);
            Singleton.getDataModel().setWebServices(webServices);
        } 
        else if (args.length > 0 && args[0].equalsIgnoreCase("-services=transmartstg")) {
			/*Singleton.getState().setDataMode(
					State.TRANSMART_DEV_SERVICES_MODE);*/
            DataRetrievalInterface webServices = new TransmartWebServices(Environment.STAGE);
            Singleton.getDataModel().setWebServices(webServices);
	} else if (args.length > 0 && args[0].equalsIgnoreCase("-services=transmarttst")) {
			/*Singleton.getState().setDataMode(
					State.TRANSMART_DEV_SERVICES_MODE);*/
            DataRetrievalInterface webServices = new TransmartWebServices(Environment.TEST);
            Singleton.getDataModel().setWebServices(webServices);
	} else if (args.length > 0 && args[0].equalsIgnoreCase("-services=transmartdev")) {
				/*Singleton.getState().setDataMode(
						State.TRANSMART_DEV_SERVICES_MODE);*/
                DataRetrievalInterface webServices = new TransmartWebServices(Environment.DEV);
                Singleton.getDataModel().setWebServices(webServices);
		} else {
            System.err.println("Driver does not have -services=<data_retrieval_interface> in args[0]");
            System.exit(1);
    	}
        
        // if URL parameter is passed then use it.
        if (args.length >= 7)
        	updateTransmartUrlFromJNLP(args[6]);

        // if user session id then use it as it will be used by Transmart to determine the user id
        if (args.length >= 8)
        	updateTransmartUserSession(args[7]);
        
		//System.out.println("Driver mode is " + Singleton.getState().getDataServicesModeName());
		Singleton.getState().addListener(stateController);
	}

	/**
	 * 
	 * Updating transmart user name.
	 * 
	 * @param sessionId
	 */
	private void updateTransmartUserSession(String sessionId) {
		TransmartServicesParameters.SESSION_ID = sessionId;
	}

	/**
	 * 
	 * Transmart URL used for WebServices access. This way it is not hardcoded but instead passed through the JNLP at creation time by Transmart.
	 * 
	 * @param host
	 */
	private void updateTransmartUrlFromJNLP(String host) {
        
        TransmartServicesParameters.updateUrlAndHosts(host);
        
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
            runInitialData();
		} catch (Exception ex) {
			System.out.println("Caught exception in main run loop");
			ex.printStackTrace();
		}
	}
    
    protected void runInitialData() {
		/**
		 * This is a key call to initialize the data and run a search from the
		 * tranSMART query.  12/19/2013 moved until after frame initialized
		 */
        String[] argsWithoutServices = removeServicesCallArg0(args);
        if(argsWithoutServices.length > 0) {
            try {
                Singleton.getDataModel().initializeData(argsWithoutServices);
                QueryPanel queryPanel = (QueryPanel) Singleton.getMainPanel().getQueryPanel();
            } catch(RetrievalException rex) {
                System.out.println("Retrieval exception " + rex.getMessage());
            }
        }
    }

	/**g
	 * Creates a frame for the main window. Note that the args are used here as
	 * a class variable to specify the results for the tranSMART-based query
	 * 
	 * @throws Exception
	 */
	protected void createMainFrame() throws Exception {
		/*String dataSrc = " using tranSMART";
		if (Singleton.getState().getDataMode() == State.BIOSERVICES_MODE) {
			dataSrc = " using AQG";
		} else if (Singleton.getState().getDataMode() == State.TRANSMART_DEV_SERVICES_MODE) {
			dataSrc = " using tranSMART Dev Instance";
		} else if (Singleton.getState().getDataMode() == State.TRANSMART_SERVICES_MODE) {
			dataSrc = " using tranSMART Stage Instance";
		}*/
        String dataRetrievalSource = Singleton.getDataModel().getWebServices().getSourceName();
        frame = Singleton.getMainFrame();
        frame.setTitle("GWAVA: Genome-Wide Association Visual Analyzer "
				+ VERSION + " using " + dataRetrievalSource);
		/*
		 * UserPreferences userPref = Singleton.getUserPreferences();
		 * userPref.loadUserPreferences();
		 */
		DataModel dataModel = Singleton.getDataModel();

		//MainPanel mainPanel = new MainPanel();
        MainPanel mainPanel = Singleton.getMainPanel();

		frame.setContentPane(mainPanel);
		frame.setJMenuBar(new MenuBar(mainPanel));
        Image img = loadImageIconResource();
        if(img != null) {
            frame.setIconImage(img);
		}

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowCloseController());
		frame.pack();
        if(Singleton.getUserPreferences().getMainFrameLocation() != null) {
            frame.setLocation(Singleton.getUserPreferences().getMainFrameLocation());
        }
        if(Singleton.getUserPreferences().getMainFrameSize() != null) {
            frame.setSize(Singleton.getUserPreferences().getMainFrameSize());
        }
		frame.setVisible(true);
        
	}
    
    /**
     * Attempts to load the GUAVA_16_ICON resource image.  It returns the
     * image if successful, else returns null.
     * @return 
     */
    private Image loadImageIconResource() {
		try {
			java.net.URL imgURL = this.getClass().getResource(GUAVA_16_ICON);
            ImageIcon imageIcon = new ImageIcon(imgURL);
            return imageIcon.getImage();
		} catch (NullPointerException npe) {
			System.err.println("Failed to load in the icon [" + GUAVA_16_ICON + "]");
		}
        return null;
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
        geneModelFrame = Singleton.getGeneModelFrame();
        geneModelFrame.setTitle("GWAVA Gene Model Selection");
		geneModelFrame.setContentPane(getModelSelectionPanel());
        Image img = loadImageIconResource();
        if(img != null) {
            geneModelFrame.setIconImage(img);
        }
		geneModelFrame.pack();
		geneModelFrame.setLocation(frame.getLocation().x + frame.getWidth(),
				frame.getY());
		geneModelFrame.setSize(new Dimension(250, frame.getHeight()));
        if(Singleton.getUserPreferences().getGeneModelFrameLocation() != null) {
            geneModelFrame.setLocation(Singleton.getUserPreferences().getGeneModelFrameLocation());
        }
        if(Singleton.getUserPreferences().getGeneModelFrameSize() != null) {
            geneModelFrame.setSize(Singleton.getUserPreferences().getGeneModelFrameSize());
        }
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
        Image img = loadImageIconResource();
        if(img != null) {
            thumbnailFrame.setIconImage(img);
}
		thumbnailFrame.setLocation(500, 250);
		thumbnailFrame.pack();
		thumbnailFrame.setVisible(false);
	}

	private class WindowCloseController implements WindowListener {
		/**
		 * Handles the windoColosing event by verifying that they want to save
		 * changes if there have been any.
		 * @param we   WindowEvent
		 */
        @Override
		public void windowClosing(WindowEvent we) {
            Singleton.getUserPreferences().setWindowPositions(frame, geneModelFrame);
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
					System.out.println("Caught exception in createThumnbnailFrame");
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

    /**
     * Main call for the GWAVA program to initialize the GUI and full program
     * @param args 
     */
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Driver(args));
	}

}

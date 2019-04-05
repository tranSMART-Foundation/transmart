package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mkapoor
 */
@CompileStatic
class CohortInformation {

    public static final int PLATFORMS_TYPE = 1
    public static final int TRIALS_TYPE = 2
    public static final int TIMEPOINTS_TYPE = 3
    public static final int SAMPLES_TYPE = 4
    public static final int GPL_TYPE = 5
    public static final int TISSUE_TYPE = 6
    public static final int RBM_PANEL_TYPE = 7

    List platforms = []
    List trials = []
    List timepoints = []
    List samples = []
    List gpls = []
    List tissues = []
    List rbmpanels = []

    String getAllTrials() {
	trials.join ','
    }
}

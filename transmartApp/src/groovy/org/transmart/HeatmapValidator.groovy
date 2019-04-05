package org.transmart

import groovy.transform.CompileStatic

/**
 * @author JIsikoff
 */
@CompileStatic
class HeatmapValidator {

    LinkedHashSet<String> platforms = []
    LinkedHashSet timepoints = []
    LinkedHashSet timepointLabels = []
    LinkedHashSet samples = []
    LinkedHashSet sampleLabels = []
    LinkedHashSet gpls = []
    LinkedHashSet gplLabels = []
    LinkedHashSet tissues = []
    LinkedHashSet tissueLabels = []
    LinkedHashSet rbmpanels = []
    LinkedHashSet rbmpanelsLabels = []
    String msg = ''
    boolean valid = false

    private Map<String, LinkedHashSet> setsByField = [
	gplLabels: gplLabels,
	gpls: gpls,
	tissueLabels: tissueLabels,
	tissues: tissues,
	rbmpanels: rbmpanels,
	rbmpanelsLabels: rbmpanelsLabels]

    boolean validate() {
	int p = platforms.size()
        if (p > 1) {
	    msg = 'Too many platforms found. '
            valid = false
        }
        else if (p < 1) {
	    msg = 'No platforms found. '
            valid=false
        }
	else { // p == 1
	    msg = ''
	    valid = true
        }

	valid
    }

    String getFirstPlatform() {
	platforms[0]
    }

    String getFirstPlatformLabel() {
	String platform = firstPlatform
	if (platform) {
	    'MRNA_AFFYMETRIX' == platform ? 'MRNA' : platform
        }
    }

    def getFirstTimepoint() {
	timepoints[0]
    }

    String getAllTimepoints() {
	timepoints.join ','
    }

    String getAllTimepointLabels() {
	timepointLabels.join ','
    }

    String getAllSamples() {
	samples.join ','
    }

    String getAllSampleLabels() {
	sampleLabels.join ','
    }

    String getAll(String field) {
	(setsByField[field] ?: []).join ','
    }
}

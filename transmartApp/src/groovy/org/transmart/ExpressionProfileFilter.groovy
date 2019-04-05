package org.transmart

import groovy.transform.CompileStatic

/**
 * stores filter params for expression profile filter screen
 */
@CompileStatic
class ExpressionProfileFilter {

    Long bioDiseaseId
    Long bioMarkerId
    String probeSet

    boolean filterDisease() {
	bioDiseaseId
    }

    boolean filterBioMarker() {
	bioMarkerId
    }

    boolean filterProbeSet() {
	probeSet
    }

    void reset() {
	bioDiseaseId = null
	bioMarkerId = null
	probeSet = null
    }
}

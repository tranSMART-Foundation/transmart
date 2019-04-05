package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class HeatmapDataValue {
    def bioMarkerId
    String bioMarkerName
    def assayAnalysisId
    def foldChangeRatio
    def rvalue
    def rhoValue
}

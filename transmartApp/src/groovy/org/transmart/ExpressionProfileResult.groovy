package org.transmart

import groovy.transform.CompileStatic

/**
 * stores results for expression profile
 * @author mmcduffie
 */
@CompileStatic
class ExpressionProfileResult {
    def genes = [] // genes retrieved from a search
    def diseases = [] // diseases retrieved from search or gene change
    def probeSets = []
    def graphURL // box plot URL
    def datasetItems = [] // dataset items associated with box plot
    int profCount = 0 // experiment count from search
}

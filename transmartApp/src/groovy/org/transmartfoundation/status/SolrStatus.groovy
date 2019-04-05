package org.transmartfoundation.status

import groovy.transform.CompileStatic

@CompileStatic
class SolrStatus {

    String url
    boolean connected
    boolean rwgAvailable
    int rwgNumberOfRecords
    boolean browseAvailable
    int browseNumberOfRecords
    boolean sampleAvailable
    int sampleNumberOfRecords
    Date lastProbe

    String toString () {
	'SolrStatus (URL: ' + url + ') - probe at: ' + lastProbe
    }
}

package org.transmartfoundation.status

import groovy.transform.CompileStatic

@CompileStatic
class GwavaStatus {

    String url
    boolean connected
    Date lastProbe
    boolean enabled
    String errorMessage

    String toString() {
	'GwavaStatus (' + url + ') - probe at: ' + lastProbe
    }
}

package org.transmartfoundation.status

import groovy.transform.CompileStatic

@CompileStatic
class RserveStatus {

    String url
    boolean connected
    boolean simpleExpressionOK
    boolean librariesOk
    String lastErrorMessage
    Date lastProbe

    String toString () {
	'RserveStatus (' + url + ') - probe at: ' + lastProbe
    }
}

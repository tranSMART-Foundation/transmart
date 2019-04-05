package org.transmartfoundation.status

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

@CompileStatic
class GwavaStatusService {

    static transactional = false

    @Value('${com.recomdata.rwg.webstart.codebase:}')
    private String webstartCodebase

    @Value('${com.recomdata.rwg.webstart.transmart.url:}')
    private String webstartTransmartUrl

    GwavaStatus getStatus() {
	String errorMessage = 'URL did not respond'
        boolean sawText = false
        URL gwava
        try {
	    gwava = new URL(webstartCodebase)
	    for (String line in gwava.text.readLines()) {
                errorMessage = ''
                if (line.contains('GWAVA QuickStart')) {
                    sawText = true
		    break
                }
            }
        }
        catch (MalformedURLException e) {
            errorMessage = 'MalformedURLException: ' + e.message
        }
	catch (IOException e) {
	    errorMessage = 'IOException: ' + e.message
        }
	catch (e) {
	    errorMessage = 'Unexpected error: ' + e.message
        }

	new GwavaStatus(
	    url: webstartCodebase,
	    enabled: webstartTransmartUrl.length() > 0,
	    connected: sawText,
	    errorMessage: errorMessage,
	    lastProbe: new Date())
    }
}

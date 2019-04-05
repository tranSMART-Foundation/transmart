import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmart.plugin.shared.UtilService

/**
 * @author mmcduffie
 */
@Slf4j('logger')
class ExportController {

    I2b2HelperService i2b2HelperService
    UtilService utilService

    def index() {}

    def exportSecurityCheck(String result_instance_id1, String result_instance_id2) {
	logger.debug 'Check export security'
	boolean canExport = canExport(result_instance_id1, result_instance_id2)
	logger.debug 'CANEXPORT:{}', canExport
	render([canExport: canExport] as JSON)
    }

    private boolean canExport(String rid1, String rid2) {
	def sectokens = i2b2HelperService.getSecureTokensWithAccessForUser()
	for (String trial in i2b2HelperService.getDistinctTrialsInPatientSets(rid1, rid2)) {
	    if (!sectokens.containsKey(trial)) {
		logger.debug 'not found key in export check:{}', trial
		return false //short circuit if found a single one that isnt in the tokens collection
	    }

	    logger.debug 'checking found key:{}:{}', trial, sectokens[trial]
	    logger.debug 'equals own:{}', sectokens[trial] == 'OWN'
	    logger.debug 'equals export:{}', sectokens[trial] == 'EXPORT'
	    if (sectokens[trial] != 'OWN' & sectokens[trial] != 'EXPORT') {//if not export or own then also return false
		logger.debug 'in return false inner'
                return false
            }
        }

	logger.debug 'made it to end of loop so the user can export'
	true
    }

    /**
     * Checks to see if the user has run at least one heatmap.  If so, return true
     * so we can export the file.  If not, notify the client so they can alert the user.
     */
    def check() {
	render([ready: session.expdsfilename != null] as JSON)
    }

    /**
     * Simply just takes the stored csv file that was stored and presents
     * it to the user.  Kinda silly to save it locally and then just restream it to the
     * user but we'll leave that for now.  We could also cache the initial results
     * or just take the tab separated values that are saved, convert it and then
     * send it to the user.
     */
    def exportDataset() {
	String expdsfilename = session.expdsfilename
	logger.debug 'Export filename: {}', expdsfilename
	byte[] bytes = 'No data to export'.bytes
	if (expdsfilename != null) {
	    logger.debug 'Made it to exportDataset for file: {}', expdsfilename
	    File testFile = new File(expdsfilename)
	    InputStream inputStream = new FileInputStream(testFile)
	    long length = testFile.length()
	    logger.debug 'Length: {}', length
	    bytes = new byte[(int) length]
            int offset = 0
            int numRead = 0
	    while (offset < bytes.length && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead
            } // Ensure all the bytes have been read in

            if (offset < bytes.length) {
		throw new IOException('Could not completely read file ' + expdsfilename)
            }

	    inputStream.close()
        }

        int outputSize = bytes.length
	logger.debug 'Size of bytes: {}', outputSize
	utilService.sendDownload response, 'text/csv', 'exportdatasets.csv', bytes
    }
}

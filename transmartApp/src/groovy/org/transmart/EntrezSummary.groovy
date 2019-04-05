package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class EntrezSummary {
    String GeneID
    String Name
    String Description
    String Orgname
    String OtherAliases
    String Mim
    String Summary
    String NomenclatureStatus

    String getOMIMID() {
	if (Mim) {
	    String[] split = Mim.split(':')
	    if (split && split.length > 1) {
		return split[1]
            }
        }
    }

    String[] getAliases() {
	OtherAliases?.split ','
    }
}

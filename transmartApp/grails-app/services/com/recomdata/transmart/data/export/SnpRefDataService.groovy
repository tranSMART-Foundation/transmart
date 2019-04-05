package com.recomdata.transmart.data.export;

import de.DeSNPInfo

class SnpRefDataService {

    static transactional = false

    Collection<String> findRsIdByGeneNames(Collection geneNames) {
	DeSNPInfo.executeQuery('''
			SELECT distinct rsId
			FROM DeSNPInfo s
			WHERE s.geneName IN (:gn)''', [gn: geneNames])
    }

    DeSNPInfo findRefByRsId(String rsid) {
	if (rsid) {
	    String nrsid = rsid.trim().toLowerCase()
	    if (!nrsid.startsWith('rs')) {
                nrsid = 'rs' + nrsid
            }
	    DeSNPInfo.findByRsId nrsid
	}
    }
}

package com.recomdata.grails.plugin.gwas

import org.transmart.biomart.BioAssayAnalysisDataIdx

class GwasSearchService {

    static transactional = false

    List<BioAssayAnalysisDataIdx> getGwasIndexData() {
	BioAssayAnalysisDataIdx.findAllByExt_type('GWAS', [sort: 'display_idx', order: 'asc'])
    }

    List<BioAssayAnalysisDataIdx> getEqtlIndexData() {
	BioAssayAnalysisDataIdx.findAllByExt_type('EQTL', [sort: 'display_idx', order: 'asc'])
    }
}

package com.recomdata.grails.plugin.gwas

import grails.plugin.cache.Cacheable
import org.transmart.biomart.Experiment

@Cacheable('com.recomdata.grails.plugin.gwas.ExperimentService')
class ExperimentService {

    static transactional = false

    String getExperimentAccession(Long experimentId) {
	experimentId ? Experiment.where { id == experimentId }.accession.get() : null
    }
}


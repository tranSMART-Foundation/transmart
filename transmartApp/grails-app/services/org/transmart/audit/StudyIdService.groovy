/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmart.audit

import grails.plugin.cache.Cacheable
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult

import javax.annotation.Resource

@Slf4j('logger')
class StudyIdService {

    static transactional = false

    @Resource QueriesResource queriesResourceService
    @Resource ConceptsResource conceptsResourceService

    @Value('${org.transmart.i2b2.view.enable:false}')
    private boolean i2b2View

    /**
     * Fetches the study id associated with a concept from the 
     * {@link ConceptsResource} using the concept key.
     * 
     * @param conceptKey the concept key.
     * @param options map with optional parameters:
     *  - 'studyConceptOnly': if set, a study name will only be returned if the
     *    concept is a study.
     * @return the study id as string if the concept is found; null if the
     *         concept key is null; the empty string if the concept key is empty 
     *         or the concept could not be found.
     */
    @Cacheable('org.transmart.audit.StudyIdService')
    String getStudyIdForConceptKey(Map options = [:], String conceptKey) {
	if (conceptKey == null) {
            return null
        }

	conceptKey = conceptKey.trim()
	if (!conceptKey) {
            return ''
        }

        String studyId = ''
        try {
//	    logger.debug 'Query study id for concept key: {} options {}', conceptKey, options
	    OntologyTerm term = conceptsResourceService.getByKey(conceptKey)
//	    logger.debug 'term {}', term
            Study study = term?.study
            studyId = study?.id
//	    logger.debug 'study {} studyId {} study.ontologyTerm {}', study, studyId, study?.ontologyTerm
            if (options?.studyConceptOnly && study?.ontologyTerm != term) {
//		logger.debug 'studyId {} set to null ontologyTerm {} does not match term {}', studyId, study?.ontologyTerm?.fullName, term?.fullName
                studyId = null
            }
//	    logger.debug 'Study id for concept key {} is: {}', conceptKey, studyId
        }
	catch (NoSuchResourceException ignored) {
	    logger.warn 'Resource not found: ConceptResource.getByKey({})', conceptKey
        }
        studyId
    }

    @Cacheable('org.transmart.audit.StudyIdService')
    Set<String> getStudyIdsForQueryId(Long queryId) {
        Set<String> result = []
        try {
//	    logger.debug 'Query trials for query id: {}', queryId
            QueryResult queryResult = queriesResourceService.getQueryResultFromId(queryId)
            result = queryResult.patients*.trial as Set
        }
	catch (NoSuchResourceException ignored) {
	    logger.warn 'Resource not found: QueriesResource.getQueryResultFromId({})', queryId
        }
        result
    }

    /**
     * Fetches the study ids associated with a collection of queries from the 
     * {@link QueriesResource} using their query ids.
     * Empty query ids and non-integer values are ignored. If a query cannot be
     * found for a certain query id, that result is ignored.
     *
     * @param queryIds a list of query ids (a.k.a. result_instance_ids). The ids
     *        are passed as string.
     * @return a string with the comma-separated list study ids.
     */
    String getStudyIdsForQueries(List<String> queryIds) {
        Set<String> studyIds = []
	for (String queryId in queryIds) {
	    if (queryId && queryId != 'null') {
                queryId = queryId.trim()
		if (queryId) {
		    if (queryId.isLong()) {
			studyIds << getStudyIdsForQueryId(queryId.toLong())
                    }
                    else {
			logger.warn 'Query id is not an integer: {}', queryId
                    }
                }
            }
        }

	studyIds.sort().join ','
    }
}

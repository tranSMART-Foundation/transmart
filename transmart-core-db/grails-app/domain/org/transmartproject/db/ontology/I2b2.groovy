/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.ontology

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.ontology.Study

@Slf4j('logger')
class I2b2 extends AbstractI2b2Metadata implements Serializable {

    static final String backingTable = 'I2B2'

    BigDecimal   cTotalnum
    String       cComment
    String       mAppliedPath
    Date         updateDate
    Date         downloadDate
    Date         importDate
    String       sourcesystemCd
    String       valuetypeCd
    String       mExclusionCd
    String       cPath
    String       cSymbol

    static transients = AbstractI2b2Metadata.transients + ['studyId', 'study']

    static mapping = {
        table  'i2b2metadata.i2b2'
	id composite: ['fullName', 'name'] // hibernate needs an id, see http://docs.jboss.org/hibernate/orm/3.3/reference/en/html/mapping.html#mapping-declaration-id
        version  false

        AbstractI2b2Metadata.mapping.delegate = delegate
        AbstractI2b2Metadata.mapping()
    }

    static constraints = {
        cComment       nullable: true
        cPath          nullable: true,  maxSize: 700
        cSymbol        nullable: true,  maxSize: 50
        cTotalnum      nullable: true
        downloadDate   nullable: true
        importDate     nullable: true
        mAppliedPath                    maxSize: 700
        mExclusionCd   nullable: true,  maxSize: 25
        sourcesystemCd nullable: true,  maxSize: 50
        valuetypeCd    nullable: true,  maxSize: 50

        AbstractI2b2Metadata.constraints.delegate = delegate
        AbstractI2b2Metadata.constraints()
    }

    @Value('${org.transmart.i2b2.view.enable:false}')
    private boolean i2b2View

    String getStudyId() {
        def matcher = cComment =~ /(?<=^trial:).+/
	String result
	logger.debug 'getStudyId cComment {} matcher {} i2b2View {}', cComment, matcher, i2b2View
        if (matcher.find()) {
            result = matcher.group 0
	    if(result == null) {
		logger.debug 'getStudyId matcher empty, no trial in comment, i2b2View true default to I2B2'
		result = 'I2B2'
	    }
	    result
	}
    }

    @Override
    Study getStudy() {
        def trial = studyId
	if (i2b2View && !trial) {
	    trial = 'I2B2'
	    return null
	} else {
            def query = sessionFactory.currentSession.createQuery '''
                SELECT I
                FROM I2b2 I, TmTrialNodes TN
                WHERE (I.fullName = TN.fullName) AND
                TN.trial = :trial'''
            query.setParameter 'trial', trial

            def res = query.list()
            if (res.size() > 1) {
		throw new UnexpectedResultException("More than one study with name $trial")
            }
            if (res.size() == 1) {
		new StudyImpl(ontologyTerm: res[0], id: trial)
            }
            else {
		logger.debug 'No study found with name "{}"', trial
		null
            }
	}
    }
}

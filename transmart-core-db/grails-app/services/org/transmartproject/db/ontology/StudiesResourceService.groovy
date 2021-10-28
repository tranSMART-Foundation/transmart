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

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.StudiesResource
import org.transmartproject.core.ontology.Study
import org.transmartproject.db.i2b2data.TmTrialNodes

@CompileStatic
class StudiesResourceService implements StudiesResource {

    static transactional = false

    @Value('${org.transmart.i2b2.view.enable:false}')
    private boolean i2b2View

    Set<Study> getStudySet() {
        // we actually only search the i2b2 table here
        // given that studies are implemented in a specific way in transmart,
        // and that i2b2 is the only used ontology table,
        // we have to drop the pretence that we use table_access and multiple
        // ontology tables at this point.
	List<Object[]> rows = I2b2.executeQuery('''
				SELECT I, TN.trial
				FROM I2b2 I, TmTrialNodes TN
				WHERE (I.fullName = TN.fullName)''') as List<Object[]>
	    // the query is awkward (cross join) due to the non-existence of an
	    // association. See comment on TmTrialNodes

	    Set<Study> studies = []
	for (Object[] row in rows) {
	    studies << new StudyImpl(ontologyTerm: (OntologyTerm) row[0], id: (String) row[1])
	}

	studies
    }

    Study getStudyById(String id) throws NoSuchResourceException {
        String normalizedStudyId = id.toUpperCase(Locale.ENGLISH)
	List<I2b2> result = I2b2.executeQuery('''
				SELECT I
				FROM I2b2 I
				WHERE fullName IN (SELECT fullName FROM TmTrialNodes WHERE trial = :trial)''',
					      [trial: normalizedStudyId])

	if (!result) {
	    if(i2b2View) {
//		return new StudyImpl(ontologyTerm: null, id: 'I2B2')
		return new StudyImpl(id: 'I2B2')
	    }
	    throw new NoSuchResourceException("No study with id '$id' was found")
	}
	if (result.size() > 1) {
	    throw new UnexpectedResultException("Found more than one study term with id '$id'")
	}
	new StudyImpl(ontologyTerm: result.first(), id: normalizedStudyId)
    }

    Study getStudyByOntologyTerm(OntologyTerm term) throws NoSuchResourceException {
	TmTrialNodes trialNodes
	if (OntologyTerm.VisualAttributes.STUDY in term.visualAttributes &&
	    term.hasProperty('studyId') && term['studyId']) {
	    return new StudyImpl(ontologyTerm: term, id: (String) term['studyId'])
	}

	if ((trialNodes = TmTrialNodes.findWhere(fullName: term.fullName))) {
	    return new StudyImpl(ontologyTerm: term, id: trialNodes.trial)
	}

	throw new NoSuchResourceException("The ontology term $term is not the top node for a study")
    }
}

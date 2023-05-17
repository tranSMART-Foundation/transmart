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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm

import static org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TABLE_CODE
import static org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TOP_TERM_NAME

@Slf4j('logger')
@CompileStatic
class AcrossTrialsConceptsResourceDecorator implements ConceptsResource {

    ConceptsResource inner

    private final OntologyTerm topTerm = new AcrossTrialsTopTerm()

    List<OntologyTerm> getAllCategories() {
//	logger.info 'getAllCategories acrosstrials CODE {} NAME {}', ACROSS_TRIALS_TABLE_CODE, ACROSS_TRIALS_TOP_TERM_NAME
//	logger.info 'getAllCategories acrosstrials topTerm {} inner {}', topTerm, inner
	([topTerm] + inner.allCategories) as List
    }

    OntologyTerm getByKey(String conceptKey) throws NoSuchResourceException {
	ConceptKey conceptKeyObj = new ConceptKey(conceptKey)

        if (conceptKeyObj.tableCode != ACROSS_TRIALS_TABLE_CODE) {
//	    logger.info 'getByKey notacross conceptKey {} inner {}', conceptKey, inner
            return inner.getByKey(conceptKey)
        }

	ConceptFullName fullName = conceptKeyObj.conceptFullName
        if (fullName[0] != ACROSS_TRIALS_TOP_TERM_NAME) {
	    throw new NoSuchResourceException(
		"All the across trials terms' first path component should be " +
		    ACROSS_TRIALS_TOP_TERM_NAME)
        }

//	logger.info 'getByKey acrosstrials fullName {}', fullName
        if (fullName.length == 1) {
//	    logger.info 'getByKey acrosstrials topTerm {}', topTerm
            topTerm
	}
	else { // > 1
	    String modifierPath = buildModifierPath(fullName)
	    DeXtrialNodeView xtrialNode = DeXtrialNodeView.get(modifierPath)
            if (!xtrialNode) {
		throw new NoSuchResourceException(
		    'Could not find across trials node with modifier_path ' + modifierPath)
            }

//	    logger.info 'getByKey acrosstrials modifierPath {} xtrialNode level {} code {} name {} path {}',
//		modifierPath, xtrialNode.level, xtrialNode.code, xtrialNode.name, xtrialNode.path
            new AcrossTrialsOntologyTerm(deXtrialNode: xtrialNode)
        }
    }

    @CompileDynamic
    private String buildModifierPath(ConceptFullName fullName) {
	"\\${fullName[1..-1].join '\\'}\\"
    }
}

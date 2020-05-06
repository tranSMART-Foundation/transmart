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

import org.transmart.plugin.shared.Utils
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.db.user.User

abstract class AbstractAcrossTrialsOntologyTerm implements OntologyTerm, MetadataSelectQuerySpecification {

    public static final String ACROSS_TRIALS_TABLE_CODE = 'xtrials'
    public static final String ACROSS_TRIALS_TOP_TERM_NAME = 'Across Trials'

    // Relies mainly on fullName; also on level

    ConceptKey getConceptKey() {
        new ConceptKey(ACROSS_TRIALS_TABLE_CODE, fullName)
    }

    String getKey() {
        conceptKey.toString()
    }

    String getName() {
        conceptKey.conceptFullName.parts[-1]
    }

    String getTooltip() {
        name
    }

    Study getStudy() {
	// null for now. We may want to create a fake study for xtrials purposes
    }

    List<OntologyTerm> getChildren() {
	getDescendants false
    }

    List<OntologyTerm> getChildren(boolean showHidden, boolean showSynonyms) {
	getDescendants false, showHidden, showSynonyms
    }

    List<OntologyTerm> getAllDescendants() {
	getDescendants true
    }

    List<OntologyTerm> getAllDescendants(boolean showHidden, boolean showSynonyms) {
	getDescendants true, showHidden, showSynonyms
    }

    private List<OntologyTerm> getDescendants(boolean allDescendants,
                                              boolean showHidden = false /* ignored */,
                                              boolean showSynonyms = false /* ignored */) {
        // do not include ACROSS_TRIALS_TOP_TERM_NAME part in prefix:
        String pathPrefix = conceptKey.conceptFullName.length > 1 ?
            "\\${conceptKey.conceptFullName[1..-1].join '\\'}\\" : null

        DeXtrialNodeView.withCriteria {
            if (pathPrefix) {
		like 'path', Utils.asLikeLiteral(pathPrefix) + '%'
            }
            if (!allDescendants) {
                eq 'level',
		    level - 1L /* "Across Trials" */ + 1L /* children */
            }
        }.collect { new AcrossTrialsOntologyTerm(deXtrialNode: it) }
    }

    List<Patient> getPatients() {
        // can't work right now because this object doesn't have access to
        // user in context. To support this we'll probably have to move
        // the user in context bean from transmartApp to core-db
	throw new UnsupportedOperationException('Retrieving patients for x-trial node not supported yet')
    }

    // query specification methods

    String getFactTableColumn() {
        'modifier_cd'
    }

    String getDimensionTableName() {
        'deapp.de_xtrial_node'
    }

    String getColumnName() {
        'modifier_path'
    }

    String getColumnDataType() {
        'T' // text
    }

    String getOperator() {
        'LIKE'
    }

    String getDimensionCode() {
	ConceptFullName conceptFullName = new ConceptFullName(fullName)
        if (conceptFullName.length == 1) {
            '\\'
        }
        else {
            "\\${conceptFullName[1..-1].join '\\'}\\"
        }
    }

    String postProcessQuery(String sql, User userInContext) {
        if (userInContext == null) {
            throw new NullPointerException('Across trial nodes need to have the user provided')
        }

	Set<Study> accessibleStudies = userInContext.accessibleStudies
        if (!accessibleStudies) {
	    return sql + ' AND FALSE'
        }

        sql += 'AND sourcesystem_cd IN '
        sql += '(' +
            userInContext.accessibleStudies.collect {
            "\'${it.id.replaceAll('\'', '\'\'')}\'"
        }.join(', ') + ')'
        sql
    }
}

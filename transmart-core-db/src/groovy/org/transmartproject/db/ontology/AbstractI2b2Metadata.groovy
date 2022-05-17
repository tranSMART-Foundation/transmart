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

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import org.transmart.plugin.shared.Utils
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study

@EqualsAndHashCode(includes = [ 'fullName', 'name' ])
@Slf4j('logger')
abstract class AbstractI2b2Metadata extends AbstractQuerySpecifyingType implements OntologyTerm {

    Integer      level
    String       fullName
    String       name
    String       code
    String       tooltip
    String       metadataxml

    // properties abstracted with other properties
    String       cVisualattributes = ''
    Character    cSynonymCd = 'N'

    // Transient
    String       tableCode

    static transients = [ 'synonym', 'metadata', 'tableCode' ]

    static mapping = {
        code               column: 'C_BASECODE'
        columnDataType     column: 'C_COLUMNDATATYPE'
	columnName         column: 'C_COLUMNNAME'
        dimensionCode      column: 'C_DIMCODE'
	dimensionTableName column: 'C_TABLENAME'
	factTableColumn    column: 'C_FACTTABLECOLUMN'
	fullName           column: 'C_FULLNAME'
	level              column: 'C_HLEVEL'
        metadataxml        column: 'C_METADATAXML'
	name               column: 'C_NAME'
	operator           column: 'C_OPERATOR'
	tooltip            column: 'C_TOOLTIP'
    }

    static constraints = {
        code              nullable: true,  maxSize: 50
	cVisualattributes size: 1..3
	fullName          size: 2..700
	level             min: 0
        metadataxml       nullable: true
	name              size: 1..2000
	tooltip           nullable: true, maxSize: 900

        AbstractQuerySpecifyingType.constraints.delegate = delegate
        AbstractQuerySpecifyingType.constraints()
    }

    EnumSet<OntologyTerm.VisualAttributes> getVisualAttributes() {
	OntologyTerm.VisualAttributes.forSequence cVisualattributes
    }

    boolean isSynonym() {
        cSynonymCd == 'Y'
    }

    void setSynonym(boolean value) {
        cSynonymCd = value ? 'Y' : 'N'
    }

    private String getTableCode() {
        if (tableCode) {
            return tableCode
        }

        TableAccess candidate = null
	for  (TableAccess ta in TableAccess.list()) {
	    if (fullName.startsWith(ta.fullName)) {
		if (!candidate || ta.fullName.length() > candidate.fullName.length()) {
		    candidate = ta
                }
            }
        }

	if (!candidate) {
	    throw new RuntimeException("Could not determine table code for $this")
	}

        tableCode = candidate.tableCode
        tableCode
    }

    ConceptKey getConceptKey() {
        new ConceptKey(getTableCode(), fullName)
    }

    String getKey() {
        conceptKey.toString()
    }

    Map getMetadata() {
	if (!metadataxml) {
            return null
	}

	GPathResult slurper = new XmlSlurper().parseText(metadataxml)
	Map metadata = [:]

	// right now we only care about normalunits and oktousevalues
	metadata.okToUseValues = slurper.Oktousevalues == 'Y'
	metadata.unitValues = [
            normalUnits: slurper.UnitValues?.NormalUnits?.toString(),
            equalUnits: slurper.UnitValues?.EqualUnits?.toString(),
        ]

        def seriesMeta = slurper.SeriesMeta
        if (seriesMeta) {
	    metadata.seriesMeta = [
		unit : seriesMeta.Unit?.toString(),
		value: seriesMeta.Value?.toString(),
		label: seriesMeta.DisplayName?.toString(),
            ]
        }

	metadata
    }

    Study getStudy() {
        // since Study (in this sense) is a transmart concept, this only makes
        // sense for objects from tranSMART's i2b2 metadata table: I2b2
    }

    List<OntologyTerm> getChildren(boolean showHidden = false, boolean showSynonyms = false) {
	getDescendants false, showHidden, showSynonyms
    }

    List<OntologyTerm> getAllDescendants(boolean showHidden = false, boolean showSynonyms = false) {
	getDescendants true, showHidden, showSynonyms
    }

    private List<OntologyTerm> getDescendants(boolean allDescendants, boolean showHidden = false,
                                              boolean showSynonyms = false) {
	String fullNameSearch = Utils.asLikeLiteral(conceptKey.conceptFullName.toString()) + '%'

	List<OntologyTerm> ret = createCriteria().list {
            and {
                like 'fullName', fullNameSearch
                if (allDescendants) {
                    gt 'level', level
                }
                else {
                    eq 'level', level + 1
                }

                if (!showHidden) {
                    not { like 'cVisualattributes', '_H%' }
                }
                if (!showSynonyms) {
                    eq 'cSynonymCd', 'N' as char
                }
            }
	    order 'name'
        }

	String tableCode = getTableCode()
	for (it in ret) {
	    it.tableCode = tableCode
	}

        ret
    }

    List<Patient> getPatients() {
        super.getPatients(this)
    }

    String toString() {
        getClass().canonicalName + "[${attached?'attached':'not attached'}" +
	    "] [ fullName=$fullName, level=$level,  ]"
    }
}

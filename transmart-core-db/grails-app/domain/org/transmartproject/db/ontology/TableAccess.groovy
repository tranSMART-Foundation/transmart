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

import grails.orm.HibernateCriteriaBuilder
import grails.util.Holders
import groovy.transform.EqualsAndHashCode
import org.transmart.plugin.shared.Utils
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTerm.VisualAttributes
import org.transmartproject.core.ontology.Study

@EqualsAndHashCode(includes = [ 'tableCode' ])
class TableAccess extends AbstractQuerySpecifyingType implements OntologyTerm, Serializable {

    Date cChangeDate
    String cComment
    Date cEntryDate
    String cMetadataxml
    String code
    Character cProtectedAccess
    Character cStatusCd
    Character cSynonymCd = 'N'
    BigDecimal cTotalnum
    String cVisualattributes = ''
    String fullName
    Integer level
    String name
    String tableCode
    String tableName
    String tooltip
    String valuetypeCd

    static mapping = {
        table 'i2b2metadata.table_access'
	id composite: ['tableCode']
	// hibernate needs an id, see http://docs.jboss.org/hibernate/orm/3.3/reference/en/html/mapping.html#mapping-declaration-id
	version false

        code                 column:   'C_BASECODE'
        columnDataType       column:   'C_COLUMNDATATYPE'
        columnName           column:   'C_COLUMNNAME'
        dimensionCode        column:   'C_DIMCODE'
        dimensionTableName   column:   'C_DIMTABLENAME'
        factTableColumn      column:   'C_FACTTABLECOLUMN'
        fullName             column:   'C_FULLNAME'
        level                column:   'C_HLEVEL'
        name                 column:   'C_NAME'
        operator             column:   'C_OPERATOR'
        tableCode            column:   'C_TABLE_CD'
        tableName            column:   'C_TABLE_NAME'
        tooltip              column:   'C_TOOLTIP'
	}

	static constraints = {
        cChangeDate         nullable:   true
        cComment            nullable:   true
        cEntryDate          nullable:   true
        cMetadataxml        nullable:   true
        code                nullable:   true,   maxSize:   50
        cProtectedAccess    nullable:   true
        cStatusCd           nullable:   true
        cSynonymCd          nullable:   true
        cTotalnum           nullable:   true
        cVisualattributes   maxSize:    3
        fullName            maxSize:    700
        name                maxSize:    2000
        tableCode           maxSize:    50
        tableName           maxSize:    50
        tooltip             nullable:   true,   maxSize:   900
        valuetypeCd         nullable:   true,   maxSize:   50

        AbstractQuerySpecifyingType.constraints.delegate = delegate
        AbstractQuerySpecifyingType.constraints()
	}

    static List<OntologyTerm> getCategories(boolean showHidden = false, boolean showSynonyms = false) {
        withCriteria {
            if (!showHidden) {
                not { like 'cVisualattributes', '_H%' }
            }
            if (!showSynonyms) {
                eq 'cSynonymCd', 'N' as char
            }
        }
    }

    Class getOntologyTermDomainClassReferred() {
        def domainClass = Holders.getGrailsApplication().domainClasses.find {
                    AbstractI2b2Metadata.isAssignableFrom(it.clazz) &&
                            tableName.equalsIgnoreCase(it.clazz.backingTable)
                }
        domainClass?.clazz
    }

    ConceptKey getConceptKey() {
        new ConceptKey(tableCode, fullName)
    }

    String getKey() {
        conceptKey.toString()
    }

    EnumSet<VisualAttributes> getVisualAttributes() {
        VisualAttributes.forSequence(cVisualattributes)
    }

    def getMetadata() {
        null /* no metadata on categories supported */
    }

    boolean isSynonym() {
        cSynonymCd != 'Y'
    }

    List<OntologyTerm> getChildren(boolean showHidden = false, boolean showSynonyms = false) {
        getDescendants(false, showHidden, showSynonyms)
    }

    List<OntologyTerm> getAllDescendants(boolean showHidden = false, boolean showSynonyms = false) {
        getDescendants(true, showHidden, showSynonyms)
    }

    private List<OntologyTerm> getDescendants(boolean allDescendants, boolean showHidden = false, boolean showSynonyms = false) {

        HibernateCriteriaBuilder c

        /* extract table code from concept key and resolve it to a table name */
        c = createCriteria()
        String tableName = c.get {
            projections {
                distinct('tableName')
            }
            eq('tableCode', conceptKey.tableCode)
        }

        /* validate this table name */
        def domainClass = ontologyTermDomainClassReferred
        if (!domainClass) {
            throw new RuntimeException("Metadata table ${tableName} is not mapped")
        }

        /* select level on the original table (is this really necessary?) */
        c = domainClass.createCriteria()
        Integer parentLevel = c.get {
            projections {
                property 'level'
            }

            and {
                eq 'fullName', fullName
                eq 'cSynonymCd', 'N' as char
            }
        }
        if (parentLevel == null) {
            throw new RuntimeException("Could not determine parent's level; could not find it in " + domainClass.name +
		"'s table (fullname: " + fullName + ")")
	}

        /* Finally select the relevant stuff */
        String fullNameSearch = Utils.asLikeLiteral(fullName) + '%'

        c = domainClass.createCriteria()
        c.list {
            and {
                like 'fullName', fullNameSearch
                if (allDescendants) {
                    gt 'level', parentLevel
                }
                else {
                    eq 'level', parentLevel + 1
                }

                if (!showHidden) {
                    not { like 'cVisualattributes', '_H%' }
                }
                if (!showSynonyms) {
                    eq 'cSynonymCd', 'N' as char
                }
            }
            order('name')
        }
    }

    Study getStudy() {
        /* never has an associated tranSMART study;
         * in tranSMART table access will only have 'Public Studies' and
         * 'Private Studies' nodes */
        null
    }

    @Override
    List<Patient> getPatients() {
        super.getPatients(this)
    }

    String toString() {
        getClass().canonicalName + "[${attached?'attached':'not attached'}] [ fullName=$fullName ]"
    }
}

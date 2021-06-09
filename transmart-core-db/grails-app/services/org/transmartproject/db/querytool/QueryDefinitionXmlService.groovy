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

package org.transmartproject.db.querytool

import groovy.xml.MarkupBuilder
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.ConstraintByOmicsValue
import org.transmartproject.core.querytool.ConstraintByValue
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryDefinitionXmlConverter

/**
 * Handles conversions of {@link org.transmartproject.core.querytool
 * .QueryDefinition}s to and from XML strings, as they are stored in
 * qtm_query_master.
 */
class QueryDefinitionXmlService implements QueryDefinitionXmlConverter {

    private static final String XMLNS_QD = 'http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/'

    static transactional = false

    QueryDefinition fromXml(Reader reader) throws InvalidRequestException {
        def xml
        try {
            xml = new XmlSlurper().parse(reader)
        }
        catch (e) {
            throw new InvalidRequestException('Malformed XML document: ' + e.message, e)
        }

        def convertItem = { item ->
            def data = [ conceptKey: item.item_key ]
            if (item.constrain_by_value.size()) {
                try {
                    def constrain = item.constrain_by_value
                    data.constraint = new ConstraintByValue(
                            valueType: ConstraintByValue.ValueType.valueOf(constrain.value_type?.toString()),
                            operator: ConstraintByValue.Operator.forValue(constrain.value_operator.toString()),
                            constraint: constrain.value_constraint?.toString()
                    )
                }
                catch (e) {
                    throw new InvalidRequestException('Invalid XML query definition constraint', e)
                }
            }

            if (item.constrain_by_omics_value.size()) {
                try {
                    def constrain = item.constrain_by_omics_value
                    data.constraintByOmicsValue = new ConstraintByOmicsValue(
                            omicsType: ConstraintByOmicsValue.OmicsType.forValue(constrain.omics_value_type?.toString()),
                            operator: ConstraintByOmicsValue.Operator.forValue(constrain.omics_value_operator.toString()),
                            projectionType: constrain.omics_projection_type?.toString(),
                            selector: constrain.omics_selector?.toString(),
                            property: constrain.omics_property?.toString(),
                            constraint: constrain.omics_value_constraint?.toString()
                    )
                }
                catch (e) {
                    throw new InvalidRequestException('Invalid XML query definition highdimension value constraint', e)
                }
                if (!Projection.prettyNames.keySet().contains(data.constraintByOmicsValue.projectionType)) {
                    throw new InvalidRequestException('Invalid projection type in highdimension value constraint: ' +
						      data.constraintByOmicsValue.projectionType +
						      '. Expected one of ' + Projection.prettyNames.keySet() + '.')
		}
            }

            new Item(data)
        }

        List<Panel> panels = xml.panel.collect { panel ->
            new Panel(invert: panel.invert == '1', items: panel.item.collect(convertItem))
        }

        if (xml.query_name.size()) {
            new QueryDefinition(xml.query_name.toString(), panels)
        }
        else {
            new QueryDefinition(panels)
        }
    }

    String toXml(QueryDefinition definition) {
        Writer writer = new StringWriter()

        /* this XML document is invalid in quite some respects according to
         * the schema, but:
         * 1) that's a subset of what tranSMART used in its requests to CRC
         * 2) i2b2 accepts these documents (not that it matters a lot at this
         * point, since we're not using i2b2's runtime anymore)
         * 3) the schema does not seem correct in many respects; several
         * elements that are supposed to be optional are actually required.
         *
         * It's possible the schema is only used to generate Java classes
         * using JAXB and that there's never any validation against the schema
         */
	new MarkupBuilder(writer).'qd:query_definition'('xmlns:qd': XMLNS_QD) {
	    query_name definition.name

	    for (Panel panelArg in definition.panels) {
		panel {
		    invert panelArg.invert ? '1' : '0'
		    for (Item itemArg in panelArg.items) {
			item {
			    item_key itemArg.conceptKey

			    if (itemArg.constraint) {
				constrain_by_value {
				    value_operator itemArg.constraint.operator.value
				    value_constraint itemArg.constraint.constraint
				    value_type itemArg.constraint.valueType.name()
				}
			    }

			    if (itemArg.constraintByOmicsValue) {
				ConstraintByOmicsValue constraint = itemArg.constraintByOmicsValue
				constrain_by_omics_value {
				    omics_value_operator constraint.operator.value
				    omics_value_constraint constraint.constraint
				    omics_value_type constraint.omicsType.value
				    omics_selector constraint.selector
				    omics_property constraint.property
				    omics_projection_type constraint.projectionType
				}
			    }
			}
		    }
		}
	    }
	}

        writer
    }
}

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

package org.transmartproject.db.dataquery.clinical

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
//import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
//import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.ScrollableResults
//import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.ApplicationContext
//import org.springframework.context.ApplicationContextAware
//import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.db.dataquery.CollectingTabularResult
import org.transmartproject.db.dataquery.clinical.variables.TerminalClinicalVariable
import org.transmartproject.db.dataquery.clinical.variables.TerminalConceptVariable

//@Component
@CompileStatic
@Slf4j('logger')
class TerminalClinicalVariablesTabularResult extends
        CollectingTabularResult<TerminalClinicalVariable, PatientIdAnnotatedDataRow> {

    public static final String TEXT_VALUE_TYPE = 'T'

    public static final int PATIENT_NUM_COLUMN_INDEX  = 0
    public static final int CODE_COLUMN_INDEX         = 1
    public static final int VALUE_TYPE_COLUMN_INDEX   = 2
    public static final int TEXT_VALUE_COLUMN_INDEX   = 3
    public static final int NUMBER_VALUE_COLUMN_INDEX = 4

//    @Autowired
//    GrailsApplication grailsApplication

    @Value('${org.transmart.i2b2.view.enable:false}')
    private boolean i2b2View

//    GrailsApplication grailsApp = Holders.grailsApplication
//    ApplicationContext ctx = grailsApp.config
//    Boolean i2b2ViewAlt = ctx.org.transmart.i2b2.view.enable ? ctx.org.transmart.i2b2.view.enable :false

//    Boolean i2b2ViewAltB = grailsApplication.config.org.transmart.i2b2.view.enable ? grailsApplication.config.org.transmart.i2b2.view.enable :false

    /* XXX: this class hierarchy needs some refactoring, we're depending on
     * implementation details of CollectingTabularResults and skipping quite
     * some logic from it (see below the assignment for allowMissingColumn and
     * the overriding of finalizeCollectedEntries()).
     * Adding a new superclass for CollectingTabularResults and extending that
     * instead would be a simple (but perhaps not very elegant) solution.
     */

    BiMap<TerminalClinicalVariable, Integer> localIndexMap = HashBiMap.create()

    // variant of above map with variables replaced with their concept code
    private Map<String, Integer> codeToIndex = [:]

    final String variableGroup

    TerminalClinicalVariablesTabularResult(ScrollableResults results,
                                           List<TerminalClinicalVariable> indicesList) {
        this.results = results
	this.indicesList = indicesList

        for (TerminalClinicalVariable index in indicesList) {
            localIndexMap[index] = indicesList.indexOf(index)
        }

        localIndexMap.each { TerminalClinicalVariable var, Integer index ->
//	    logger.info 'codeToIndex[{}] = {}', var.code, index
            codeToIndex[var.code] = index
        }

	if (!indicesList) {
	    throw new InvalidArgumentsException("Indices list is empty")
        }

        Collection<String> groups = indicesList*.group.unique()
        if (groups.size() != 1) {
	    throw new InvalidArgumentsException("Expected all the clinical " +
						"variables in this sub-result to have the same type, " +
						"found these: $groups")
        }
        variableGroup = groups[0]

        columnsDimensionLabel = 'Clinical Variables'
        rowsDimensionLabel    = 'Patients'
        // actually yes, but this skips the complex logic in addToCollectedEntries() and just adds the row to the list
        allowMissingColumns   = false

	columnIdFromRow = { Object[] row -> row[CODE_COLUMN_INDEX] }
	inSameGroup = { Object[] row1, Object[] row2 ->
            row1[PATIENT_NUM_COLUMN_INDEX] == row2[PATIENT_NUM_COLUMN_INDEX]
        }

        finalizeGroup = this.&finalizePatientGroup

	// session is managed outside, in ClinicalDataTabularResult
        closeSession = false
    }

    final String columnEntityName = 'concept'

    protected getIndexObjectId(TerminalConceptVariable object) {
        object.conceptCode
    }

    protected void finalizeCollectedEntries(List collectedEntries) {
	// nothing to do here. All the logic in finalizePatientGroup
    }

    private PatientIdAnnotatedDataRow finalizePatientGroup(List<Object[]> list) {
        Map<Integer, TerminalClinicalVariable> indexToColumn = localIndexMap.inverse()

        Object[] transformedData = new Object[localIndexMap.size()]

//	logger.info 'transformedData {}', transformedData

        for (Object[] rawRow in list) {
	    // array with 5 elements
            if (!rawRow) {
		continue
            }

//	    logger.info 'rawRow {}', rawRow

	    // find out the position of this concept in the final result
            Integer index = codeToIndex[rawRow[CODE_COLUMN_INDEX] as String]
            if (index == null) {
		throw new IllegalStateException("Unexpected concept code " +
						"'${rawRow[CODE_COLUMN_INDEX]}' at this point; " +
						"expected one of ${codeToIndex.keySet()}")
            }

	    // and the corresponding variable
            TerminalClinicalVariable var = indexToColumn[index]

            if (transformedData[index] != null) {
//		logger.info 'More than one clinical result i2b2View {} index {} result size {}', i2b2View, index, transformedData.size()
		if(!i2b2View && 0) {
		    throw new UnexpectedResultException("Got more than one fact for " +
							"patient ${rawRow[PATIENT_NUM_COLUMN_INDEX]} and " +
							"code $var.code. This is currently unsupported in tranSMART")
		}
            }
	    else {
		transformedData[index] = getVariableValue(rawRow)
	    }
        }

//	logger.info 'patientId {}', (list.find { it != null})[PATIENT_NUM_COLUMN_INDEX] as Long
//	logger.info 'data: {}', Arrays.asList(transformedData)
//	logger.info 'columnToIndex: {}', localIndexMap as Map

        new PatientIdAnnotatedDataRow(
            patientId:     (list.find { it != null})[PATIENT_NUM_COLUMN_INDEX] as Long,
	    data: Arrays.asList(transformedData),
            columnToIndex: localIndexMap as Map
	)
    }

    private getVariableValue(Object[] rawRow) {
        String valueType = rawRow[VALUE_TYPE_COLUMN_INDEX]

        if (valueType == TEXT_VALUE_TYPE) {
            rawRow[TEXT_VALUE_COLUMN_INDEX]
        }
        else {
            rawRow[NUMBER_VALUE_COLUMN_INDEX]
        }
    }
}

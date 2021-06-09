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

import groovy.util.logging.Slf4j
import org.transmart.plugin.shared.Utils
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.querytool.ConstraintByOmicsValue
import org.transmartproject.core.querytool.ConstraintByValue
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService
import org.transmartproject.db.ontology.MetadataSelectQuerySpecification
import org.transmartproject.db.support.DatabasePortabilityService
import org.transmartproject.db.user.User

import java.util.regex.Matcher

import static org.transmartproject.core.querytool.ConstraintByValue.Operator.BETWEEN
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.EQUAL_TO
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.GREATER_OR_EQUAL_TO
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.GREATER_THAN
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.LOWER_OR_EQUAL_TO
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.LOWER_THAN
import static org.transmartproject.db.support.DatabasePortabilityService.DatabaseType.ORACLE

@Slf4j('logger')
class PatientSetQueryBuilderService {

    static transactional = false

    ConceptsResource conceptsResourceService
    DatabasePortabilityService databasePortabilityService
    HighDimensionResourceService highDimensionResourceService

    String buildPatientIdListQuery(QueryDefinition definition, User user = null) throws InvalidRequestException {

        generalDefinitionValidation definition

        int panelNum = 1
        List<Map> panelClauses = definition.panels.collect { Panel panel ->

            def itemPredicates = panel.items.collect { Item item ->
                OntologyTerm term
                try {
                    term = conceptsResourceService.getByKey(item.conceptKey)
                    if (!(term instanceof MetadataSelectQuerySpecification)) {
                        throw new InvalidArgumentsException("Ontology term with key $item.conceptKey does not specify a query")
                    }
                }
                catch (NoSuchResourceException nsr) {
                    throw new InvalidRequestException("No such concept key: $item.conceptKey", nsr)
                }

                doItem term, item, user
            }

            /*
             * itemPredicates are similar to this example:
             * concept_cd IN
             *  (SELECT concept_cd FROM concept_dimension WHERE concept_path
             *  LIKE '\\...\%')
             *  AND (
             *      (valtype_cd = 'N' AND nval_num > 50 AND tval_char IN
             *          ('E', 'GE'))
             *      OR
             *      (valtype_cd = 'N' AND nval_num >= 50 AND tval_char = 'G')
             * )
             */
            String bigPredicate = itemPredicates.collect { '(' + it + ')' }.join(' OR ')

            if (panel.items.size() > 1) {
                bigPredicate = '(' + bigPredicate + ')'
            }

            [id: panelNum++,
             select: "SELECT patient_num FROM I2B2DEMODATA.observation_fact WHERE $bigPredicate AND concept_cd != 'SECURITY'",
             invert: panel.invert]
        }.sort { a, b ->
            (a.invert && !b.invert) ? 1
                    : (!a.invert && b.invert) ? -1
                    : (a.id - b.id)
        }

        if (panelClauses.size() == 1) {
            Map panel = panelClauses[0]
            if (!panel.invert) {
                /* The intersect/expect is not enough for deleting duplicates
                 * because there is only one select; we must add a group by */
                panel.select + ' GROUP BY patient_num'
            }
            else {
                'SELECT patient_num FROM I2B2DEMODATA.patient_dimension ' +
                        databasePortabilityService.complementOperator + ' (' + panel.select + ')'
            }
        }
        else {
            panelClauses.inject('') { String acc, panel ->
                acc + (acc.empty
                       ? ''
                       : panel.invert
                       ? " $databasePortabilityService.complementOperator "
                       : ' INTERSECT ') +
                    '(' + panel.select + ')'
            }
        }
    }

    String buildPatientSetQuery(QtmQueryResultInstance resultInstance, QueryDefinition definition,
                                User user = null) throws InvalidRequestException {

        if (!resultInstance.id) {
            throw new RuntimeException('QtmQueryResultInstance has not been persisted')
        }

        String patientSubQuery = buildPatientIdListQuery(definition, user)

        //$patientSubQuery has result set with single column: 'patient_num'
        String windowFunctionOrderBy = ''
        if (databasePortabilityService.databaseType == ORACLE) {
            //Oracle requires this, PostgreSQL supports it, and H2 rejects it
            windowFunctionOrderBy = 'ORDER BY patient_num'
        }

        String sql = 'INSERT INTO I2B2DEMODATA.qtm_patient_set_collection (result_instance_id, patient_num, set_index) ' +
                "SELECT ${resultInstance.id}, P.patient_num, " +
                " row_number() OVER ($windowFunctionOrderBy) " +
                "FROM ($patientSubQuery ORDER BY 1) P"

        logger.debug 'SQL statement: {}', sql

        sql
    }

    /* Mapping between the number value constraint and the SQL predicates. The
     * value constraint may correspond to one or two SQL predicates ORed
     * together */
    private static final Map<ConstraintByValue.Operator, List> NUMBER_QUERY_MAPPING = [
            (LOWER_THAN):          [['<',  ['E', 'LE']], ['<=', ['L']]],
            (LOWER_OR_EQUAL_TO):   [['<=', ['E', 'LE', 'L']]],
            (EQUAL_TO):            [['=',  ['E']]],
            (BETWEEN):             [['BETWEEN', ['E']]],
            (GREATER_THAN):        [['>',  ['E', 'GE']], ['>=', ['G']]],
            (GREATER_OR_EQUAL_TO): [['>=', ['E', 'GE', 'G']]]
    ]

    private String doItem(MetadataSelectQuerySpecification term, Item item, User user) {
        /* constraint represented by the ontology term */
        String clause = generateObservationFactConstraint(user, term)
        /* additional (and optional) constraint by value */
        ConstraintByValue constraint = item.constraint
        ConstraintByOmicsValue omicsValueConstraint = item.constraintByOmicsValue
        if (constraint) {
            if (constraint.valueType == ConstraintByValue.ValueType.NUMBER) {
                List spec = NUMBER_QUERY_MAPPING[constraint.operator]
                String constraintValue = doConstraintNumber(constraint.operator, constraint.constraint)
                def predicates = spec.collect {
                    "valtype_cd = 'N' AND nval_num ${it[0]} $constraintValue AND " +
                            'tval_char ' + (it[1].size() == 1
                            ? "= '${it[1][0]}'"
                            : "IN (${it[1].collect { "'$it'" }.join ', '})")
                }

                clause += ' AND (' + predicates.collect { '(' + it + ')' }.join(' OR ') + ')'
            }
            else if (constraint.valueType == ConstraintByValue.ValueType.FLAG) {
                clause += " AND (valueflag_cd = ${doConstraintFlag(constraint.constraint)})"
            }
        }

        if (omicsValueConstraint) {
            HighDimensionDataTypeResource resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(item.conceptKey)
            if (resource) {
                Map distribution = resource.getDistribution(omicsValueConstraint, item.conceptKey, null)
                clause += 'AND patient_num IN (' + distribution.keySet().collect {it.toString()}.join(',') + ')'
            }
            else {
                logger.warn 'No implementation exists for building a patient set query for {} data.',
		    resource.highDimensionFilterType
            }
        }
        clause
    }

    private String doConstraintNumber(ConstraintByValue.Operator operator,
				      String value) throws NumberFormatException, InvalidRequestException {

        /* validate constraint value to prevent injection */
        try {
            if (operator == BETWEEN) {
                Matcher matcher = value =~
                        /([+-]?[0-9]+(?:\.[0-9]*)?)(?i: and )([+-]?[0-9]+(?:\.[0-9]*)?)/
                if (matcher.matches()) {
                    return Double.parseDouble(matcher.group(1).toString()) +
                            ' AND ' +
                            Double.parseDouble(matcher.group(2).toString())
                }
            }
            else {
                if (value =~ /[+-]?[0-9]+(?:\.[0-9]*)?/) {
                    return Double.parseDouble(value).toString()
                }
            }
        }
        catch (NumberFormatException e) {
            /* may fail because the number is too large, for instance.
             * We'd rather fail here than failing when the SQL statement is
             * compiled. */
            throw new InvalidRequestException('Error parsing constraint value: ' + e.message, e)
        }

        throw new InvalidRequestException("The value '" + value +
                "' is an invalid number constraint value for the operator " + operator)
    }

    private String doConstraintFlag(String value) throws InvalidRequestException {

        if (['L', 'H', 'N'].contains(value)) {
            return "'$value'"
        }

        throw new InvalidRequestException("A flag value constraint's operand must be either 'L', 'H' or 'N'; got '$value'")
    }

    private void generalDefinitionValidation(QueryDefinition definition) {
        if (!definition.panels) {
            throw new InvalidRequestException('No panels were specified')
        }

        if (definition.panels.any { Panel p -> !p.items }) {
            throw new InvalidRequestException('Found panel with no items')
        }

        def anyItem = { Closure c ->
            definition.panels.any { Panel p ->
                p.items.any { Item item ->
                    c(item)
                }
            }
        }
        if (anyItem { it == null }) {
            throw new InvalidRequestException('Found panel with null value in its item list')
        }
        if (anyItem { Item it -> it.conceptKey == null }) {
            throw new InvalidRequestException('Found item with null conceptKey')
        }
        if (anyItem { it.constraint && it.constraint.constraint == null }) {
            throw new InvalidRequestException('Found item constraint with null constraint value')
        }
        if (anyItem { it.constraint && it.constraint.operator == null }) {
            throw new InvalidRequestException('Found item constraint with null operator')
        }
        if (anyItem { it.constraint && it.constraint.valueType == null }) {
            throw new InvalidRequestException('Found item constraint with null value type')
        }
        if (anyItem { Item it ->
	    it.constraint &&
		it.constraint.valueType == ConstraintByValue.ValueType.FLAG &&
                it.constraint.operator != EQUAL_TO
	}) {
            throw new InvalidRequestException('Found item flag constraint with an operator different from EQUAL_TO')
        }
    }

    private String getProcessedDimensionCode(MetadataSelectQuerySpecification spec) {
        def v = spec.dimensionCode
        if (!v) {
            return v
        }

        if (spec.columnDataType == 'T' && v.length() > 2) {
            if (spec.operator.equalsIgnoreCase('like')) {
                if (v[0] != "'" && !v[0] != '(') {
                    if (v[-1] != '%') {
                        if (v[-1] != '\\') {
                            v += '\\'
                        }
                        v = Utils.asLikeLiteral(v) + '%'
                    }
                }
            }

            if (v[0] != "'") {
                v = v.replaceAll(/'/, "''") /* escape single quotes */
                v = "'$v'"
            }

        }

        if (spec.operator.equalsIgnoreCase('in')) {
            v = "($v)"
        }

        v
    }

    String generateObservationFactConstraint(User userInContext, MetadataSelectQuerySpecification spec) {
        String res = "SELECT $spec.factTableColumn " +
				"FROM $spec.dimensionTableName " +
				"WHERE $spec.columnName $spec.operator ${getProcessedDimensionCode(spec)}"
        if (spec.operator.equalsIgnoreCase('like') &&
                databasePortabilityService.databaseType == ORACLE) {
            res += " ESCAPE '\\'"
        }

        spec.postProcessQuery "$spec.factTableColumn IN ($res)", userInContext
    }
}

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

import grails.transaction.Transactional
import groovy.sql.Sql
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.Assert
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.querytool.QueryStatus
import org.transmartproject.db.user.User
import org.transmartproject.db.user.UsersResourceService

import javax.sql.DataSource

@CompileStatic
@Slf4j('logger')
class QueriesResourceService implements QueriesResource {

    @Value('${org.transmartproject.i2b2.group_id:}')
    private String i2b2GroupId

    @Value('${org.transmartproject.i2b2.user_id:}')
    private String i2b2UserId

    @Autowired private DataSource dataSource
    @Autowired private PatientSetQueryBuilderService patientSetQueryBuilderService
    @Autowired private QueryDefinitionXmlService queryDefinitionXmlService
    @Autowired private SessionFactory sessionFactory
    @Autowired private UsersResourceService usersResourceService

    @Deprecated
    QueryResult runQuery(QueryDefinition definition) throws InvalidRequestException {
        runQuery definition, i2b2UserId
    }

    @Transactional
    QueryResult runQuery(QueryDefinition definition, String username) throws InvalidRequestException {
        // 1. Populate qt_query_master
        QtQueryMaster queryMaster = new QtQueryMaster(
            name           : definition.name,
	    userId         : username,
            groupId        : i2b2GroupId,
            createDate     : new Date(),
            requestXml     : queryDefinitionXmlService.toXml(definition))

        // 2. Populate qt_query_instance
        QtQueryInstance queryInstance = new QtQueryInstance(
                userId       : username,
                groupId      : i2b2GroupId,
                startDate    : new Date(),
                statusTypeId : QueryStatus.PROCESSING.id,
                queryMaster  : queryMaster)
        addToQueryInstances queryMaster, queryInstance

        // 3. Populate qt_query_result_instance
        QtQueryResultInstance resultInstance = new QtQueryResultInstance(
                statusTypeId  : QueryStatus.PROCESSING.id,
                startDate     : new Date(),
                queryInstance : queryInstance)
        addToQueryResults queryInstance, resultInstance

        // 4. Save the three objects
        if (!queryMaster.validate()) {
            throw new InvalidRequestException('Could not create a valid QtQueryMaster: ' + queryMaster.errors)
        }
        if (!queryMaster.save()) {
            throw new RuntimeException('Failure saving QtQueryMaster')
        }

        // 5. Flush session so objects are inserted & raw SQL can access them
        sessionFactory.currentSession.flush()

        // 6. Build the patient set
        long setSize
	String sqlString = '<NOT BUILT>'
	Sql sql = new Sql(dataSource)
        try {
	    sql.execute 'SAVEPOINT doWork'

	    sqlString = patientSetQueryBuilderService.buildPatientSetQuery(
		resultInstance, definition, tryLoadingUser(username))

	    queryMaster.generatedSql = sqlString

	    setSize = sql.executeUpdate(sqlString)

	    logger.debug 'Inserted {} rows into qt_patient_set_collection', setSize
        }
        catch (InvalidRequestException e) {
            logger.error 'Invalid request; rolling back transaction', e
            throw e /* unchecked; rolls back transaction */
        }
        catch (e) {
            // 6e. Handle error when building/running patient set query
            logger.error 'Error running (or building) querytool SQL query, failing query was "{}"', sqlString, e

            // Rollback to save point
            sql.executeUpdate 'ROLLBACK TO SAVEPOINT doWork'

            StringWriter sw = new StringWriter()
            e.printStackTrace new PrintWriter(sw, true)

            resultInstance.setSize = resultInstance.realSetSize = -1L
            resultInstance.endDate = new Date()
	    resultInstance.statusTypeId = (short) QueryStatus.ERROR.id
            resultInstance.errorMessage = sw.toString()

            queryInstance.endDate = new Date()
	    queryInstance.statusTypeId = QueryStatus.ERROR.id
            queryInstance.message = sw.toString()

            if (!resultInstance.save()) {
                logger.error 'After exception from patientSetQueryBuilderService::buildService,' +
						' failed saving updated resultInstance and queryInstance'
            }
            return resultInstance
        }

        // 7. Update result instance and query instance
        resultInstance.setSize = resultInstance.realSetSize = setSize
        resultInstance.description = "Patient set for \"${definition.name}\""
        resultInstance.endDate = new Date()
        resultInstance.statusTypeId = (short) QueryStatus.FINISHED.id

        queryInstance.endDate = new Date()
        queryInstance.statusTypeId = QueryStatus.COMPLETED.id

        if (!resultInstance.save()) {
            throw new RuntimeException('Failure saving resultInstance after ' +
                    'successfully building patient set. Errors: ' + resultInstance.errors)
        }

        // 8. Return result instance
        resultInstance
    }

    QueryResult getQueryResultFromId(Long id) throws NoSuchResourceException {
	QtQueryResultInstance qtQueryResultInstance = QtQueryResultInstance.get(id)
	if (qtQueryResultInstance) {
	    qtQueryResultInstance
	}
	else {
	    throw new NoSuchResourceException('Could not find query result instance with id ' + id)
	}
    }

    QueryDefinition getQueryDefinitionForResult(QueryResult result) throws NoSuchResourceException {
	List<String> requestXmls = QtQueryResultInstance.executeQuery('''
				SELECT R.queryInstance.queryMaster.requestXml
				FROM QtQueryResultInstance R WHERE R = ?''', [result]) as List<String>
	    if (!requestXmls) {
	    throw new NoSuchResourceException(
		'Could not find definition for query result with id=' + result.id)
	}

	queryDefinitionXmlService.fromXml new StringReader(requestXmls[0])
    }

    /**
     * This doesn't fail if the user doesn't exist. This is for historical
     * reasons. The user associated with the query used to be an I2B2 user,
     * not a tranSMART user. This lax behavior is to allow core-db to work
     * under this old assumption (useful only for interoperability with
     * i2b2). Though, arguably, this should not be supported in transmart
     * as across trials queries and permission checks will fail if the
     * user is not a tranSMART user. Log a warning.
     */
    User tryLoadingUser(String username) {
	Assert.hasLength username, 'Username not provided'

	try {
	    (User) usersResourceService.getUserFromUsername(username)
	}
	catch (NoSuchResourceException ignored) {
	    logger.warn 'User {} not found. This is permitted for compatibility with i2b2, ' +
		'but tranSMART functionality will be degraded, and this behavior is deprecated', username
	    return null
	}
    }

    @CompileDynamic
    private void addToQueryInstances(QtQueryMaster queryMaster, QtQueryInstance queryInstance) {
	queryMaster.addToQueryInstances queryInstance
    }

    @CompileDynamic
    private void addToQueryResults(QtQueryInstance queryInstance, QtQueryResultInstance resultInstance) {
	queryInstance.addToQueryResults resultInstance
    }
}

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

import com.google.common.collect.Iterables
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.querytool.QueryStatus
import org.transmartproject.db.i2b2data.PatientDimension

class QueryResultData {

	static QtmQueryMaster createQueryResult(List<PatientDimension> patients) {
		QtmQueryMaster queryMaster = new QtmQueryMaster(
				name: 'test-fake-query-1',
				userId: 'fake-user',
				groupId: 'fake group',
				createDate: new Date(),
				requestXml: '')

		QtmQueryInstance queryInstance = new QtmQueryInstance(
				userId: 'fake-user',
				groupId: 'fake group',
				startDate: new Date(),
				statusTypeId: QueryStatus.COMPLETED.id,
				queryMaster: queryMaster)
		queryMaster.addToQueryInstances queryInstance

		QtmQueryResultInstance resultInstance = new QtmQueryResultInstance(
				statusTypeId: QueryStatus.FINISHED.id,
				startDate: new Date(),
				queryInstance: queryInstance,
				setSize: patients.size(),
				realSetSize: patients.size())
		queryInstance.addToQueryResults resultInstance

		int i = 0
		for (PatientDimension patient in patients) {
			resultInstance.addToPatientSet new QtmPatientSetCollection(setIndex: i++, patient: patient)
		}

		queryMaster
	}

	static QueryResult getQueryResultFromMaster(QtmQueryMaster master) {
		def f = { Iterables.getFirst it, null }
		f(f(master.queryInstances).queryResults)
	}
}

package org.transmart.marshallers

import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.querytool.QueryStatus
import spock.lang.Specification

class QueryResultMarshallerSpec extends Specification {

	void 'basic test'() {
		when:
		QueryResult value = [
				getId          : { -> -1L },
				getResultTypeId: { -> 1L },
				getSetSize     : { -> 77L },
				getStatus      : { -> QueryStatus.FINISHED },
				getErrorMessage: { -> 'error message' },
				getUsername    : { -> 'bogus_user_name' }
		] as QueryResult

		Map<Object, Object> out = QueryResultConverter.convert(value)

		then:
		out.errorMessage == 'error message'
		out.id == -1L
		out.setSize == 77L
		out.status == QueryStatus.FINISHED
		!out.statusTypeId
		!out.description
		!out.username
	}
}

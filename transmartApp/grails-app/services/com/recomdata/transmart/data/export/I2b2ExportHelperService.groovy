package com.recomdata.transmart.data.export

import groovy.sql.Sql

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

class I2b2ExportHelperService {

    static transactional = false

    DataSource dataSource

    List<String> findStudyAccessions(result_instance_ids) {
        checkQueryResultAccess(*(result_instance_ids as List))

        def rids = []
        for (r in result_instance_ids) {
	    if (r?.trim()) {
		rids << 'CAST(' + r + ' AS numeric)'
            }
        }

	String sql = '''
			select DISTINCT b.TRIAL
			FROM i2b2demodata.QTM_PATIENT_SET_COLLECTION a
			INNER JOIN i2b2demodata.PATIENT_TRIAL b ON a.PATIENT_NUM=b.PATIENT_NUM
			WHERE RESULT_INSTANCE_ID IN(''' + rids.join(", ") + ')'
	List<String> trials = []
	new Sql(dataSource).eachRow sql, { row ->
	    trials << row.TRIAL
        }
	trials
    }
}

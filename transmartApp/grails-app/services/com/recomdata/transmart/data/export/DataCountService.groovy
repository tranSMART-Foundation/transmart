package com.recomdata.transmart.data.export

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.querytool.QueriesResource

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * @deprecated now only used for SNP, which is not in core-db
 */
@Deprecated
class DataCountService {

    static transactional = false

    DataSource dataSource
    @Autowired QueriesResource queriesResource

    //For the given list of Subjects get counts of what kind of data we have for those cohorts.
    //We want to return a map that looks like {"PLINK": "102","RBM":"28"}
    Map getDataCounts(Long rID, Long[] resultInstanceIds) {
        checkQueryResultAccess(*resultInstanceIds)

	// patient_num should be unique across all studies.
	String sql = '''
				SELECT count(distinct snp.patient_num)
				FROM DEAPP.de_subject_snp_dataset snp
				WHERE snp.patient_num IN (
					SELECT DISTINCT patient_num
					FROM I2B2DEMODATA.qt_patient_set_collection
					WHERE result_instance_id = ?
					  AND patient_num IN (
						select patient_num
						from I2B2DEMODATA.patient_dimension
						where sourcesystem_cd not like '%:S:%'
					)
				)'''

	[SNP: rID ? getCountFromDB(sql, String.valueOf(rID)) : 0]
    }

    /**
     * @return The number of patients within the given subset that have clinical data
     */
    long getClinicalDataCount(Long resultInstanceId) {
        // TODO: Convert this into using
	if (resultInstanceId) {
	    queriesResource.getQueryResultFromId(resultInstanceId).setSize
        }
        else {
	    0
	}
    }

    long getCountFromDB(String sql, String rID = null) {
	List params = []
	if (rID?.trim()) {
	    params << rID
	}

	new Sql(dataSource).firstRow(sql, params)[0]
    }
}

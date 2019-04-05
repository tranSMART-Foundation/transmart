package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

@Slf4j('logger')
class AdditionalDataService {

    private static final char separator = '\t'

    static transactional = false

    DataSource dataSource
    GeneExpressionDataService geneExpressionDataService

    List findAdditionalDataFiles(String resultInstanceId, studyList) {
        checkQueryResultAccess resultInstanceId

	Sql sql = new Sql(dataSource)
        def filesList = []

        String studies = geneExpressionDataService.convertList(studyList, false, 1000)

        try {
            def additionalFilesQuery = """
				select * from BIOMART.bio_content b where exists (
				  select distinct s.sample_cd from DEAPP.de_subject_sample_mapping s
				  where s.trial_name in ${studies} and patient_id in (
					SELECT DISTINCT sc.patient_num FROM I2B2DEMODATA.qt_patient_set_collection sc, I2B2DEMODATA.patient_dimension pd
					WHERE sc.result_instance_id = ? AND sc.patient_num = pd.patient_num
				  ) and s.sample_cd is not null and b.file_name like s.sample_cd||'%'
				)
				"""
	    filesList = sql.rows(additionalFilesQuery, [resultInstanceId])
        }
	catch (e) {
	    logger.error 'Problem finding Files for Additional Data :: {}', e.message
        }

	filesList
    }

    void downloadFiles(String resultInstanceId, studyList, File studyDir, String jobName) {
        def filesList = findAdditionalDataFiles(resultInstanceId, studyList)
	if (!filesList) {
	    logger.debug 'No Additional data files found to download'
	    return
	}

        for (file in filesList) {
	    String fileURL = file.CEL_LOCATION + file.FILE_NAME + file.CEL_FILE_SUFFIX
            FileWriterUtil writerUtil = new FileWriterUtil(studyDir, file.FILE_NAME, jobName, 'Additional_Files', null, separator)
	    writerUtil.writeFile fileURL, writerUtil.outputFile
        }
    }
}

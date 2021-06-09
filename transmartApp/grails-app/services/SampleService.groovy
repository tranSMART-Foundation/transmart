import groovy.sql.Sql
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

@CompileStatic
@Slf4j('logger')
class SampleService implements InitializingBean {

    static transactional = false

    @Autowired private DataSource dataSource
    @Autowired private I2b2HelperService i2b2HelperService
    @Autowired private GrailsApplication grailsApplication
    @Autowired private SolrService solrService

    private Map<String, String> sampleBreakdownMap

    //Populate the QTM_PATIENT_SAMPLE_COLLECTION table based on a result_instance_id.
    void generateSampleCollection(String resultInstanceId) {
	new Sql(dataSource).execute('''
			INSERT INTO I2B2DEMODATA.QTM_PATIENT_SAMPLE_COLLECTION (SAMPLE_ID, PATIENT_ID, RESULT_INSTANCE_ID)
			SELECT DISTINCT DSSM.SAMPLE_ID, DSSM.patient_id, ?
			FROM I2B2DEMODATA.QTM_PATIENT_SET_COLLECTION QTM
			INNER JOIN DEAPP.DE_SUBJECT_SAMPLE_MAPPING DSSM ON DSSM.PATIENT_ID = QTM.PATIENT_NUM
			WHERE RESULT_INSTANCE_ID = ?''',
			resultInstanceId.toInteger(), resultInstanceId.toInteger())
    }

    Map loadSampleStatisticsObject(String resultInstanceId) {

	StringWriter writer = new StringWriter()

	i2b2HelperService.renderQueryDefinition resultInstanceId, 'Query Definition', new PrintWriter(writer)

	Map<String, Object> sampleSummary = [:]

	sampleSummary.queryDefinition = writer.toString()

	sampleBreakdownMap.each { String key, String value ->
	    int count = solrService.getFacetCountForField(key, resultInstanceId, 'sample')
	    sampleSummary[value] = count
	    logger.debug 'Finished count for field {} - {}: {}', value, key, count
        }

	sampleSummary
    }

    @CompileDynamic
    void afterPropertiesSet() {
	sampleBreakdownMap = grailsApplication.config.edu.harvard.transmart.sampleBreakdownMap
    }
}

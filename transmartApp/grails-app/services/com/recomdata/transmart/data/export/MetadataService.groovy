package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil
import com.recomdata.transmart.util.FileDownloadService
import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Compound
import org.transmart.biomart.Experiment
import org.transmart.biomart.Taxonomy

import javax.sql.DataSource

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

@Slf4j('logger')
class MetadataService {

    private static final char separator = '\t'
    private static final String[] studyColumns = ['Title', 'Date', 'Owner', 'Institution', 'Country', 'Description',
	                                          'Access Type', 'Phase', 'Objective', 'BioMarker Type', 'Compound',
	                                          'Design Factor', 'Number of Patients', 'Organism', 'Target/Pathways']

    static transactional = false

    DataSource dataSource
    SpringSecurityService springSecurityService
    FileDownloadService fileDownloadService

    /**
     * Gather study data and write it to a file.
     *  The file will contain basic study metadata
     */
    void getData(File studyDir, String fileName, String jobName, List<String> studyAccessions) {
	logger.info 'loading study metadata for {}', studyAccessions
        // try to find it by Clinical Trial
	Map<String, String[]> studiesMap = [:]
	for (studyUid in studyAccessions) {
            // work around to fix the lazy loading issue - we don't have full transaction support there
	    def exp = ClinicalTrial.findByTrialNumber(studyUid) ?: Experiment.findByAccession(studyUid)
	    List<Taxonomy> organisms = Taxonomy.findAll(new Taxonomy(experiments: [exp]))
	    List<Compound> compounds = Compound.findAll(new Compound(experiments: [exp]))

	    if (exp) {
		studiesMap[studyUid] = getStudyData(exp, organisms, compounds)
            }
        }
	writeData studyColumns, studiesMap, studyDir, fileName, jobName
    }

    private void writeData(String[] studyCols, Map<String, String[]> studiesMap, File studyDir, String fileName, String jobName) {
	if (studiesMap) {
            def dataTypeName = null
            def dataTypeFolder = null
            FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator)

	    studyCols.eachWithIndex { String studyCol, int i ->
		List<String> lineVals = [studyCol]
		for (String[] studyData in studiesMap.values()) {
		    lineVals << studyData[i]
                }
                writerUtil.writeLine(lineVals as String[])
            }

            writerUtil.finishWriting()
        }
    }

    protected String[] getStudyData(Experiment exp, List<Taxonomy> organisms, List<Compound> compounds) {
	List<String> data = []
        if (exp instanceof ClinicalTrial) {
	    data << exp.title
	    data << exp.completionDate
	    data << exp.studyOwner
	    data << exp.institution
	    data << exp.country
	    data << exp.description
	    data << exp.accessType
	    data << exp.studyPhase
	    data << exp.design
	    data << exp.bioMarkerType
	    data << getCompoundNames(compounds)
	    data << exp.overallDesign
	    data << (exp.numberOfPatients as String)
	    data << getOrganismNames(organisms)
	    data << exp.target
        }
        else {
	    data << exp.title
	    data << (exp.completionDate as String)
	    data << exp.primaryInvestigator
	    data << exp.institution
	    data << exp.country
	    data << exp.description
	    data << exp.accessType
	    data << ''
	    data << exp.design
	    data << exp.bioMarkerType
	    data << getCompoundNames(compounds)
	    data << exp.overallDesign
	    data << ''
	    data << getOrganismNames(organisms)
	    data << exp.target
        }

	data
    }

    String getCompoundNames(List<Compound> compounds) {
        StringBuilder compoundNames = new StringBuilder()
	for (Compound c in compounds) {
	    if (c.name) {
		if (compoundNames) {
		    compoundNames << '; '
                }
		compoundNames << c.name
            }
        }
	compoundNames
    }

    String getOrganismNames(List<Taxonomy> organisms) {
        StringBuilder taxNames = new StringBuilder()
	for (Taxonomy t in organisms) {
	    if (t.label) {
		if (taxNames) {
		    taxNames << '; '
                }
		taxNames << t.label
            }
        }
	taxNames
    }

    List findAdditionalDataFiles(String resultInstanceId, String study) {
        checkQueryResultAccess resultInstanceId

	List filesList = []
        try {
	    String sql = '''
					select *
					from BIOMART.bio_content b
					where exists (
					   select distinct s.sample_cd
					   from DEAPP.de_subject_sample_mapping s
				  where s.trial_name = ? and patient_id in (
					      SELECT DISTINCT sc.patient_num
					      FROM I2B2DEMODATA.qt_patient_set_collection sc, I2B2DEMODATA.patient_dimension pd
					      WHERE sc.result_instance_id = ?
					        AND sc.patient_num = pd.patient_num
				)
					   and s.sample_cd is not null
					   and b.file_name like s.sample_cd||'%'
					)
			'''
	    logger.debug 'Study, ResultInstanceId :: {}, {}', study, resultInstanceId
	    filesList = new Sql(dataSource).rows(sql, [study, resultInstanceId])
        }
	catch (e) {
	    logger.error 'Problem finding Files for Additional Data :: {}', e.message
        }

	filesList
    }

    def downloadAdditionalDataFiles(String resultInstanceId, String study, File studyDir, String jobName) {
	List filesList = findAdditionalDataFiles(resultInstanceId, study)
	if (filesList) {
	    File additionalDataDir = new FileWriterUtil().createDir(studyDir, 'Additional_Data')

            def fileURLsList = []
            for (file in filesList) {
		fileURLsList << file.CEL_LOCATION + file.FILE_NAME + file.CEL_FILE_SUFFIX
            }

	    fileDownloadService.getFiles filesList, additionalDataDir.path
        }
        else {
	    logger.debug 'No Additional data files found to download'
        }
    }
}

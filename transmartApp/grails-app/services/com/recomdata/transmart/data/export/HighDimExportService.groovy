package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import com.recomdata.asynchronous.JobResultsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService
import org.transmartproject.db.ontology.OntologyTermTagsResourceService
import org.transmartproject.db.querytool.QueriesResourceService
import org.transmartproject.export.HighDimExporter
import org.transmartproject.export.HighDimExporterRegistry

@Slf4j('logger')
class HighDimExportService {

    static transactional = false

    private static final META_FILE_NAME = 'meta.tsv'
    private static final SAMPLES_FILE_NAME = 'samples.tsv'
    private static final PLATFORM_FILE_NAME = 'platform.tsv'
    private final static char COLUMN_SEPARATOR = '\t' as char
    private static final String[] META_FILE_HEADER = ['Attribute', 'Description'].asImmutable()
    private static final List<String> SAMPLE_FILE_HEADER = [
	'Assay ID', 'Subject ID', 'Sample Type',
	'Time Point', 'Tissue Type', 'Platform ID',
	'Sample Code'].asImmutable()

    static final List<String> PLATFORM_FILE_HEADER = [
	'Platform ID', 'Title', 'Genome Release ID',
	'Organism', 'Marker Type', 'Annotation Date'].asImmutable()

    ConceptsResource conceptsResourceService
    @Autowired private HighDimensionResourceService highDimensionResourceService
    @Autowired private HighDimExporterRegistry highDimExporterRegistry
    @Autowired private JobResultsService jobResultsService
    @Autowired private OntologyTermTagsResourceService ontologyTermTagsResourceService
    @Autowired private QueriesResourceService queriesResourceService

    /**
     * @param resultInstanceId  id of patient set for denoting patients for which export data for.
     * @param studyDir  directory where to store exported files
     * @param format  data file format (e.g. 'TSV', 'VCF'; see HighDimExporter.getFormat())
     * @param dataType  data format (e.g. 'mrna', 'acgh'; see HighDimensionDataTypeModule.getName())
     * @param jobName name of the current export job to check status whether we need to break export.
     * @param exportOptions map with boolean values. Specifies what kind of data to export.
     *                                  Possible keys:
     *                                  - meta - ontology tags data
     *                                  - samples - subject sample mapping
     *                                  - platform - platform information
     * @return exported files
     */
    List<File> exportHighDimData(String jobName, long resultInstanceId, List<String> conceptKeys,
	                         String dataType, String format, File studyDir,
	                         Map exportOptions = [:].withDefault { true } /*export everything by default*/) {

	logger.info 'Start a HD data export job: {}', jobName

	HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(
	    dataType)

	Collection<OntologyTerm> ontologyTerms
        if (conceptKeys) {
            ontologyTerms = conceptKeys.collectAll { conceptsResourceService.getByKey it }
        }
        else {
	    QueryResult queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
            ontologyTerms = dataTypeResource.getAllOntologyTermsForDataTypeBy(queryResult)
        }

	List<File> files = []
	for (OntologyTerm term in ontologyTerms) {
	    logger.info '[{}] Start export for a term: {}', jobName, term.key
            if (!jobResultsService.isJobCancelled(jobName)) {
		files.addAll exportForSingleNode(term, resultInstanceId, studyDir,
						 format, dataTypeResource, jobName, exportOptions)
            }
        }

        files.findAll()
    }

    List<File> exportForSingleNode(OntologyTerm term, Long resultInstanceId, File studyDir,
	                           String format, HighDimensionDataTypeResource dataTypeResource,
	                           String jobName, Map exportOptions) {

        if (jobResultsService.isJobCancelled(jobName)) {
            return []
        }

        List<File> outputFiles = []

	List<AssayConstraint> assayConstraints = [
            dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId),
            dataTypeResource.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: term.key)
        ]

	Set<HighDimExporter> exporters = highDimExporterRegistry.findExporters(
	    format, dataTypeResource.dataTypeName)

        if (!exporters) {
	    throw new RuntimeException('No exporter was found for ' + dataTypeResource.dataTypeName +
				       ' data type and ' + format + ' file format.')
        }

	if (exporters.size() > 1) {
	    logger.warn 'There are more than one exporter for {} data type and {} file format. Using first one: {}',
		dataTypeResource.dataTypeName, format, exporters[0]
        }

	HighDimExporter exporter = exporters[0]

        Projection projection = dataTypeResource.createProjection(exporter.projection)

	logger.debug '[job={} key={}] Retrieving the HD data for the term and a patient set: {}.',
	    jobName, term.key, resultInstanceId
        TabularResult<AssayColumn, DataRow> tabularResult =
            dataTypeResource.retrieveData(assayConstraints, [], projection)

        File nodeDataFolder = new File(studyDir, getRelativeFolderPathForSingleNode(term))
	logger.debug 'Create a node data folder: {}.', nodeDataFolder.path
        nodeDataFolder.mkdirs()

        try {
	    logger.debug '[job={} key={}] Export the HD data to the file.', jobName, term.key
	    exporter.export(tabularResult, projection,
			    { String dataFileName, String dataFileExt ->
                    File outputFile = new File(nodeDataFolder,
					       dataFileName + '_' + dataTypeResource.dataTypeName + '.' + dataFileExt.toLowerCase())
                    if (outputFile.exists()) {
			throw new RuntimeException(outputFile.path + ' file already exists.')
                    }

                    nodeDataFolder.mkdirs()
                    outputFiles << outputFile
		    logger.debug 'Inflating the data file: {}.', outputFile.path
                    outputFile.newOutputStream()
                },
			    { jobResultsService.isJobCancelled(jobName) })
        }
        catch (RuntimeException e) {
	    logger.error 'Data export to the file has thrown an exception', e
        }
        finally {
            tabularResult.close()
        }

        if (exportOptions.samples && !jobResultsService.isJobCancelled(jobName)) {
            if (tabularResult.indicesList) {
		logger.debug '[job={} key={}] Export the assays to the file.',
		    jobName, term.key
                outputFiles << exportAssays(tabularResult.indicesList, nodeDataFolder)
            }
        }

        if (exportOptions.platform && !jobResultsService.isJobCancelled(jobName)) {
            Set<Platform> platforms = tabularResult.indicesList*.platform
            if (platforms) {
		logger.debug '[job={} key={}] Export the platform to the file.',
		    jobName, term.key
                outputFiles << exportPlatform(platforms, nodeDataFolder)
            }
        }

        if (exportOptions.meta && !jobResultsService.isJobCancelled(jobName)) {
	    Map<OntologyTerm, List<OntologyTermTag>> tagsMap = ontologyTermTagsResourceService.getTags(
		[term] as Set, false)
            if (tagsMap && tagsMap[term]) {
		logger.debug '[job={} key={}] Export the tags to the file.', jobName, term.key
                outputFiles << exportMetaTags(tagsMap[term], nodeDataFolder)
            }
        }

        outputFiles
    }

    private File exportMetaTags(Collection<OntologyTermTag> tags, File parentFolder) {
	File metaTagsFile = new File(parentFolder, META_FILE_NAME)

        metaTagsFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
	    csvWriter.writeNext META_FILE_HEADER
	    for (OntologyTermTag tag in tags) {
                csvWriter.writeNext([tag.name, tag.description] as String[])
            }
        }

        metaTagsFile
    }

    private File exportAssays(Collection<Assay> assays, File parentFolder) {
	File samplesFile = new File(parentFolder, SAMPLES_FILE_NAME)

        samplesFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
	    csvWriter.writeNext SAMPLE_FILE_HEADER as String[]
	    for (Assay assay in assays) {
		csvWriter.writeNext([assay.id, assay.patientInTrialId, assay.sampleType.label,
				     assay.timepoint.label, assay.tissueType.label,
				     assay.platform.id, assay.sampleCode] as String[])
            }
        }

        samplesFile
    }

    private File exportPlatform(Collection<Platform> platforms, File parentFolder) {
	File platformFile = new File(parentFolder, PLATFORM_FILE_NAME)

        platformFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
            csvWriter.writeNext(PLATFORM_FILE_HEADER as String[])
	    for (Platform platform in platforms) {
		csvWriter.writeNext([platform.id, platform.title, platform.genomeReleaseId,
				     platform.organism, platform.markerType, platform.annotationDate] as String[])
            }
        }

        platformFile
    }

    private String getRelativeFolderPathForSingleNode(OntologyTerm term) {
	String leafConceptFullName = term.fullName
        String resultConceptPath = leafConceptFullName
	Study study = term.study
        if (study) {
	    String studyConceptFullName = study.ontologyTerm.fullName
            //use internal study folders only
            resultConceptPath = leafConceptFullName.replace(studyConceptFullName, '')
        }

        resultConceptPath.split('\\\\').findAll().collect { String folderName ->
            //Reversible way to encode a string to use as filename
            //http://stackoverflow.com/questions/1184176/how-can-i-safely-encode-a-string-in-java-to-use-as-a-filename
            URLEncoder.encode(folderName, 'UTF-8')
        }.join(File.separator)
    }
}

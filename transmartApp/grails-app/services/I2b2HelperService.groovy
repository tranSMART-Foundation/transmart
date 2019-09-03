import com.recomdata.db.DBHelper
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.export.GenePatternFiles
import com.recomdata.export.GwasFiles
import com.recomdata.export.SnpViewerFiles
import com.recomdata.export.SurvivalAnalysisFiles
import com.recomdata.export.SurvivalData
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import i2b2.Concept
import i2b2.GeneWithSnp
import i2b2.SnpDataByProbe
import i2b2.SnpDataset
import i2b2.SnpDatasetListByProbe
import i2b2.SnpInfo
import i2b2.SnpProbeSortedDef
import i2b2.StringLineReader
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.runtime.NullObject
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.classic.Session
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.util.Assert
import org.transmart.CohortInformation
import org.transmart.HeatmapValidator
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.Utils
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUserSecureAccess
import org.transmart.searchapp.SecureAccessLevel
import org.transmart.searchapp.SecureObjectPath
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.ontology.AcrossTrialsOntologyTerm
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.querytool.QtPatientSetCollection
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import javax.sql.DataSource
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.sql.Clob
import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess
import static org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TABLE_CODE
import static org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TOP_TERM_NAME

@Slf4j('logger')
class I2b2HelperService implements InitializingBean {

    static transactional = false

    private static final String QUOTE = "'"
    private static final String GENE_PATTERN_WHITE_SPACE_DEFAULT = '0'
    private static final String GENE_PATTERN_WHITE_SPACE_EMPTY = ''
    private static final String[] ALL_CHROMS = ['1', '2', '3', '4', '5', '6', '7', '8',
	                                        '9', '10', '11', '12', '13', '14', '15', '16',
	                                        '17', '18', '19', '20', '21', '22', 'X', 'Y']
    private static final Map<String, String[]> CHROM_END_PROBE_LINE_MAP
    static {
	List<String> snps = new LinkedList<>(Arrays.asList(
	    'SNP_A-8575125\t1\t564621', 'SNP_A-8391333\t1\t249198692',
	    'SNP_A-8615982\t2\t15703', 'SNP_A-8304446\t2\t243048760',
	    'SNP_A-2100278\t3\t66866', 'SNP_A-8336753\t3\t197856433',
	    'SNP_A-8661350\t4\t45410', 'SNP_A-8713585\t4\t190921709',
	    'SNP_A-8392711\t5\t36344', 'SNP_A-2186029\t5\t180692833',
	    'SNP_A-8533260\t6\t203249', 'SNP_A-8608599\t6\t170918031',
	    'SNP_A-8539824\t7\t43259', 'SNP_A-8436508\t7\t159119220',
	    'SNP_A-8325516\t8\t161222', 'SNP_A-2094900\t8\t146293414',
	    'SNP_A-8574568\t9\t37747', 'SNP_A-8302801\t9\t141071475',
	    'SNP_A-8435658\t10\t104427', 'SNP_A-4271863\t10\t135434551',
	    'SNP_A-8300213\t11\t198510', 'SNP_A-2246844\t11\t134944770',
	    'SNP_A-8434276\t12\t161382', 'SNP_A-4219877\t12\t133777645',
	    'SNP_A-8687595\t13\t19045720', 'SNP_A-8587371\t13\t115106996',
	    'SNP_A-8430270\t14\t20211644', 'SNP_A-2127677\t14\t107285437',
	    'SNP_A-8429754\t15\t20071673', 'SNP_A-8685263\t15\t102400037',
	    'SNP_A-1807459\t16\t86671', 'SNP_A-1841720\t16\t90163275',
	    'SNP_A-8398136\t17\t6689', 'SNP_A-8656409\t17\t81049726',
	    'SNP_A-8496414\t18\t11543', 'SNP_A-8448011\t18\t78015057',
	    'SNP_A-8509279\t19\t260912', 'SNP_A-8451148\t19\t59095126',
	    'SNP_A-8559313\t20\t61795', 'SNP_A-8480501\t20\t62912463',
	    'SNP_A-4217519\t21\t9764385', 'SNP_A-8349060\t21\t48084820',
	    'SNP_A-8656401\t22\t16055171', 'SNP_A-8313387\t22\t51219006',
	    'SNP_A-8572888\tX\t119805', 'SNP_A-8363487\tX\t154925045',
	    'SNP_A-8655052\tY\t2722506', 'SNP_A-8433021\tY\t28758193'))
	Map<String, String[]> map = [:]
	for (chrom in ALL_CHROMS) {
	    map[chrom] = [snps.remove(0), snps.remove(0)] as String[]
	}
	assert !snps

	CHROM_END_PROBE_LINE_MAP = map.asImmutable()
    }

    @Autowired private ConceptService conceptService
    ConceptsResource conceptsResourceService
    @Autowired private DataSource dataSource
    @Autowired private GrailsApplication grailsApplication
    @Autowired private SecurityService securityService
    @Autowired private SessionFactory sessionFactory

    @Value('${com.recomdata.datasetExplorer.plinkExcutable:}')
    private String plinkExecutable

    @Value('${edu.harvard.transmart.gridview.expandFolderIntoColumns:false}')
    private boolean expandFolderIntoColumns

    @Value('${edu.harvard.transmart.gridview.blacklist.paths:}')
    private List<String> blacklistPaths

    private List<String> censorFlagList
    private List<String> survivalDataList

    /**
     * Gets a distribution of information from the patient dimension table for value columns
     */
    double[] getPatientDemographicValueDataForSubset(String col, String resultInstanceId) {
	checkQueryResultAccess resultInstanceId

        String authStudiesString = getSqlInString(getAuthorizedStudies())

        // NOTE: UGLY, UGLY CODE -  The sourcesystem_cd field, in the case that across trials data
        // exists, will be TrialId:SubjectId, where SubjectId is a unique subject id across trials.
	List<Double> values = []
	Set<String> idSet = []
	String sql = '''
		SELECT ''' + col + ''', sourcesystem_cd, f.patient_num
		FROM I2B2DEMODATA.patient_dimension f
		JOIN patient_trial pt ON pt.patient_num = f.patient_num
		WHERE
		pt.trial IN (''' + authStudiesString + ''') AND
		f.patient_num IN (
		    select distinct patient_num
		    from I2B2DEMODATA.qt_patient_set_collection
		    where result_instance_id = ?)'''
	eachRow(sql, [resultInstanceId]) { row ->
	    String id = rowGet(row, 2, String)
	    String code = rowGet(row, 1, String)
	    if (code) {
		String[] parts = code.split(':')
		if (parts && parts.length == 2 && parts[1] != null) {
		    id = parts[1]
                }
            }
            if (!idSet.contains(id)) {
		idSet << id
		values << rowGet(row, 0, Double)
            }
        }

	values.findAll { it != null } as double[]
    }

    /**
     * Converts a concept key to a path
     */
    String keyToPath(String conceptKey) {
	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	if (fullname.endsWith('\\')) {
	    fullname
	}
	else {
	    fullname + '\\'
        }
    }

    /**
     *  Gets the parent concept key of a concept key
     */
    private String getParentConceptKey(String conceptKey) {
	conceptKey.substring(0, conceptKey.lastIndexOf('\\', conceptKey.length() - 2) + 1)
    }

    /**
     * Gets the trimmed last part of the display name from a concept key
     */
    def String getTrimmedNameFromKey(String conceptKey, int maxTrimmed = 125) {
        String[] splits = conceptKey.split('\\\\');

        String concept_name = ''
        String node_name = ''
        int fullName = 1
        int nameLength

        // skip 3 levels \\Top Node\ parse from repeated \Top Node\...
        for(int i = (splits.length); i > 1; i--)  {
            node_name = splits[i-1]
            nameLength = concept_name.length() + node_name.length()
            if(nameLength > maxTrimmed) {
                concept_name = '...' + concept_name
                fullName = 0
                break
            }

            concept_name = '\\' + node_name + concept_name;
        }

        return concept_name;
    }

    /**
     * Gets the short display name from a concept key
     */
    String getShortNameFromKey(String conceptKey) {
        String[] splits = conceptKey.split('\\\\');

        String concept_name = splits[splits.length - 1]
    }

    /**
     * Gets the concept codes associated with a concept key (comma delimited string returned)
     */
    String getConceptCodeFromKey(String key) {
        String path = key.substring(key.indexOf('\\', 2), key.length())
        if (!path.endsWith('\\')) {
            path += '\\'
        }

	StringBuilder concepts = new StringBuilder()
	String sql = 'SELECT CONCEPT_CD FROM I2B2DEMODATA.CONCEPT_DIMENSION c WHERE CONCEPT_PATH = ?'
	eachRow(sql, [path]) { row ->
	    if (concepts) {
		concepts << ','
	    }
	    concepts << rowGet(row, 'CONCEPT_CD', String)
	}
	concepts
    }

    String getConceptPathFromCode(String code) {
        String path = null
	String sql = 'SELECT CONCEPT_PATH FROM I2B2DEMODATA.CONCEPT_DIMENSION c WHERE CONCEPT_CD = ?'
	eachRow(sql, [code]) { row ->
	    path = rowGet(row, 'CONCEPT_PATH', String)
	}
	path
    }

    /**
     * Gets concept key for analysis, does logic to return parent folder if a leaf
     * is not a value type leaf
     */
    String getConceptKeyForAnalysis(String conceptKey) {
	if (isLeafConceptKey(conceptKey)) {
	    if (isValueConceptKey(conceptKey) || isHighDimensionalConceptKey(conceptKey)) {
		conceptKey //just use me cause im a value node
            }
            else {
		//get parent folder (could make recursive)
		getParentConceptKey conceptKey
	    }
        }
        else {
	    //must be folder
	    conceptKey
	}
    }

    /**
     * Gets the level from a concept key (level indicates depth in tree)
     */
    private int getLevelFromKey(String key) {
        String fullname = key.substring(key.indexOf('\\', 2), key.length())
        int res = 0
	String sql = 'SELECT c_hlevel FROM i2b2metadata.i2b2 WHERE C_FULLNAME = ?'
	eachRow(sql, [fullname]) { row ->
	    res = rowGet(row, 'c_hlevel', Integer)
	}

	res
    }

    String getMarkerTypeFromConceptCd(String conceptCd) {
	String sql = '''
			select dgi.marker_type
			from I2B2DEMODATA.concept_dimension cd, DEAPP.de_gpl_info dgi
			where cd.concept_path like('%'||dgi.title||'%')
			  and cd.concept_cd = ?'''
	String markerType = ''
	eachRow(sql, [conceptCd]) { row ->
	    markerType = rowGet(row, 'marker_type', String)
	}

	markerType
    }

    private boolean isXTrialsConcept(String conceptKey) {
	conceptsResourceService.getByKey(conceptKey) instanceof AcrossTrialsOntologyTerm
    }

    /**
     * Determines if a concept key is a value concept.
     */
    private boolean isValueConceptKey(String conceptKey) {
	if (isXTrialsConcept(conceptKey)) {
	    AcrossTrialsOntologyTerm itemProbe = (AcrossTrialsOntologyTerm) conceptsResourceService.getByKey(conceptKey)
	    boolean xTrialsValueConcept = itemProbe.modifierDimension.valueType.equalsIgnoreCase('N')
            return xTrialsValueConcept
	}

	String conceptCode = getConceptCodeFromKey(conceptKey)
	boolean ret = isValueConceptCode(conceptCode)

	ret
    }

    /**
     * Determines if a concept key is a high dimensional concept;
     * true for keys of the form ...\Human Affymetrix ...\
     * as well as ...\Human Affymetrix ...\TNF
     */
    boolean isHighDimensionalConceptKey(String conceptKey) {
	isHighDimensionalConceptCode getConceptCodeFromKey(conceptKey)
    }

    /**
     * Determines if a concept key is a leaf or not (by conceptKey)
     */
    /*private */boolean isLeafConceptKey(String conceptKey) {
        // profuse appoligies to future programmers reading this code; it is clearly a mess
        // and this is a patch on top of a mess; the correct solution is to rewrite all this code
        // to use the API and to rewrite the underlying object class to use the API.
        // A special case was made for across trials data, because, at this time of this change,
        // there is no unified representation of across trials data and 'normal' data
	
	if (isXTrialsConcept(conceptKey)) {
	    return isLeafConceptKey((AcrossTrialsOntologyTerm) conceptsResourceService.getByKey(conceptKey))
        }

	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	boolean res = false
	String sql = 'SELECT C_VISUALATTRIBUTES FROM I2B2METADATA.I2B2 WHERE C_FULLNAME = ?'
	eachRow(sql, [fullname]) { row ->
	    res = rowGet(row, 'c_visualattributes', String).contains('L')
	}
	res
    }

    /**
     * Determines if a concept item is a leaf.
     */
    /*private */boolean isLeafConceptKey(AcrossTrialsOntologyTerm conceptItem) {
        // profuse appoligies to future programmers reading this code; it is clearly a mess
        // and this is a patch on top of a mess; the correct solution is to rewrite all this code
        // to use the API and to rewrite the underlying object class to use the API.
        EnumSet probeSet = conceptItem.visualAttributes
	if (probeSet) {
	    probeSet.any { it == OntologyTerm.VisualAttributes.LEAF }
	}
	else {
	    false
	}
    }

    /**
     * Determines if a concept item is a leaf or not
     */
    boolean isLeafConceptKey(I2b2 conceptItem) {
        // profuse appoligies to future programmers reading this code; it is clearly a mess
        // and this is a patch on top of a mess; the correct solution is to rewrite all this code
        // to use the API and to rewrite the underlying object class to use the API.
	conceptItem.cVisualattributes.contains 'L'
    }

    /**
     * Gets the distinct patient counts for the children of a parent concept key
     */
    Map<String, Integer> getChildrenWithPatientCountsForConcept(String conceptKey) {

	String xTrialsTopNode = '\\\\' + ACROSS_TRIALS_TABLE_CODE + '\\' + ACROSS_TRIALS_TOP_TERM_NAME + '\\'
	boolean xTrialsCaseFlag = isXTrialsConcept(conceptKey) || (conceptKey == xTrialsTopNode)

	Map<String, Integer> counts = [:]

        if (xTrialsCaseFlag) {
	    OntologyTerm node = conceptsResourceService.getByKey(conceptKey)
	    for (OntologyTerm term in node.children) {
		counts[term.fullName] = getObservationCountForXTrialsNode((AcrossTrialsOntologyTerm) term)
            }
        }
        else {
	    String path = keyToPath(conceptKey)
	    String sql = 'select * from I2B2DEMODATA.CONCEPT_COUNTS where parent_concept_path = ?'
	    eachRow(sql, [path]) { row ->
		String conceptPath = rowGet(row, 'concept_path', String)
		counts[conceptPath] = rowGet(row, 'patient_count', Integer)
	    }
        }

	counts
    }

    /**
     * Gets the data associated with a value type concept from observation fact table
     * for display in a distribution histogram
     */
    private List<Double> getConceptDistributionDataForValueConcept(String conceptCode) {
	List<Double> values = []
	String sql = 'SELECT NVAL_NUM FROM I2B2DEMODATA.OBSERVATION_FACT f WHERE CONCEPT_CD = ?'
	eachRow(sql, [conceptCode]) { row ->
	    Double nval = rowGet(row, 'NVAL_NUM', Double)
	    if (nval != null) {
		values << nval
            }
	}

	values
    }

    /**
     *  Gets the data associated with a value type concept from observation fact table
     * for display in a distribution histogram for a given subset
     */
    private List<Double> getConceptDistributionDataForValueConcept(String conceptKey, String resultInstanceId) {

	checkQueryResultAccess resultInstanceId

	boolean xTrialsCaseFlag = isXTrialsConcept(conceptKey)

	List<Double> values

	String authStudiesString = getSqlInString(getAuthorizedStudies())

	if (xTrialsCaseFlag) {
	    values = fetchAcrossTrialsData(conceptKey, resultInstanceId)*.value as List<Double>
            }
        else {
	    String sql = '''
			SELECT NVAL_NUM
			FROM I2B2DEMODATA.OBSERVATION_FACT f
			WHERE CONCEPT_CD = ?
			AND PATIENT_NUM IN (
				select distinct patient_num
				from I2B2DEMODATA.qt_patient_set_collection
				where result_instance_id = ?)'''
	    values = []
            try {
		eachRow(sql, [getConceptCodeFromKey(conceptKey), resultInstanceId]) { row ->
		    Double nval = rowGet(row, 'NVAL_NUM', Double)
		    if (nval != null) {
			values << nval
                    }
		}
            }
	    catch (e) {
		logger.error 'exception in getConceptDistributionDataForValueConcept: {}', e.message
            }
	}

	values
    }

    List<Double> getConceptDistributionDataForValueConceptFromCode(String conceptCode, String resultInstanceId) {

	checkQueryResultAccess resultInstanceId

	List<Double> values = []
	if (!resultInstanceId) {
	    return getConceptDistributionDataForValueConcept(conceptCode)
        }

	String sql = '''
		SELECT NVAL_NUM
		FROM I2B2DEMODATA.OBSERVATION_FACT f
		WHERE CONCEPT_CD = ?
		AND PATIENT_NUM IN (
			select distinct patient_num
			from I2B2DEMODATA.qt_patient_set_collection
	where result_instance_id = ?)'''
	eachRow(sql, [conceptCode, resultInstanceId]) { row ->
	    Double nval = rowGet(row, 'NVAL_NUM', Double)
	    if (nval != null) {
		values << nval
	    }
        }

	values
    }

    /**
     *  Gets the count of a patient set from the result instance id
     */
    int getPatientSetSize(String resultInstanceId) {
	checkQueryResultAccess resultInstanceId

        String authStudiesString = getSqlInString(getAuthorizedStudies())

        // original code counted split_part(pd.sourcesystem_cd , ':', 2)
        // but this is a postgres-only built-in function
	String sql = '''
		select count(*) as patcount
            FROM (
                SELECT DISTINCT pd.sourcesystem_cd AS subject_id
			FROM I2B2DEMODATA.qt_patient_set_collection ps
			JOIN I2B2DEMODATA.patient_dimension pd ON ps.patient_num=pd.patient_num
                    JOIN patient_trial pt ON pt.patient_num = ps.patient_num
                WHERE ps.result_instance_id = CAST(? AS numeric)
                AND pt.trial IN (''' + authStudiesString + ''')
            ) patient_set'''

	int i = 0
	eachRow(sql, [resultInstanceId]) { row ->
	    i = rowGet(row, 'patcount', Integer)
	}

	i
    }

    /**
     *  Gets the intersection of the patient sets from two result instance ids
     */
    int getPatientSetIntersectionSize(String resultInstanceId1, String resultInstanceId2) {
	checkQueryResultAccess resultInstanceId1, resultInstanceId2

        // original code counted split_part(pd.sourcesystem_cd , ':', 2)
        // but this is a postgres-only built-in function
	String sql = '''
		select count(*) as patcount
        FROM (
                SELECT DISTINCT pd.sourcesystem_cd AS subject_id
			from I2B2DEMODATA.qt_patient_set_collection a
			inner join I2B2DEMODATA.qt_patient_set_collection b
                on a.patient_num=b.patient_num and a.result_instance_id = CAST(? AS numeric)
			join I2B2DEMODATA.patient_dimension pd
                on b.patient_num=pd.patient_num and b.result_instance_id = CAST(? AS numeric)
        ) qt_patient_set'''

	int i = 0
	eachRow(sql, [resultInstanceId1, resultInstanceId2]) { row ->
	    i = rowGet(row, 'patcount', Integer)
	}

	i
    }

    /**
     * Converts a clob to a string I hope
     */
    private String clobToString(clob) {
        if (clob == null) {
	    ''
        }
        else if (clob instanceof NullObject) {
	    ''
        }
	else if (clob instanceof String) {
            // postgres schema uses strings in some places oracle uses clobs
	    clob
        }
	else {
	    ((Clob) clob).asciiStream.text
        }
    }

    /**
     * Determines if a concept code is a value concept code or not by checking the metadata xml
     */
    boolean isValueConceptCode(String conceptCode) {
	String sql = 'SELECT C_METADATAXML FROM I2B2METADATA.I2B2 WHERE C_BASECODE = ?'
        String xml = ''
	eachRow(sql, [conceptCode]) { row ->
	    def clob = rowGet(row, 'c_metadataxml', Object)
	    xml = clobToString(clob)
	}

	nodeXmlRepresentsValueConcept xml
    }

    /**
     * Determines if a concept code is a high-dimensional concept code by checking the visual
     * style.
     * TODO: when omics data have proper metadata, change this to check to metadata!
     * @param conceptCode the concept code to check
     * @return true if i2b2metadata.i2b2.c_visualattributes equals 'LAH' for the given concept code
     */
    boolean isHighDimensionalConceptCode(String conceptCode) {
	boolean res = false
	String sql = 'SELECT C_VISUALATTRIBUTES FROM I2B2METADATA.I2B2 WHERE C_BASECODE = ?'
	eachRow(sql, [conceptCode]) { row ->
	    res = rowGet(row, 'c_visualattributes', String) == 'LAH'
        }

	res
    }

    /**
     * Check if a map contains all the keys an omics_params map should contain
     * @param params the map to check
     * @return True if the map contains all necessary keys, false otherwise
     */
    def Boolean isValidOmicsParams(Map params) {
        ['omics_selector', 'omics_projection_type', 'omics_property', 'omics_selector'].every {params?.containsKey(it)}
    }

    private boolean nodeXmlRepresentsValueConcept(String xml) {
	if (!xml) {
	    return false
        }

	Document doc = parseXml(xml)
	Node node = (Node) newXPath().evaluate('//ValueMetadata/Oktousevalues', doc, XPathConstants.NODE)
	node.textContent.equalsIgnoreCase 'Y'
    }

    Map<String, Integer> getConceptDistributionDataForConcept(String conceptKey,
	                                                      String resultInstanceId) throws SQLException {
	checkQueryResultAccess resultInstanceId

	boolean xTrialsCaseFlag = isXTrialsConcept(conceptKey)
	boolean leafNodeFlag = isLeafConceptKey(conceptKey)

	Map<String, Integer> results = [:]

	if (leafNodeFlag) {
	    conceptKey = getParentConceptKey(conceptKey)
        }

	OntologyTerm node = conceptsResourceService.getByKey(conceptKey)

        if (xTrialsCaseFlag) {
	    for (OntologyTerm term in node.children) {
		results[term.name] = getObservationCountForXTrialsNode(
		    (AcrossTrialsOntologyTerm) term, resultInstanceId)
            }
        }
        else {
	    String full = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	    int i = getLevelFromKey(conceptKey) + 1
	    String sql = '''
                SELECT DISTINCT c_name, c_fullname
                FROM i2b2metadata.i2b2
			WHERE C_FULLNAME LIKE ? escape '\\'
			  AND c_hlevel = ?
			ORDER BY C_FULLNAME'''
	    eachRow(sql, [Utils.asLikeLiteral(full) + '%', i]) { row ->
		String name = rowGet(row, 0, String)
		String fullName = rowGet(row, 1, String)
		results[name] = getObservationCountForConceptForSubset('\\blah' + fullName, resultInstanceId)
            }
        }

	results
    }

    /**
     *  Returns the patient count for a concept key
     */
    int getPatientCountForConcept(String conceptKey) {
	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	String sql = '''
		select count (distinct patient_num) as patcount
		    FROM i2b2demodata.observation_fact
			WHERE concept_cd IN (
				select concept_cd
				from i2b2demodata.concept_dimension c
				where concept_path LIKE ? escape '\\')'''

	int i = 0
	eachRow(sql, [Utils.asLikeLiteral(fullname) + '%']) { row ->
	    i = rowGet(row, 0, Integer)
        }

	i
    }

    private int getObservationCountForXTrialsNode(AcrossTrialsOntologyTerm termNode, String resultInstanceId) {
	checkQueryResultAccess resultInstanceId

	List<String> modifierList = getAllXTrialsLeafNodes(termNode)*.code

        // original code counted split_part(pd.sourcesystem_cd , ':', 2)
        // but this is a postgres-only built-in function
	String sql = '''
            SELECT count(*) FROM (
                SELECT DISTINCT pd.sourcesystem_cd AS subject_id
			FROM I2B2DEMODATA.observation_fact f
			JOIN I2B2DEMODATA.patient_dimension pd ON f.patient_num=pd.patient_num
			WHERE modifier_cd in (''' + listToIN(modifierList) + ''')
                    AND concept_cd != 'SECURITY'
			AND f.patient_num IN (
				select distinct patient_num
				from I2B2DEMODATA.qt_patient_set_collection
                        where result_instance_id = ?)
		) subjectList'''

        int count = 0
	eachRow(sql, [resultInstanceId]) { row ->
	    count = rowGet(row, 0, Integer)
	}

	count
    }

    private int getObservationCountForXTrialsNode(AcrossTrialsOntologyTerm termNode) {
        String authStudiesString = getSqlInString(getAuthorizedStudies())
	List<String> modifierList = getAllXTrialsLeafNodes(termNode)*.code
	if (!modifierList) {
	    return 0
	}

        // original code counted split_part(pd.sourcesystem_cd , ':', 2)
        // but this is a postgres-only built-in function
	String sql = '''
            SELECT count(*) FROM (
                SELECT DISTINCT pd.sourcesystem_cd AS subject_id
			FROM I2B2DEMODATA.observation_fact f
			JOIN I2B2DEMODATA.patient_dimension pd ON f.patient_num=pd.patient_num
                    JOIN patient_trial pt ON pt.patient_num = f.patient_num
			WHERE
                    pt.trial IN (''' + authStudiesString + ''') AND
                    f.modifier_cd in (''' + listToIN(modifierList) + ''')
                    AND f.concept_cd != 'SECURITY'
		) subjectList'''
		
        int count = 0
	eachRow(sql) { row ->
	    count = rowGet(row, 0, Integer)
	}

	count
    }

    private List<AcrossTrialsOntologyTerm> getAllXTrialsLeafNodes(AcrossTrialsOntologyTerm top) {
	List<AcrossTrialsOntologyTerm> nodes = []

        if (isLeafConceptKey(top)) {
	    nodes << top
            return nodes
        }

	for (OntologyTerm child in top.children) {
	    nodes.addAll getAllXTrialsLeafNodes((AcrossTrialsOntologyTerm) child)
        }

	nodes
    }

    /**
     * Gets the count of the observations in the fact table for a concept and a subset
     */
    private int getObservationCountForConceptForSubset(String conceptKey, String resultInstanceId) {
	checkQueryResultAccess resultInstanceId

	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())
	String fullnameLike = Utils.asLikeLiteral(fullname) + '%'
	String sql = '''
		select count(*) from (
                select distinct patient_num
                FROM i2b2demodata.observation_fact
                WHERE concept_cd IN (
                        select concept_cd
                        from i2b2demodata.concept_dimension c
				where concept_path LIKE ? escape '\\'
					)
                    AND PATIENT_NUM IN (
                        select distinct patient_num
				from I2B2DEMODATA.qt_patient_set_collection
				where result_instance_id = ?
			)
		) subjectList'''

	int i = 0
	eachRow(sql, [fullnameLike, resultInstanceId]) { row ->
	    i = rowGet(row, 0, Integer)
	}

	i
    }

    /**
     * Fills the main demographic data in an export table for the grid
     */
    ExportTableNew addAllPatientDemographicDataForSubsetToTable(ExportTableNew table,
	                                                        String resultInstanceId, String subset) {
	checkQueryResultAccess resultInstanceId

        String authStudiesString = getSqlInString(getAuthorizedStudies())

	Map<Long, String> mapOfSampleCdsByPatientNum = buildMapOfSampleCdsByPatientNum(resultInstanceId)

        //if i have an empty table structure so far
	if (!table.columns) {
	    table.putColumn 'subject', new ExportColumn('subject', 'Subject', '', 'String')
	    table.putColumn 'patient', new ExportColumn('patient', 'Patient', '', 'String')
	    table.putColumn 'SAMPLE_CDS', new ExportColumn('SAMPLE_CDS', 'Samples', '', 'String')
	    table.putColumn 'subset', new ExportColumn('subset', 'Subset', '', 'String')
	    table.putColumn 'TRIAL', new ExportColumn('TRIAL', 'Trial', '', 'String')
	    table.putColumn 'SEX_CD', new ExportColumn('SEX_CD', 'Sex', '', 'String')
	    table.putColumn 'AGE_IN_YEARS_NUM', new ExportColumn('AGE_IN_YEARS_NUM', 'Age', '', 'Number')
	    table.putColumn 'RACE_CD', new ExportColumn('RACE_CD', 'Race', '', 'String')
	}

	String sql = '''
		SELECT I.*
		FROM (
			SELECT p.*, t.trial
			FROM I2B2DEMODATA.patient_dimension p
			INNER JOIN I2B2DEMODATA.patient_trial t ON p.patient_num = t.patient_num
			WHERE p.PATIENT_NUM IN (
				SELECT DISTINCT ps.patient_num
				FROM I2B2DEMODATA.qt_patient_set_collection ps
                        JOIN patient_trial pt ON pt.patient_num = ps.patient_num
				WHERE
                            pt.trial IN (''' + authStudiesString + ''') AND
                            result_instance_id=?
			)
		) I
		ORDER BY I.PATIENT_NUM'''

	eachRow(sql, [resultInstanceId]) { row ->
	    // If I already have this subject mark it in the subset column as belonging to both subsets
	    String subject = rowGet(row, 'PATIENT_NUM', String)
	    if (table.containsRow(subject)) {
		String s = table.getRow(subject).get('subset')
		table.getRow(subject).put 'subset', s + ',' + subset
	    }
	    else { // fill the row
		ExportRowNew newrow = new ExportRowNew()
		newrow.put 'subject', subject
		String[] arr = rowGet(row, 'SOURCESYSTEM_CD', String)?.split(':')
		newrow.put 'patient', arr?.length == 2 ? arr[1] : ''
		String cds = mapOfSampleCdsByPatientNum[rowGet(row, 'PATIENT_NUM', Long)]
		newrow.put 'SAMPLE_CDS', cds ?: ''
		newrow.put 'subset', subset
		newrow.put 'TRIAL', rowGet(row, 'TRIAL', String)
		String sexCode = rowGet(row, 'SEX_CD', String)
		if (sexCode) {
		    sexCode = sexCode.toLowerCase()
		    newrow.put 'SEX_CD',
			sexCode == 'm' || sexCode == 'male' ?
			'male' :
			sexCode == 'f' || sexCode == 'female' ?
			'female' :
			'NULL'
		}
		String ageInYears = rowGet(row, 'AGE_IN_YEARS_NUM', String)
		if (ageInYears) {
		    newrow.put 'AGE_IN_YEARS_NUM', ageInYears
		}
		String raceCode = rowGet(row, 'RACE_CD', String)
		if (raceCode) {
		    newrow.put 'RACE_CD', raceCode.toLowerCase()
		}
		table.putRow subject, newrow
	    }
	}

	table
    }

    private Map<Long, String> buildMapOfSampleCdsByPatientNum(String resultInstanceId) {
	Map<Long, String> map = [:]
	String sql = '''
		SELECT DISTINCT f.PATIENT_ID, f.SAMPLE_CD
		FROM DEAPP.de_subject_sample_mapping f
		WHERE f.PATIENT_ID IN (
			SELECT DISTINCT patient_num
			FROM I2B2DEMODATA.qt_patient_set_collection
			WHERE result_instance_id = ?)
            ORDER BY PATIENT_ID, SAMPLE_CD'''
	for (row in new Sql(dataSource).rows(sql, resultInstanceId)) {
	    Long patientNum = rowGet(row, 'PATIENT_ID', Long)
	    if (!patientNum) {
		continue
            }

	    String sampleCd = rowGet(row, 'SAMPLE_CD', String)
	    if (!sampleCd) {
		continue
            }

	    String entry = map[patientNum] ?: ''
	    if (entry) {
		entry += ','
	    }
	    map[patientNum] = entry + sampleCd
        }

	map
    }

    /**
     * Checks if a string represents a URL
     */
    private boolean isURL(String s) {
        // Attempt to convert string into an URL.
        try {
	    new URL(s)
	    true
        }
	catch (MalformedURLException ignored) {
	    false
        }
    }

    /**
     * Adds a column of data to the grid export table
     */
    ExportTableNew addConceptDataToTable(ExportTableNew table, String conceptKey, String resultInstanceId) {
	//checkQueryResultAccess resultInstanceId

        ExportColumn hascol

        if (pathIsBlacklisted(conceptKey)) {
	    return table
	}

	boolean leafConceptFlag = isLeafConceptKey(conceptKey)
	boolean xTrialsCaseFlag = isXTrialsConcept(conceptKey)

	// As the column headers only show the (in many cases ambiguous) leaf part of the concept path,
	// showing the full concept path in the tooltip is much more informative.
	// As no tooltip text is passed on to the GridView code, the value of the string columnId is used
	// and shown as the tooltip text when hoovering over the column header in GridView.
	// Explicitly passing a tooltip text to the GridView code removes the necessity to use this columnId value.
	// Removal of some undesired non-alpha-numeric characters from tooltip string
	// prevents display errors in GridView (drop down menu, columns not showing or cells not being filled).

	String columnId = encodeAsSHA1(conceptKey)
	String columnName = conceptKey.split('\\\\')[-1].replace(' ', '_')
	String columnTooltip = keyToPath(conceptKey).replaceAll('[^a-zA-Z0-9_\\-\\\\]+', '_')
        String columnType

        if (leafConceptFlag) {
	    boolean valueLeafNodeFlag = isValueConceptKey(conceptKey)
	    columnType = valueLeafNodeFlag ? 'number' : 'string'

	    // add the subject and columnId column to the table if its not there
	    if (table.getColumn('subject') == null) {
		table.putColumn 'subject', new ExportColumn('subject', 'Subject', '', 'string')
            }

	    hascol = table.getColumnByBasename(columnName); // check existing column with same basename

	    if (table.getColumn(columnId) == null) {
		table.putColumn columnId, new ExportColumn(columnId, columnName, '', columnType, columnTooltip)
                if(hascol)
                    table.setColumnUnique(columnId); // make labels unique by expanding
            }

            if (xTrialsCaseFlag) {
		insertAcrossTrialsConceptDataIntoTable columnId, conceptKey, resultInstanceId, table
            }
            else {
		insertConceptDataIntoTable columnId, conceptKey, resultInstanceId, table
            }
        }
        else {
            // If a folder is dragged in, we want the contents of the folder to be added to the data
            // That is possible if the folder contains only categorical values and no subfolders.

            // Check whether the folder is valid: first find all children of the current code
	    OntologyTerm item = conceptsResourceService.getByKey(conceptKey)

            if (!item.children) {
		return table
            }

            // All children should be leaf categorical values
	    boolean any = item.children.any { OntologyTerm it ->
                if (xTrialsCaseFlag) {
		    !isLeafConceptKey(it)
                }
		else {
		    !isLeafConceptKey(it) || nodeXmlRepresentsValueConcept(it.metadataxml)
		}
	    }

	    if (any) {
		return table
            }

	    // add the subject column to the table if it's not there
	    if (table.getColumn('subject') == null) {
		table.putColumn 'subject', new ExportColumn('subject', 'Subject', '', 'string')
	    }

            hascol = table.getColumnByBasename(columnName); // check existing column with same basename

            if (table.getColumn(columnId) == null) {
                table.putColumn(columnId, new ExportColumn(columnId, columnName, '', columnType,columnTooltip));
                if(hascol)
                    table.setColumnUnique(columnId); // make labels unique by expanding
            }

 	    addFolderConceptDataToTable table, conceptKey, resultInstanceId, item, xTrialsCaseFlag
        }

	table
    }

    private boolean pathIsBlacklisted(String conceptKey) {
	String path = conceptKey.substring(conceptKey.indexOf('\\', 2) + 1, conceptKey.length())
	for (String blacklistPath in blacklistPaths) {
	    if (path.startsWith(blacklistPath)) {
		return true
	    }
	}
	false
    }

    private void addFolderConceptDataToTable(ExportTableNew table, String conceptKey, String resultInstanceId,
	                                     OntologyTerm item, boolean xTrialsCaseFlag) {

	String columnId = encodeAsSHA1(conceptKey)
	if (!expandFolderIntoColumns || xTrialsCaseFlag) {
	    if (table.getColumn(columnId) == null) {
		String columnName = conceptKey.split('\\\\')[-1].replace(' ', '_')
		String columnTooltip = keyToPath(conceptKey).replaceAll('[^a-zA-Z0-9_\\-\\\\]+', '_')
		table.putColumn columnId, new ExportColumn(columnId, columnName, '', 'string', columnTooltip)
            }
        }

	if (!xTrialsCaseFlag) {
	    addSingleTrialFolderConceptDataToTable table, resultInstanceId, item, expandFolderIntoColumns, columnId
	    return
	}

	for (OntologyTerm child in item.children) {
	    conceptKey = child.key
	    insertAcrossTrialsConceptDataIntoTable columnId, conceptKey, resultInstanceId, table
	}
    }

    private void addSingleTrialFolderConceptDataToTable(ExportTableNew table, String resultInstanceId,
	                                                OntologyTerm item, boolean expandFolderIntoColumns,
	                                                String folderColumnId) {
	// Store the concept paths to query
	List<String> paths = item.children*.fullName

        // Find the concept codes for the given children
	List<ConceptDimension> concepts = findAllByConceptPathInList(paths)

	Collection<String> conceptCodes = []
	Map<String, String> columnIds = [:]
	for (ConceptDimension concept in concepts) {
	    conceptCodes << concept.conceptCode

	    if (expandFolderIntoColumns) {
		String conceptPath = concept.conceptPath
		String columnId = encodeAsSHA1(conceptPath)
		columnIds[concept.conceptCode] = columnId

		if (table.getColumn(columnId) == null) {
		    String columnName = conceptPath.split('\\\\')[-1].replace(' ', '_')
		    String columnTooltip = conceptPath.replaceAll('[^a-zA-Z0-9_\\-\\\\]+', '_')
		    table.putColumn columnId, new ExportColumn(columnId, columnName, '', 'string', columnTooltip)
		}
	    }
	    else {
		columnIds[concept.conceptCode] = folderColumnId
	    }
        }

        // Determine the patients to query
	List<Long> patientIds = QtPatientSetCollection.executeQuery('''
		SELECT q.patient.id
		FROM QtPatientSetCollection q
		WHERE q.resultInstance.id = ?''',
		[resultInstanceId.toLong()]) as List<Long>

        // If nothing is found, return
        if (!concepts || !patientIds) {
            return
        }

        // After that, retrieve all data entries for the children

	Map args = [:]
	String hql = 'SELECT o.patient.id, o.textValue, o.conceptCode FROM ObservationFact o WHERE ('
	hql += hqlBatched(splitIntoBatches(conceptCodes), args, 'o.conceptCode', 'conceptCodes')
	hql += ') AND ('
	hql += hqlBatched(splitIntoBatches(patientIds), args, 'o.patient.id', 'patientIds')
	hql += ')'
	List<Object[]> results = ObservationFact.executeQuery(hql, args) as List<Object[]>

	for (Object[] result in results) {
	    // If I already have this subject mark it in the subset column as belonging to both subsets
	    String subject = result[0]
	    String value = result[1] ?: 'Y'
	    String conceptCode = result[2]
	    ExportRowNew row
	    if (table.containsRow(subject)) {
		// should contain all subjects already if I ran the demographics first
		row = table.getRow(subject)
	    }
	    else {
		// fill the row
		row = new ExportRowNew()
		row.put 'subject', subject
		table.putRow subject, row
            }

	    row.put columnIds[conceptCode], value.toString()
        }
    }

    private List<List> splitIntoBatches(List things, int max = 1000) {
	assert max > 0
	List<List> split = []
	List current
	for (Iterator iter = things.iterator(); iter.hasNext(); ) {
	    if (current == null || current.size() == max) {
		current = new ArrayList(Math.min(things.size(), max))
		split << current
            }
	    current << iter.next()
        }
	split
    }

    private String hqlBatched(List batches, Map args, String column, String keyPrefix) {
	List parts = []
	for (int i = 0; i < batches.size(); i++) {
	    parts << column + ' IN (:' + keyPrefix + '_' + i + ')'
	    args[keyPrefix + '_' + i] = batches[i]
	}
	parts.join ' OR '
    }

    @CompileDynamic
    private List<ConceptDimension> findAllByConceptPathInList(List<String> paths) {
	ConceptDimension.findAllByConceptPathInList paths
    }

    private List<Map> fetchConceptData(String conceptKey, String resultInstanceId) {
	boolean valueLeafNodeFlag = isValueConceptKey(conceptKey)
	List<Map> dataList = []
	String conceptCode = getConceptCodeFromKey(conceptKey)
	String column
        if (valueLeafNodeFlag) {
	    column = 'NVAL_NUM'
        }
        else {
	    column = 'TVAL_CHAR'
	}

	String sql = '''
			SELECT PATIENT_NUM, START_DATE, ''' + column + '''
			FROM I2B2DEMODATA.OBSERVATION_FACT f
			WHERE CONCEPT_CD = ?
			AND PATIENT_NUM IN (
				select distinct patient_num
				from I2B2DEMODATA.qt_patient_set_collection
				where result_instance_id = ?)'''
	eachRow(sql, [conceptCode, resultInstanceId]) { row ->
	    def value
	    if (valueLeafNodeFlag) {
		value = rowGet(row, column, Object)
            }
	    else {
		value = rowGet(row, 'TVAL_CHAR', String) ?: 'Y'
		if (isURL((String) value)) {
		    // Embed URL in a HTML Link
		    value = '<a href="' + value + '" target="_blank">' + value + '</a>'
                }
            }

	    dataList << [subject: rowGet(row, 'PATIENT_NUM', String), value: value]
	}

	dataList
    }

    private void insertConceptDataIntoTable(String columnId, String conceptKey,
	                                    String resultInstanceId, ExportTableNew table) {
	for (Map map in fetchConceptData(conceptKey, resultInstanceId)) {
	    String subject = map.subject
	    String value = map.value as String
	    if (table.containsRow(subject)) /*should contain all subjects already if I ran the demographics first*/ {
		table.getRow(subject).put columnId, value
            }
	    else { // fill the row
                ExportRowNew newrow = new ExportRowNew()
		newrow.put 'subject', subject
		newrow.put columnId, value
		table.putRow subject, newrow
            }
        }
    }

    private List<Map> fetchAcrossTrialsData(String conceptKey, String resultInstanceId) {

	boolean valueLeafNodeFlag = isValueConceptKey(conceptKey)
	List<Map> dataList = []

	String authStudiesString = getSqlInString(getAuthorizedStudies())

	AcrossTrialsOntologyTerm itemProbe = (AcrossTrialsOntologyTerm) conceptsResourceService.getByKey(conceptKey)
	String modifierCode = itemProbe.modifierDimension.code

	String column
        if (valueLeafNodeFlag) {
	    column = 'NVAL_NUM'
        }
        else {
	    column = 'TVAL_CHAR'
	}

	String sql = '''
			SELECT PATIENT_NUM, START_DATE, ''' + column + '''
			FROM I2B2DEMODATA.OBSERVATION_FACT f
			WHERE modifier_cd = ?
                    AND concept_cd != 'SECURITY'
			AND PATIENT_NUM IN (
				select distinct ps.patient_num
				from I2B2DEMODATA.qt_patient_set_collection ps
                        	JOIN patient_trial pt ON pt.patient_num = ps.patient_num
				where pt.trial IN (''' + authStudiesString + ''') AND 
				result_instance_id = ?
			)'''

	eachRow(sql, [modifierCode, resultInstanceId]) { row ->
	    // If I already have this subject mark it in the subset column as belonging to both subsets
	    dataList << [subject: rowGet(row, 'PATIENT_NUM', String),
			 value  : rowGet(row, column, Object)]
	}

	dataList
    }

    private void insertAcrossTrialsConceptDataIntoTable(String columnId, String conceptKey,
	                                                String resultInstanceId, ExportTableNew table) {
	for (Map map in fetchAcrossTrialsData(conceptKey, resultInstanceId)) {
	    String subject = map.subject
	    String value = map.value as String
	    if (table.containsRow(subject)) {
		// should contain all subjects already if I ran the demographics first
		table.getRow(subject).put columnId, value
	    }
	    else {
		// fill the row
                ExportRowNew newrow = new ExportRowNew()
		newrow.put 'subject', subject
		newrow.put columnId, value
		table.putRow subject, newrow
            }
        }
    }

    /**
     * A distribution of information from the patient dimension table.
     */
    Map<String, Integer> getPatientDemographicDataForSubset(String col, String resultInstanceId) {

	checkQueryResultAccess resultInstanceId
	String authStudiesString = getSqlInString(getAuthorizedStudies())

	Map<String, Integer> results = [:]

        // original code counted split_part(pd.sourcesystem_cd , ':', 2)
        // but this is a postgres-only built-in function
	String sql = '''
		SELECT cat, COUNT(subject_id) as demcount
        FROM (
                SELECT DISTINCT UPPER(''' + col + ''') as cat, pd.sourcesystem_cd AS subject_id
			FROM I2B2DEMODATA.qt_patient_set_collection ps
			JOIN I2B2DEMODATA.patient_dimension pd
                ON ps.patient_num=pd.patient_num AND result_instance_id = ?
                JOIN patient_trial pt ON pt.patient_num = ps.patient_num
                WHERE pt.trial IN (''' + authStudiesString + ''')
        ) base
		GROUP BY cat
		ORDER BY cat'''

	eachRow(sql, [resultInstanceId]) { row ->
	    String cat = rowGet(row, 0, String)
	    int count = rowGet(row, 1, Integer)
	    if (cat != null && count != 0) {
		results[cat] = count
            }
	}

	results
    }

    /**
     * Concept keys in a subset.
     */
    private List<String> getConceptKeysInSubset(String resultInstanceId) {

	List<String> concepts = []
	String sql = '''
		SELECT REQUEST_XML
		FROM I2B2DEMODATA.QT_QUERY_MASTER c
		INNER JOIN I2B2DEMODATA.QT_QUERY_INSTANCE a ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID
		INNER JOIN I2B2DEMODATA.QT_QUERY_RESULT_INSTANCE b ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID
		WHERE RESULT_INSTANCE_ID = ?'''

        String xmlrequest = ''
	eachRow(sql, [resultInstanceId]) { row ->
	    xmlrequest = clobToString(rowGet(row, 'request_xml', Object))

	    Document doc = parseXml(xmlrequest)
	    NodeList nodes = (NodeList) newXPath().evaluate('//item/item_key', doc, XPathConstants.NODESET)
	    for (int i = 0; i < nodes.length; i++) {
		concepts << nodes.item(i).textContent
	    }
	}

	concepts
    }

    /**
     * Gets all the concept keys in both subsets into a map that distincts them
     */
    List<String> getConceptKeysInSubsets(String resultInstanceId1, String resultInstanceId2) {
	// get all distinct  concepts for analysis from both subsets into map
	Map<String, String> h = [:]
	if (resultInstanceId1) {
	    for (String c in getConceptKeysInSubset(resultInstanceId1)) {
		h[c] = c
	    }
        }
	if (resultInstanceId2) {
	    for (String c in getConceptKeysInSubset(resultInstanceId2)) {
		h[c] = c
	    }
	}

	h.keySet() as List
    }

    /**
     *  Returns a map mapping each named concept in the output to a
     *  specific concept within a single trial
     *  This is used when presenting results across trials
     */
    String lookupParentConcept(String conceptPath) {

        // DOES NOT APPEAR TO WORK - July 14, 2015 - Terry E Weymouth
        // I believe that this might be an old implementation of xTrials
        // In the current ETL, deapp.de_xtrial_child_map, is not populated!

	// get all distinct  concepts for analysis from both subsets into map
        try {
	    String sql = '''
			select parent_cd
			from deapp.de_xtrial_child_map xcm
			inner join I2B2DEMODATA.concept_dimension cd on xcm.concept_cd=cd.concept_cd
			where concept_path = ?'''
            String parentConcept = ''
	    eachRow(sql, [conceptPath]) { row ->
		parentConcept = rowGet(row, 'parent_cd', String)
            }

	    parentConcept ?: null
        }
        catch (e) {
	    logger.error 'Exception occurred when looking up parent concept: {}', e.message
        }
    }

    Set<String> lookupChildConcepts(String parentConcept, String resultInstanceId1,
	                            String resultInstanceId2 = null) {
	checkQueryResultAccess resultInstanceId1, resultInstanceId2

	Set<String> childConcepts = []

        if (parentConcept == null) {
            return (childConcepts)
        }

	if (!resultInstanceId1 && !resultInstanceId2) {
            return (childConcepts)
        }

	String sql = '''
		SELECT distinct x.concept_CD
		FROM deapp.de_xtrial_child_map x
		WHERE and x.parent_cd=?
		and x.study_id IN (
				select distinct p.trial
			from I2B2DEMODATA.qt_patient_set_collection q
			inner join I2B2DEMODATA.patient_trial p on q.patient_num=p.patient_num
			where q.result_instance_id=?'''
	List args = [parentConcept]

	if (!resultInstanceId1) {
	    args << resultInstanceId2
        }
	else if (!resultInstanceId2) {
	    args << resultInstanceId1
        }
        else {
	    args << resultInstanceId1
	    args << resultInstanceId2
	    sql += ' or q.result_instance_id=?'
        }
	sql += ')'

        try {
	    eachRow(sql) { row ->
		childConcepts << rowGet(row, 'concept_cd', String)
            }
        }
	catch (e) {
	    logger.error 'Exception occurred when looking up child concepts: {}; ' +
		'query: {} parentConcept: {} resultInstanceId1: {} resultInstanceId2: {}',
		e.message, sql, parentConcept, resultInstanceId1, resultInstanceId2
	}

	childConcepts
    }

    Set<String> getDistinctConceptSet(String resultInstanceId1, String resultInstanceId2) {
	// get all distinct  concepts for analysis from both subsets into map
	// only need one concept from each family, because the rendering methods find the others

	Set<String> workingSet = []
	Set<String> finalSet = []
	Set<String> parentSet = []

	if (resultInstanceId1) {
	    workingSet.addAll getConceptKeysInSubset(resultInstanceId1)
        }
	if (resultInstanceId2) {
	    workingSet.addAll getConceptKeysInSubset(resultInstanceId2)
        }

	for (String k in workingSet) {
            // always look for a parent
            String parentConcept = lookupParentConcept(keyToPath(k))
            if (parentConcept == null) {
		finalSet << k // add an orphan straight to the final set
            }
            else if (!parentSet.contains(parentConcept)) {
		parentSet << parentConcept
		finalSet << k // add the first concept to the final set
            }
        }

	finalSet
    }

    /**
     * Gets the request xml for query def id
     */
    String getQueryDefinitionXMLFromQID(String qid) {

        String xmlrequest = ''
	String sqlt = '''select REQUEST_XML from I2B2DEMODATA.QT_QUERY_MASTER WHERE QUERY_MASTER_ID = ?'''
	eachRow(sqlt, [qid]) { row ->
	    def clob = rowGet(row, 'REQUEST_XML', Object)
	    xmlrequest = clobToString(clob)
	}

	xmlrequest
    }

    /**
     * Gets the request xml for a result instance id
     */
    private String getQueryDefinitionXML(String resultInstanceId) {
        String xmlrequest = ''

	String sql = '''
		select REQUEST_XML
		from I2B2DEMODATA.QT_QUERY_MASTER c
		INNER JOIN I2B2DEMODATA.QT_QUERY_INSTANCE a ON a.QUERY_MASTER_ID=c.QUERY_MASTER_ID
		INNER JOIN I2B2DEMODATA.QT_QUERY_RESULT_INSTANCE b ON a.QUERY_INSTANCE_ID=b.QUERY_INSTANCE_ID
		WHERE RESULT_INSTANCE_ID = ?'''
	eachRow(sql, [resultInstanceId]) { row ->
	    def clob = rowGet(row, 'REQUEST_XML', Object)
	    xmlrequest = clobToString(clob)
	}

	xmlrequest
    }

    /**
     * Gets a comma delimited list of subjects for a result instance id
     */
    String getSubjects(String resultInstanceId) {
        if (resultInstanceId == null) {
            return null
        }

	String sql = '''
		select distinct patient_num
		from I2B2DEMODATA.qt_patient_set_collection
		where result_instance_id = ?
		  AND patient_num IN (
		     select patient_num
		     from I2B2DEMODATA.patient_dimension
		     where sourcesystem_cd not like '%:S:%'
		  )'''

	List<Long> ids = []
	eachRow(sql, [resultInstanceId]) { row ->
	    ids << rowGet(row, 'PATIENT_NUM', Long)
        }

	ids.join ','
    }

    /**
     * Subjects for a result instance id.
     */
    List<String> getSubjectsAsList(String resultInstanceId) {
        checkQueryResultAccess resultInstanceId

	List<String> subjectIds = []
	String sql = '''
		select distinct patient_num
		from I2B2DEMODATA.qt_patient_set_collection
		where result_instance_id = ?'''
	eachRow(sql, [resultInstanceId]) { row ->
	    subjectIds << rowGet(row, 'PATIENT_NUM', String)
	}

	subjectIds
    }

    /**
     * Subjects for a list of sample ids.
     */
    List<String> getSubjectsAsListFromSample(List sampleIdList) {

	List<String> subjectIds = []

	String sql = '''
		select distinct PATIENT_ID
		from DEAPP.DE_SUBJECT_SAMPLE_MAPPING
		where SAMPLE_ID in (''' + listToIN(sampleIdList) + ')'
	eachRow(sql) { row ->
	    subjectIds << rowGet(row, 'PATIENT_ID', String)
	}

	subjectIds
    }

    /**
     * Gets a list of subjects for a list of sample ids.
     */
    List<Long> getSubjectsAsListFromSampleLong(List sampleIdList) {

	List<Long> subjectIds = []

	String sql = '''
		select distinct PATIENT_ID
		from DEAPP.DE_SUBJECT_SAMPLE_MAPPING
		where SAMPLE_ID in (''' + listToIN(sampleIdList) + ')'

	eachRow(sql) { row ->
	    subjectIds << rowGet(row, 'PATIENT_ID', Long)
	}

	subjectIds
    }

    String getConcepts(String resultInstanceId) {

        if (resultInstanceId == null) {
            return null
        }

        String xmlrequest = getQueryDefinitionXML(resultInstanceId)

	Document doc = parseXml(xmlrequest)
	NodeList nodes = (NodeList) newXPath().evaluate('//item/item_key', doc, XPathConstants.NODESET)

	StringBuilder concepts = new StringBuilder()
	for (int i = 0; i < nodes.length; i++) {
	    String key = nodes.item(i).textContent
	    String conceptcds = getConceptCodeFromKey(key)  //should only return the exact concept_cd not the children
	    if (concepts) {
		concepts << ','
            }
	    if (conceptcds) {
		concepts << conceptcds
	    }
        }

	concepts
    }

    List<String> getConceptsAsList(String resultInstanceId) {

	String xmlrequest = getQueryDefinitionXML(resultInstanceId)

	List<String> concepts = []
	Document doc = parseXml(xmlrequest)
	NodeList nodes = (NodeList) newXPath().evaluate('//item/item_key', doc, XPathConstants.NODESET)
	for (int i = 0; i < nodes.length; i++) {
	    //should only return the exact concept_cd not the children
	    String conceptcds = getConceptCodeFromKey(nodes.item(i).textContent)
	    concepts << conceptcds
        }

	concepts
    }

    private String filterSubjectIdByBiomarkerData(String ids) {
	if (!ids) {
	    return ids
        }

	String sql = '''
		SELECT distinct s.patient_id
		FROM DEAPP.de_subject_sample_mapping s
		WHERE s.patient_id IN (''' + ids + ')'

	StringBuilder fids = new StringBuilder()

	eachRow(sql) { row ->
	    String st = rowGet(row, 'patient_id', String)
	    if (fids) {
		fids << ','
            }
	    fids << st
        }

	fids
    }

    void getHeatMapData(String pathwayName, String sids1, String sids2, String concepts1,
	                String concepts2, String timepoint1, String timepoint2, String sample1,
	                String sample2, String rbmPanels1, String rbmPanels2, String datatype,
	                GenePatternFiles gpf, boolean fixlast, boolean rawdata, String analysisType) {

	Assert.notNull datatype, 'Please choose a platform for analysis.'

        //  For most cases, GenePattern server cannot accept gct file with empty expression ratio.
        //  Use 0 rather than empty cell. However, Comparative Marker Select needs to use empty space
	String whiteString = analysisType == 'Select' ? GENE_PATTERN_WHITE_SPACE_EMPTY : GENE_PATTERN_WHITE_SPACE_DEFAULT

        //Get a distinct list of the patients we have data on. Queries 'de_subject_sample_mapping'.
        String ids1 = filterSubjectIdByBiomarkerData(sids1)
        String ids2 = filterSubjectIdByBiomarkerData(sids2)

        //Check to see if we actually had data in the table.
	Assert.isTrue ids1?.size() > 0 && ids2?.size() > 0, 'No heatmap data for the given subjects.'

        //Get pretty names for the subjects.
        String[] subjectNameArray = getSubjectNameArray(ids1, ids2, 'S1_', 'S2_')

        try {
            gpf.openGctFile()
            gpf.openCSVFile()

            //Write cls file
	    gpf.writeClsFile ids1, ids2

	    int rowCount = 0
	    int numCols = 0
	    Closure numColumnsClosure = { ResultSetMetaData meta -> numCols = meta.columnCount }

            //Determine if we are dealing with MRNA or Protein data.
            if (datatype.toUpperCase() == 'MRNA_AFFYMETRIX') {

		String intensityType = rawdata ? 'RAW' : 'LOG2'

                // handle *
                if (fixlast) {
                    String[] newNameArray = new String[subjectNameArray.length + 1]
                    newNameArray[subjectNameArray.length] = '*'
		    System.arraycopy subjectNameArray, 0, newNameArray, 0, subjectNameArray.length
                    subjectNameArray = newNameArray
                }

		String query = createMRNAHeatmapBaseQuery(pathwayName, ids1, ids2, timepoint1,
							  timepoint2, sample1, sample2, intensityType)
		StringBuilder s = new StringBuilder()

		StringBuilder cs = new StringBuilder()

		Session session = sessionFactory.currentSession

                // execute your statement against the connection
                Statement st = null
                ResultSet rs = null
		Transaction trans = null
                try {
                    trans = session.beginTransaction()
		    Connection conn = session.connection()
                    st = conn.createStatement()
		    st.execute 'alter session enable parallel dml'
		    st.fetchSize = 5000
                    rs = st.executeQuery(query)
		    int totalCol = rs.metaData.columnCount

                    while (rs.next()) {
			cs.setLength 0
                        for (int count = 1; count < totalCol; count++) {
                            if (count > 1) {
				s << '\t'
				cs << ','
                            }

			    String sval = rs.getString(count)
                            if (sval != null) {
				if (sval == 'null') {
				    s << whiteString
				    cs << whiteString
                                }
                                else {
				    s << sval
				    cs << sval
                                }
                            }
                            else {
				s << whiteString
				cs << whiteString
                            }
                        }
                        // special *
                        if (fixlast) {
			    s << '\t' << '0'
			    cs << ',' << '0'
                        }
			rowCount++
                        // write to csv file to improve performance
			s << '\n'
			gpf.writeToCSVFile cs.toString()
                    }
                }
                finally {
		    rs?.close()
		    st?.close()
                    trans.commit()
                }

		gpf.createGctHeader rowCount, subjectNameArray, '\t'
		gpf.writeToGctFile s.toString()
            }
            else if (datatype.toUpperCase() == 'RBM') {
		StringBuilder s = new StringBuilder()
		String query = createRBMHeatmapQuery(pathwayName, ids1, ids2, timepoint1,
						     timepoint2, rbmPanels1, rbmPanels2)
		List<GroovyRowResult> rows = new Sql(dataSource).rows(query, numColumnsClosure)

                // create header
		gpf.createGctHeader rows.size(), subjectNameArray, '\t'

		for (row in rows) {
		    s.setLength 0
                    for (int count in 0..<numCols - 1) {
			String val = rowGet(row, count, String)
                        // just impute zero; these are z scores
                        if (val == 'null' || val == null) {
                            val = whiteString
                        }
                        if (count > 0) {
			    s << '\t'
                        }
			s << val
                    }
		    rowCount++
		    gpf.writeToGctFile s.toString()
		    gpf.writeToCSVFile s.toString().replaceAll('\t', ',')
                }
            }
            else if (datatype.toUpperCase() == 'PROTEIN') {
                String query = createProteinHeatmapQuery(pathwayName, ids1, ids2,
							 concepts1, concepts2, timepoint1, timepoint2)
		StringBuilder s = new StringBuilder()
		List<GroovyRowResult> rows = new Sql(dataSource).rows(query, numColumnsClosure)

                // create header
		gpf.createGctHeader rows.size(), subjectNameArray, '\t'

		for (row in rows) {
		    s.setLength 0
		    String component = rowGet(row, 'component', String)
		    String geneSymbol = rowGet(row, 'GENE_SYMBOL', String)
		    if (component == null) {
			s << geneSymbol
                    }
                    else {
			s << component
                    }
		    s << '\t' << geneSymbol
		    for (int count in 2..<numCols - 1) {
			String val = rowGet(row, count, String)
                        if (val == 'null' || val == null) {
                            val = whiteString
                        }
                        if (count > 0) {
			    s << '\t'
                        }
			s << val
                    }

		    rowCount++
		    gpf.writeToGctFile s.toString()
		    gpf.writeToCSVFile s.toString().replaceAll('\t', ',')
                }
            }

	    Assert.isTrue rowCount > 0, 'No heatmap data for the specified parameters.'

	    if (analysisType == 'Cluster' && rowCount == 1) {
                throw new Exception('Not enough data for Hierarchical Clustering Analysis.')
            }
        }
        finally {
            gpf.closeGctFile()
            gpf.closeCSVFile()
        }
    }

    void getSurvivalAnalysisData(List<String> concepts1, List<String> concepts2, List<String> subjects1,
	                         List<String> subjects2, SurvivalAnalysisFiles saFiles) {

	Assert.notEmpty concepts1, 'The subset 1 has empty concepts'
	Assert.notEmpty concepts2, 'The subset 2 has empty concepts'
	Assert.notEmpty subjects1, 'The subset 1 has no subjects'
	Assert.notEmpty subjects2, 'The subset 2 has no subjects'
	Assert.notNull saFiles, 'The object saFiles cannot be null'

	List<SurvivalData> survivalDataList1 = getSurvivalDataForSubset(subjects1, concepts1)
	List<SurvivalData> survivalDataList2 = getSurvivalDataForSubset(subjects2, concepts2)

	int totalCount = survivalDataList1.size() + survivalDataList2.size()

	StringBuilder clsBuf = new StringBuilder()
	clsBuf << totalCount << ' 2 1\n# clsA clsB\n'

	StringBuilder dataBuf = new StringBuilder()
	dataBuf << 'name\ttime\tcensor\n'

	for (SurvivalData sd in survivalDataList1) {
	    clsBuf << '1 '
	    dataBuf << sd.subjectId << '\t' << sd.survivalTime.intValue() << '\t'
	    if (sd.isEvent) {
		dataBuf << '1\n'
            }
            else {
		dataBuf << '0\n'
            }
        }

	survivalDataList2.eachWithIndex { SurvivalData sd, int i ->
	    clsBuf << '2'
	    if (i != survivalDataList2.size() - 1) {
		clsBuf << ' '
            }
	    dataBuf << sd.subjectId << '\t' << sd.survivalTime.intValue() << '\t'
	    if (sd.isEvent) {
		dataBuf << '1\n'
            }
            else {
		dataBuf << '0\n'
            }
        }

	clsBuf << '\n'
	// The format for cls file in survival analysis is very strict.
	// The last flag is followed by a line break, and nothing else

	saFiles.clsFile.write clsBuf.toString()
	saFiles.dataFile.write dataBuf.toString()
    }

    private List<SurvivalData> getSurvivalDataForSubset(List<String> subjectStrList, List<String> conceptStrList) {
	List<Concept> conceptList = []
	for (String conceptBaseCode in conceptStrList) {
	    Concept concept = conceptService.getConceptByBaseCode(conceptBaseCode)
	    if (concept) {
		conceptList << concept
            }
        }

        SurvivalConcepts survivalConcepts = new SurvivalConcepts()
	defineConceptsForSurvivalAnalysis conceptList, survivalConcepts
	Assert.notNull survivalConcepts.conceptSurvivalTime,
	    'The concept for survival time is not defined'

	Map<String, SurvivalData> survivalDataMap = getSurvivalDataForSurvivalTime(
	    survivalConcepts.conceptSurvivalTime, subjectStrList)
	fillCensoringToSurvivalData survivalConcepts.conceptCensoring, survivalDataMap, subjectStrList
	fillEventToSurvivalData survivalConcepts.conceptEvent, survivalDataMap, subjectStrList

	survivalDataMap.values() as List
    }

    /**
     * The modeling of survival data is standardized. The censoring node will
     * has String '(PFSCENS)' or '(OSCENS)' in the name. It also will have
     * child nodes of 'Yes' and 'No' to indicate censored or Event.
     * The survival time node will have String '(PFS)' and '(OS)' in the name,
     * and the unit is in days.
     */
    private void defineConceptsForSurvivalAnalysis(List<Concept> concepts, SurvivalConcepts survivalConcepts) {
        if (survivalConcepts == null) {
            return
        }

	for (Concept concept in concepts) {
	    String shortName = concept.name
            if (isSurvivalData(shortName)) {
                if (survivalConcepts.conceptSurvivalTime != null) {
		    throw new Exception('More than one node with "(PFS)" or "(OS)" in the name that can be used as survival time node')
                }
                survivalConcepts.conceptSurvivalTime = concept
            }
            if (isSurvivalCensor(shortName)) {
                // Need to get the 'Yes' and 'No' child nodes
		for (Concept conceptChild in conceptService.getChildrenConcepts(concept)) {
		    String shortNameChild = conceptChild.name
                    if (shortNameChild.equalsIgnoreCase('Yes')) {
                        survivalConcepts.conceptCensoring = conceptChild
                    }
                    else if (shortNameChild.equalsIgnoreCase('No')) {
                        survivalConcepts.conceptEvent = conceptChild
                    }
                }
            }
        }
    }

    private boolean isSurvivalData(String conceptName) {
	for (String data in survivalDataList) {
	    if (conceptName.contains(data)) {
                return true
            }
        }

	false
    }

    private boolean isSurvivalCensor(String conceptName) {
	for (String data in censorFlagList) {
	    if (conceptName.contains(data)) {
                return true
            }
        }

	false
    }

    private Map<String, SurvivalData> getSurvivalDataForSurvivalTime(Concept conceptSurvivalTime,
	                                                             List<String> subjectStrList) {
	Assert.hasLength conceptSurvivalTime?.baseCode, 'The concept for survival time is not defined'

	Map<String, SurvivalData> dataMap = [:]
	String subjectIdListInStr = DBHelper.listToInString(subjectStrList)
	String sql = 'SELECT * FROM I2B2DEMODATA.observation_fact WHERE CONCEPT_CD = ?'
	if (subjectIdListInStr) {
	    sql += ' and PATIENT_NUM in (' + subjectIdListInStr + ')'
	}
	eachRow(sql, [conceptSurvivalTime.baseCode]) { row ->
	    SurvivalData survivalData = new SurvivalData(
		subjectId: rowGet(row, 'patient_num', String),
		survivalTime: rowGet(row, 'nval_num', Float))
	    dataMap[survivalData.subjectId] = survivalData
        }

	dataMap
    }

    /**
     * For now the patients have to be in the same trial, for the sake of simplicity.
     */
    void getSNPViewerDataByProbe(String subjectIds1, String subjectIds2, List<Long> geneSearchIdList,
	                         List<String> geneNameList, List<String> snpNameList,
	                         SnpViewerFiles snpFiles, StringBuilder geneSnpPageBuf) {
	Assert.notNull snpFiles, 'The SNPViewerFiles object is not instantiated'
	Assert.notNull geneSnpPageBuf, 'The geneSnpPageBuf object is not instantiated'

        SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe()

        // For the patient numbers selected by users in subset 1 and subset 2
	List<Long>[] patientNumListArray = [
	    getPatientNumListFromSubjectIdStr(subjectIds1),
	    getPatientNumListFromSubjectIdStr(subjectIds2)]

        allDataByProbe.patientNumList_1 = patientNumListArray[0]
        allDataByProbe.patientNumList_2 = patientNumListArray[1]

        // Get SQL query String for all the subject IDs
	String subjectListStr = [subjectIds1, subjectIds2].findAll().join(', ')

        // Get the gene-snp map, and the snp set related to all the user-input genes.
        // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = [:]
	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
	Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = [:]
	getGeneWithSnpMapForGenes geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList

        // Get the gene-snp map for the user-selected SNPs.
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = [:]
	getGeneWithSnpMapForSnps geneSnpMapForSnp, snpNameList

	Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = []
	geneSnpMapList << geneSnpMapForGene
	geneSnpMapList << geneSnpMapForSnp
        Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = mergeGeneWithSnpMap(geneSnpMapList)

	Assert.notEmpty allGeneSnpMap, 'There is no SNP data for selected genes and SNP IDs'

        // Generate the web page to display the Gene and SNP selected by User
	getSnpGeneAnnotationPage geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap,
	    geneNameToGeneWithSnpMap, geneNameList, snpNameList

        Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap
	getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr

	StringBuilder sampleInfoBuf = new StringBuilder()
        List<SnpDataset> datasetList = allDataByProbe.datasetList
        List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList
	getSnpSampleInfo datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
        Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap

        Set<Long> allSnpIdSet = getSnpSet(allGeneSnpMap)
	getSNPDataByProbeByChrom datasetList, snpDataByChromMap, allSnpIdSet

        // Write the sample info text file for SNPViewer
	snpFiles.sampleFile << sampleInfoBuf.toString()

        // Write the xcn file
	File dataFile = snpFiles.dataFile
        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile))
        // Write the header column
	dataWriter.write 'SNP\tChromosome\tPhysicalPosition'
	for (String datasetName in datasetNameForSNPViewerList) {
	    dataWriter.write '\t' + datasetName + '\t' + datasetName + ' Call'
        }
	dataWriter.write '\n'
        // Write the data section, by chrom. Stop at the last used chrom in snpDataByChromMap
        List<String> sortedChromList = getSortedChromList(snpDataByChromMap.keySet())
	String lastChrom = sortedChromList[-1]
	for (String chrom in ALL_CHROMS) {
	    List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap[chrom]
	    if (snpDataByProbeList) {
                // SNPViewer has problem rendering single SNP without boundary blank SNPs.
		String[] chromEndProbeLine = CHROM_END_PROBE_LINE_MAP[chrom]
		dataWriter.write chromEndProbeLine[0]
                for (int i = 0; i < datasetList.size(); i++) {
		    dataWriter.write '\t2.0\tNC'
                }
		dataWriter.write '\n'

		for (SnpDataByProbe snpDataByProbe in snpDataByProbeList) {
		    dataWriter.write snpDataByProbe.snpName + '\t' + chrom + '\t' + snpDataByProbe.chromPos
                    String[][] dataArray = snpDataByProbe.dataArray
                    for (int i = 0; i < datasetList.size(); i++) {
			dataWriter.write '\t' + dataArray[i][0].trim() + '\t' + dataArray[i][1]
                    }
		    dataWriter.write '\n'
                }

		dataWriter.write chromEndProbeLine[1]
                for (int i = 0; i < datasetList.size(); i++) {
		    dataWriter.write '\t2.0\tNC'
                }
		dataWriter.write '\n'
            }
            else {
                // There is no snp data needed for this chrom
		String[] chromEndProbeLine = CHROM_END_PROBE_LINE_MAP[chrom]
		for (int i = 0; i < 2; i++) {
		    dataWriter.write chromEndProbeLine[i]
		    for (int j = 0; j < datasetList.size(); j++) {
			dataWriter.write '\t2.0\tNC'
                    }
		    dataWriter.write '\n'
                }
            }

	    if (chrom == lastChrom) {
                break
            }
        }

        dataWriter.close()
    }

    private void getSNPDataByProbeByChrom(List<SnpDataset> datasetList,
	                                  Map<String, List<SnpDataByProbe>> snpDataByChromMap,
					  Collection snpIds) {
	Assert.notEmpty datasetList, 'The datasetList is empty'
	Assert.notNull snpDataByChromMap, 'The snpDataByChromMap is null'
	Assert.notEmpty snpIds, 'The snpIds is empty'

        Sql sql = new Sql(dataSource)

	String trialName = datasetList[0].trialName
        // Get the order of each dataset in the compacted data String
	Map<Long, Integer> datasetCompactLocationMap = [:]
	String sqlStr = 'select snp_dataset_id, location from DEAPP.de_snp_data_dataset_loc where trial_name = ?'
	eachRow(sql, sqlStr, [trialName]) { row ->
	    Long datasetId = rowGet(row, 'snp_dataset_id', Long)
	    datasetCompactLocationMap[datasetId] = rowGet(row, 'location', Integer)
        }

        String snpIdListStr = getStringFromCollection(snpIds)
        // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
	sqlStr = '''
		select b.name, b.chrom, b.chrom_pos, c.snp_data_by_probe_id, c.snp_id,
		       c.probe_id, c.probe_name, c.trial_name, c.data_by_probe
		from DEAPP.de_snp_info b, DEAPP.de_snp_data_by_probe c
		where b.snp_info_id = c.snp_id
		  and c.trial_name = ?
		  and b.snp_info_id in (''' + snpIdListStr + ''')
            order by b.chrom, b.chrom_pos'''
	eachRow(sql, sqlStr, [trialName]) { row ->
	    String dataByProbe = clobToString(rowGet(row, 'data_by_probe', Object))
	    SnpDataByProbe snpDataByProbe = new SnpDataByProbe(
		snpDataByProbeId: rowGet(row, 'snp_data_by_probe_id', Long),
		snpInfoId: rowGet(row, 'snp_id', Long),
		snpName: rowGet(row, 'name', String),
		probeId: rowGet(row, 'probe_id', Long),
		probeName: rowGet(row, 'probe_name', String),
		trialName: rowGet(row, 'trial_name', String),
		chrom: rowGet(row, 'chrom', String),
		chromPos: rowGet(row, 'chrom_pos', Long),
		dataArray: getSnpDataArrayFromCompactedString(datasetList, datasetCompactLocationMap, dataByProbe))

	    List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap[snpDataByProbe.chrom]
            if (snpDataByProbeList == null) {
		snpDataByProbeList = []
		snpDataByChromMap[snpDataByProbe.chrom] = snpDataByProbeList
            }
	    snpDataByProbeList << snpDataByProbe
        }
    }

    private String[][] getSnpDataArrayFromCompactedString(List<SnpDataset> datasetList,
	                                                  Map<Long, Integer> datasetCompactLocationMap,
	                                                  String dataByProbe) {
        String[][] dataArray = new String[datasetList.size()][2]

        for (int i = 0; i < datasetList.size(); i++) {
	    int location = datasetCompactLocationMap[datasetList[i].id]
            // The snp data is compacted in the format of [##.##][AB] for copy number and genotype
	    String copyNumber = dataByProbe.substring(location * 7, location * 7 + 5)
	    String genotype = dataByProbe.substring(location * 7 + 5, location * 7 + 7)
            dataArray[i][0] = copyNumber
            dataArray[i][1] = genotype
        }

	dataArray
    }

    private String getStringFromCollection(Collection collection) {
	if (!collection) {
            return null
        }

	StringBuilder buf = new StringBuilder()
	for (obj in collection) {
	    if (buf) {
		buf << ', '
            }
            if (obj instanceof Long || obj instanceof Integer || obj instanceof Float || obj instanceof Double) {
		buf << obj
            }
            else {
		buf << QUOTE << obj << QUOTE
            }
        }

	buf
    }

    private List<Long> getPatientNumListFromSubjectIdStr(String subjectIds) {
	if (!subjectIds) {
            return null
        }

	List<Long> patientNumList = []
	for (String subjectId in subjectIds.split(',')) {
	    patientNumList << Long.valueOf(subjectId.trim())
        }

	patientNumList
    }

    private String getConceptDisplayName(String conceptId, Map<String, String> conceptIdToDisplayNameMap) {
	if (!conceptId || conceptIdToDisplayNameMap == null) {
            return null
        }

	String conceptDisplayName = conceptIdToDisplayNameMap[conceptId]
        if (conceptDisplayName == null) {
	    String sql = 'select name_char from I2B2DEMODATA.concept_dimension where concept_cd = ?'
	    eachRow(sql, [conceptId]) { row ->
		conceptDisplayName = rowGet(row, 'name_char', String)
            }
	}

	conceptDisplayName
    }

    private void getGeneWithSnpMapForGenes(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom,
	                                   Map<Long, GeneWithSnp> geneEntrezIdMap,
	                                   Map<String, GeneWithSnp> geneNameToGeneWithSnpMap,
	                                   List<Long> geneSearchIdList) {
	if (!geneSearchIdList) {
            return
        }
	Assert.notNull geneSnpMapByChrom, 'geneSnpMapByChrom is not instantiated'
	Assert.notNull geneEntrezIdMap, 'geneEntrezIdMap is not instantiated'

        String geneSearchIdListStr = getStringFromCollection(geneSearchIdList)

        // Get the gene entrez id
	String sqlStr = '''
		select unique_id, keyword
		from SEARCHAPP.search_keyword
		where and data_category = 'GENE'
		and search_keyword_id in (''' + geneSearchIdListStr + ')'
	StringBuilder geneEntrezIdListStr = new StringBuilder()
	Sql sql = new Sql(dataSource)
	eachRow(sql, sqlStr) { row ->
	    String uniqueId = rowGet(row, 'unique_id', String)
	    String geneEntrezIdStr = uniqueId.substring(uniqueId.indexOf(':') + 1).trim()
	    if (geneEntrezIdListStr) {
		geneEntrezIdListStr << ','
	    }
	    geneEntrezIdListStr << geneEntrezIdStr
	    GeneWithSnp gene = new GeneWithSnp(
		entrezId: Long.valueOf(geneEntrezIdStr),
		name: rowGet(row, 'keyword', String))
	    geneEntrezIdMap[gene.entrezId] = gene
	    geneNameToGeneWithSnpMap[gene.name] = gene
        }

        // Get the snp association and chrom mapping
	sqlStr = '''
		select a.entrez_gene_id, b.*
		from DEAPP.de_snp_gene_map a, DEAPP.de_snp_info b
		where a.snp_id = b.snp_info_id
		  and a.entrez_gene_id in (''' + geneEntrezIdListStr + ')'
	eachRow(sql, sqlStr) { row ->
	    Long snpId = rowGet(row, 'snp_info_id', Long)
	    String snpName = rowGet(row, 'name', String)
	    String chrom = rowGet(row, 'chrom', String)
	    Long chromPos = rowGet(row, 'chrom_pos', Long)
	    Long entrezId = rowGet(row, 'entrez_gene_id', Long)

	    GeneWithSnp gene = geneEntrezIdMap[entrezId]
            if (gene.chrom == null) {
                gene.chrom = chrom
            }
            else {
		if (gene.chrom != chrom) {
		    throw new Exception('Inconsistant SNP-Gene mapping in database: The Gene ' + gene.name +
					', with Entrez ID of ' + gene.entrezId + ', is mapped to chromosome ' +
					gene.chrom + ' and ' + chrom)
                }
            }

	    gene.snpMap[chromPos] = new SnpInfo(id: snpId, name: snpName, chrom: chrom, chromPos: chromPos)
        }

        // Organize the GeneWithSnp by chrom
	for (GeneWithSnp gene in geneEntrezIdMap.values()) {
	    if (gene.chrom == null || !gene.snpMap) {
                continue
            }
	    SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom[gene.chrom]
            if (genes == null) {
		genes = new TreeMap<>()
		geneSnpMapByChrom[gene.chrom] = genes
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> geneMap = genes[chromPosGene]
            if (geneMap == null) {
		geneMap = [:]
		genes[chromPosGene] = geneMap
            }
	    geneMap[gene.entrezId] = gene
        }
    }

    private void getGeneWithSnpMapForSnps(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom,
	                                  List<String> snpNameList) {
	if (!snpNameList) {
            return
        }

	Assert.notNull geneSnpMapByChrom, 'geneSnpMapByChrom is not instantiated'

        String snpNameListStr = getStringFromCollection(snpNameList)

	Map<Long, GeneWithSnp> geneEntrezIdMap = [:]
        // Get the snp association and chrom mapping
        Sql sql = new Sql(dataSource)
	String sqlStr = '''
		select a.entrez_gene_id, b.*
		from DEAPP.de_snp_gene_map a, DEAPP.de_snp_info b
		where a.snp_id = b.snp_info_id
		  and b.name in (''' + snpNameListStr + ')'
	eachRow(sql, sqlStr) { row ->
	    Long snpId = rowGet(row, 'snp_info_id', Long)
	    String snpName = rowGet(row, 'name', String)
	    String chrom = rowGet(row, 'chrom', String)
	    Long chromPos = rowGet(row, 'chrom_pos', Long)
	    Long entrezId = rowGet(row, 'entrez_gene_id', Long)

	    GeneWithSnp gene = geneEntrezIdMap[entrezId]
	    if (!gene) {
		gene = new GeneWithSnp(entrezId: entrezId)
		geneEntrezIdMap[gene.entrezId] = gene
            }
            if (gene.chrom == null) {
                gene.chrom = chrom
            }
            else {
		if (gene.chrom != chrom) {
		    throw new Exception('The Gene ' + gene.name + ', with Entrez ID of ' + gene.entrezId +
					', is on chromosome ' + gene.chrom + ' and ' + chrom)
                }
            }

	    gene.snpMap[chromPos] = new SnpInfo(id: snpId, name: snpName, chrom: chrom, chromPos: chromPos)
        }

        // Construct the unique_id list from Entrez IDs
	StringBuilder geneSearchStr = new StringBuilder()
	for (Map.Entry<Long, GeneWithSnp> entry in geneEntrezIdMap.entrySet()) {
	    if (geneSearchStr) {
		geneSearchStr << ','
            }
	    geneSearchStr << QUOTE << 'GENE:' << entry.key << QUOTE
        }

        // Get the gene name from search_keyword table
	sqlStr = '''
		select unique_id, keyword
		from SEARCHAPP.search_keyword
		where and data_category = 'GENE'
		  and unique_id in (''' + geneSearchStr + ')'
	eachRow(sql, sqlStr) { row ->
	    String uniqueId = rowGet(row, 'unique_id', String)
	    String geneEntrezIdStr = uniqueId.substring(uniqueId.indexOf(':') + 1).trim()
	    GeneWithSnp gene = geneEntrezIdMap[Long.valueOf(geneEntrezIdStr)]
	    gene.name = rowGet(row, 'keyword', String)
        }

        // Organize the GeneWithSnp by chrom
	for (GeneWithSnp gene in geneEntrezIdMap.values()) {
	    if (gene.chrom == null || !gene.snpMap) {
                continue
            }
	    SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom[gene.chrom]
            if (genes == null) {
		genes = new TreeMap<>()
		geneSnpMapByChrom[gene.chrom] = genes
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> geneMap = genes[chromPosGene]
            if (geneMap == null) {
		geneMap = [:]
		genes[chromPosGene] = geneMap
            }
	    geneMap[gene.entrezId] = gene
        }
    }

    /**
     * Merges the sorted snp in sorted gene, organized by chromosome.
     * In the rare case that snp are merged into a same gene, the chrom position
     * of the gene may change. Organize gene first.
     */
    private Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergeGeneWithSnpMap(
	Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> mapList) {

	Map<Long, GeneWithSnp> geneMap = new TreeMap<>()
	for (Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> map in mapList) {
	    if (!map) {
                continue
            }
	    for (SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMap in map.values()) {
		for (Map<Long, GeneWithSnp> entrezIdGeneMap in geneWithSnpMap.values()) {
		    for (Map.Entry<Long, GeneWithSnp> entrezIdGeneMapEntry in entrezIdGeneMap.entrySet()) {
			Long entrezId = entrezIdGeneMapEntry.key
			GeneWithSnp geneWithSnp = entrezIdGeneMapEntry.value
			GeneWithSnp geneWithSnpInMap = geneMap[entrezId]
                        if (geneWithSnpInMap == null) {
                            // First time to have this entrezId, use the existing gene structure
                            geneWithSnpInMap = geneWithSnp
			    geneMap[entrezId] = geneWithSnpInMap
                        }
                        else {
                            // The gene structure and associated snp list already exist
			    geneWithSnpInMap.snpMap.putAll geneWithSnp.snpMap
                        }
                    }
                }
            }
        }

	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergedMap = [:]
	for (GeneWithSnp gene in geneMap.values()) {
	    SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapByChrom = mergedMap[gene.chrom]
            if (geneWithSnpMapByChrom == null) {
		geneWithSnpMapByChrom = new TreeMap<>()
		mergedMap[gene.chrom] = geneWithSnpMapByChrom
            }
            Long chromPosGene = gene.snpMap.firstKey()
	    Map<Long, GeneWithSnp> entrezIdgeneMap = geneWithSnpMapByChrom[chromPosGene]
            if (entrezIdgeneMap == null) {
		entrezIdgeneMap = [:]
		geneWithSnpMapByChrom[chromPosGene] = entrezIdgeneMap
            }
	    entrezIdgeneMap[gene.entrezId] = gene
        }

	mergedMap
    }

    private void getSnpDatasetBySubjectMap(Map<Long, SnpDataset[]> snpDatasetBySubjectMap, String subjectListStr) {
	if (snpDatasetBySubjectMap == null || !subjectListStr) {
            return
        }

        // The display concept name like 'Normal Blood Lymphocyte' for dataset with conceptId of '1222211'
	Map<String, String> conceptIdToDisplayNameMap = [:]

        // Get the dataset list from subject lists, and organize them in pairs for each patient.
	String commonPlatformName = null    // To make sure there is only one platform among all the datasets
	String commonTrialName = null    // For now only one trial is allowed.

	String sql = 'select * from DEAPP.de_subject_snp_dataset where patient_num in (' + subjectListStr + ')'
	eachRow(sql) { row ->
	    String conceptId = rowGet(row, 'concept_cd', String)
	    SnpDataset snpDataset = new SnpDataset(
		id: rowGet(row, 'subject_snp_dataset_id', Long),
		datasetName: rowGet(row, 'dataset_name', String),
		conceptId: conceptId,
		conceptName: getConceptDisplayName(conceptId, conceptIdToDisplayNameMap),
		platformName: rowGet(row, 'platform_name', String),
		patientNum: rowGet(row, 'patient_num', Long),
		timePoint: rowGet(row, 'timepoint', String),
		subjectId: rowGet(row, 'subject_id', String),
		sampleType: rowGet(row, 'sample_type', String),
		pairedDatasetId: rowGet(row, 'paired_dataset_id', Long),
		patientGender: rowGet(row, 'patient_gender', String),
		trialName: rowGet(row, 'trial_name', String))
            if (commonPlatformName == null) {
                commonPlatformName = snpDataset.platformName
            }
	    else if (commonPlatformName != snpDataset.platformName) {
		throw new Exception('The platform for SnpDataset ' + snpDataset.datasetName + ', ' +
				    snpDataset.platformName + ', is different from previous platform ' + commonPlatformName)
            }
            if (commonTrialName == null) {
                commonTrialName = snpDataset.trialName
            }
	    else if (commonTrialName != snpDataset.trialName) {
		throw new Exception('The trial for SnpDataset ' + snpDataset.datasetName + ', ' +
				    snpDataset.trialName + ', is different from previous trial ' + commonTrialName)
            }

	    SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap[snpDataset.patientNum]
            if (snpDatasetPair == null) {
                snpDatasetPair = new SnpDataset[2]
		snpDatasetBySubjectMap[snpDataset.patientNum] = snpDatasetPair
            }
	    if (snpDataset.sampleType == SnpDataset.SAMPLE_TYPE_NORMAL) {
                snpDatasetPair[0] = snpDataset
            }
            else {
                snpDatasetPair[1] = snpDataset
            }
        }
    }

    private void getPatientGenderMap(String subjectListStr, Map<Long, String> patientGenderMap) {
	Assert.notNull patientGenderMap, 'The object patientGenderMap is not instantiated'
	if (!subjectListStr) {
            return
        }

	String sql = '''
		select patient_num, sex_cd
		from I2B2DEMODATA.patient_dimension
		where patient_num in (''' + subjectListStr + ')'
	eachRow(sql) { row ->
	    Long patientNum = rowGet(row, 'patient_num', Long)
	    String gender = rowGet(row, 'sex_cd', String)
            if (gender != null) {
		patientGenderMap[patientNum] = gender
            }
        }
    }

    private Set<Long> getSnpSet(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap) {
	if (!allGeneSnpMap) {
            return null
        }

	Set<Long> allSnpSet = []
	for (SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapChrom in allGeneSnpMap.values()) {
	    for (Map<Long, GeneWithSnp> geneMap in geneWithSnpMapChrom.values()) {
		for (GeneWithSnp gene in geneMap.values()) {
		    for (SnpInfo snp in gene.snpMap.values()) {
			allSnpSet << snp.id
                    }
		}
            }
	}

	allSnpSet
    }

    private void getSnpSampleInfo(List<SnpDataset> datasetList, List<String> datasetNameForSNPViewerList,
	                          List<Long>[] patientNumListArray, Map<Long, SnpDataset[]> snpDatasetBySubjectMap,
	                          StringBuilder sampleInfoBuf) {
	Assert.notNull datasetList, 'The datasetList is null'
	Assert.notNull patientNumListArray, 'The patient number list for two subsets cannot be null'
	Assert.notNull sampleInfoBuf, 'The StringBuilder for sample info text must be instantiated'

        // Organize the datasetList and SNPViewer dataset name List, also generate the SNPViewer sample info text in this pass
	sampleInfoBuf << 'Array\tSample\tType\tPloidy(numeric)\tGender\tPaired'
        for (int idxSubset = 0; idxSubset < 2; idxSubset++) {
	    List<Long> patientNums = patientNumListArray[idxSubset]
	    for (Long patientNum in patientNums) {
		SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap[patientNum]
		if (snpDatasetPair) {
		    String datasetNameForSNPViewer1 = null
		    SnpDataset snpDataset1 = snpDatasetPair[0]
		    SnpDataset snpDataset2 = snpDatasetPair[1]
		    if (snpDataset1) {
                        // Has the control dataset
			datasetNameForSNPViewer1 = 'S' + (idxSubset + 1) + '_' + snpDataset1.datasetName
			datasetList << snpDataset1
			datasetNameForSNPViewerList << datasetNameForSNPViewer1
			sampleInfoBuf << '\n' << datasetNameForSNPViewer1 << '\t' << datasetNameForSNPViewer1
			sampleInfoBuf << '\t' << snpDataset1.conceptName << '\t2\t' << snpDataset1.patientGender << '\t'
			if (snpDataset2) {
			    // Paired
			    sampleInfoBuf << 'Yes'
			}
                        else {
			    // Not paired
			    sampleInfoBuf << 'No'
                        }
		    }
		    if (snpDataset2) {
                        // Has the control dataset
			String datasetNameForSNPViewer2 = 'S' + (idxSubset + 1) + '_' + snpDataset2.datasetName
			datasetList << snpDataset2
			datasetNameForSNPViewerList << datasetNameForSNPViewer2
			sampleInfoBuf << '\n' << datasetNameForSNPViewer2 << '\t' << datasetNameForSNPViewer2
			sampleInfoBuf << '\t' << snpDataset2.conceptName << '\t2\t' << snpDataset2.patientGender << '\t'
			if (snpDataset1) {
			    // Paired
			    sampleInfoBuf << datasetNameForSNPViewer1
			}
                        else {
			    // Not paired
			    sampleInfoBuf << 'No'
                        }
                    }
                }
            }
        }
    }

    private void getSnpGeneAnnotationPage(StringBuilder geneSnpPageBuf,
	                                  Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap,
	                                  Map<Long, GeneWithSnp> geneEntrezIdMap,
	                                  Map<String, GeneWithSnp> geneNameToGeneWithSnpMap,
					  List<String> geneNameList, List<String> snpNameList) {
	geneSnpPageBuf << '<html><header></hearder><body><p>Selected Genes and SNPs</p>'
	geneSnpPageBuf << '<table width="100%" border="1" cellpadding="4" cellspacing="3">'
	geneSnpPageBuf << '<tr align="center"><th>Gene</th><th>SNP</th><th>Chrom</th><th>Position</th></tr>'
	Set<String> snpNameSet = []
	if (snpNameList) {
	    snpNameSet.addAll snpNameList
        }

	Set<String> geneNotUsedNameSet = []
	for (String geneName in geneNameList) {
	    if (!geneNameToGeneWithSnpMap.containsKey(geneName)) {
		geneNotUsedNameSet << geneName
            }
        }

	Set<String> snpUsedNameSet = []

	for (String chrom in ALL_CHROMS) {
	    SortedMap<Long, Map<Long, GeneWithSnp>> geneMapChrom = allGeneSnpMap[chrom]
	    for (Map<Long, GeneWithSnp> map in geneMapChrom.values()) {
		for (GeneWithSnp gene in map.values()) {
                    SortedMap<Long, SnpInfo> snpMap = gene.snpMap
                    String geneDisplay = gene.name
		    if (geneEntrezIdMap && geneEntrezIdMap[gene.entrezId] != null) {
                        // This gene is selected by user
			geneDisplay = '<font color="red">' + gene.name + '</font>'
                    }
		    geneSnpPageBuf << '<tr align="center" valign="top"><td rowspan="'
		    geneSnpPageBuf << snpMap.size() << '">' << geneDisplay << '</td>'
                    boolean firstEntry = true
		    for (SnpInfo snp in snpMap.values()) {
                        String snpDisplay = snp.name
			snpUsedNameSet << snpDisplay
			if (snpNameSet?.contains(snp.name)) {
                            // This SNP is entered by user
			    snpDisplay = '<font color="red">' + snp.name + '</font>'
                        }
			if (!firstEntry) {
			    geneSnpPageBuf << '<tr align="center">'
                        }
			geneSnpPageBuf << '<td>' << snpDisplay << '</td><td>' << snp.chrom << '</td><td>' << snp.chromPos << '</td></tr>'
                        firstEntry = false
                    }
                }
            }
        }
	geneSnpPageBuf << '</table>'

	if (geneNotUsedNameSet) {
	    geneSnpPageBuf << '<p>The user-selected genes that do not have matching SNP data: '
	    geneSnpPageBuf << geneNotUsedNameSet.join(', ') << '</p>'
        }

	if (snpNameList) {
	    Set<String> snpNotUsedNameSet = []// Need to get the list of SNPs that do not have data
	    for (String snpName in snpNameList) {
		if (snpUsedNameSet) {
		    if (!snpUsedNameSet.contains(snpName)) {
			snpNotUsedNameSet << snpName
                    }
                }
                else {
		    snpNotUsedNameSet << snpName
		}
            }
	    if (snpNotUsedNameSet) {
		geneSnpPageBuf << '<p>The user-selected SNPs that do not have data: '
		geneSnpPageBuf << snpNotUsedNameSet.join(', ') << '</p>'
            }
        }

	geneSnpPageBuf << '</body></html>'
    }

    void getSNPViewerDataByPatient(String subjectIds1, String subjectIds2, String chroms, SnpViewerFiles snpFiles) {
	Assert.notNull snpFiles, 'The SNPViewerFiles object is not instantiated'

        // For the patient numbers selected by users in subset 1 and subset 2
	List<Long>[] patientNumListArray = [
	    getPatientNumListFromSubjectIdStr(subjectIds1),
	    getPatientNumListFromSubjectIdStr(subjectIds2)]

        // Get SQL query String for all the subject IDs
	String subjectListStr = [subjectIds1, subjectIds2].findAll().join(', ')

	Map<Long, SnpDataset[]> snpDatasetBySubjectMap = [:]
	getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr
	if (!snpDatasetBySubjectMap) {
            throw new Exception('Error: The selected cohorts do not have SNP data.')
        }

	StringBuilder sampleInfoBuf = new StringBuilder()
	List<SnpDataset> datasetList = []
	List<String> datasetNameForSNPViewerList = []
	getSnpSampleInfo datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

        Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms)

        /** There is a bug in GenePattern SNPViewer. If there is no probe position information for previous chrom,
         * The display of chroms becomes erratic.
         * The work-around is to enter dummy data for starting and ending probes of the absent chrom, so 
         SNPViewer can display the chrom number correctly. Need to build a list of chroms to the last used chrom*/

        List<String> neededChroms = getSortedChromList(chroms)
	String lastChrom = neededChroms[-1]

	String platform = datasetList[0].platformName
        Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms)

        BufferedWriter dataWriter = new BufferedWriter(new FileWriter(snpFiles.dataFile))

        // Write the header column
	dataWriter.write 'SNP\tChromosome\tPhysicalPosition'
	for (String datasetName in datasetNameForSNPViewerList) {
	    dataWriter.write '\t' + datasetName + '\t' + datasetName + ' Call'
	}
	dataWriter.write '\n'

	for (String chrom in neededChroms) {
	    SnpProbeSortedDef probeDef = probeDefMap[chrom]
	    if (probeDef) {
                // This chrom is selected by user
                // Create the list of BufferedReader for SNP data for each dataset for this chrom
		List<StringLineReader> snpDataReaderList = []
		for (SnpDataset dataset in datasetList) {
		    Map<String, String> snpDataByChrom = snpDataByDatasetByChrom[dataset.id]
		    String snpDataStr = snpDataByChrom[chrom]
		    snpDataReaderList << new StringLineReader(snpDataStr)
                }

                String probeDefStr = probeDef.snpIdDef
		int numProbe = probeDef.numProbe
                StringLineReader probeReader = new StringLineReader(probeDefStr)
		for (int index = 0; index < numProbe; index++) {
                    String probeLine = probeReader.readLine()
		    if (!probeLine) {
			throw new Exception('The number ' + index +
					    ' line in probe definition file for chromosome ' + chrom + ' is empty')
                    }
		    dataWriter.write probeLine

		    for (StringLineReader dataReader in snpDataReaderList) {
			dataWriter.write '\t' + dataReader.readLine()
                    }
		    dataWriter.write '\n'
                }
            }
            else {
                // This chrom need dummy data for the starting and ending probes
		String[] endProbeLines = CHROM_END_PROBE_LINE_MAP[chrom]
		dataWriter.write endProbeLines[0]
		for (SnpDataset dataset in datasetList) {
		    dataWriter.write '\t2.0\tNC'
                }
		dataWriter.write '\n'

		dataWriter.write endProbeLines[1]
		for (SnpDataset dataset in datasetList) {
		    dataWriter.write '\t2.0\tNC'
                }
		dataWriter.write '\n'
            }
	    if (chrom == lastChrom) {
		// Stop at the last needed chrom
                break
	    }
        }
        snpFiles.sampleFile << sampleInfoBuf
        dataWriter.close()
    }

    void getGwasDataByPatient(List<String> subjectIdList1, List<String> subjectIdList2,
	                      String chroms, GwasFiles gwasFiles) {
	Assert.notNull gwasFiles, 'The GwasFiles object is not instantiated'

	String subjectIds1 = subjectIdList1?.join(',') ?: ''
	String subjectIds2 = subjectIdList2?.join(',') ?: ''

        // For the patient numbers selected by users in subset 1 and subset 2
	List<Long>[] patientNumListArray = [
	    getPatientNumListFromSubjectIdStr(subjectIds1),
	    getPatientNumListFromSubjectIdStr(subjectIds2)]

        List<Integer> patientCountList = gwasFiles.patientCountList
	patientCountList << patientNumListArray[0].size()
	patientCountList << patientNumListArray[1].size()

        List<Integer> datasetCountList = gwasFiles.datasetCountList
	datasetCountList << getSNPDatasetIdList(subjectIds1).size()
	datasetCountList << getSNPDatasetIdList(subjectIds2).size()

        // Get SQL query String for all the subject IDs
	String subjectListStr = [subjectIds1, subjectIds2].findAll().join(', ')

	Map<Long, SnpDataset[]> snpDatasetBySubjectMap = [:]
	getSnpDatasetBySubjectMap snpDatasetBySubjectMap, subjectListStr

	Map<Long, String> patientGenderMap = [:]
	getPatientGenderMap subjectListStr, patientGenderMap

	StringBuilder sampleInfoBuf = new StringBuilder()
	List<SnpDataset> datasetList = []
	List<String> datasetNameForSNPViewerList = []
	getSnpSampleInfo datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf

        Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms)

        List<String> neededChroms = getSortedChromList(chroms)
	gwasFiles.chromList = neededChroms
	String platform = datasetList[0].platformName
        Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms)

	BufferedWriter mapWriter = new BufferedWriter(new FileWriter(gwasFiles.mapFile))
	for (String chrom in neededChroms) {
	    SnpProbeSortedDef probeDef = probeDefMap[chrom]
            StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef)
            String probeLine = probeReader.readLine()
	    while (probeLine) {
                String[] probeValues = probeLine.split('\t')
                String snpName = probeValues[0]
                String chromProbe = probeValues[1]
                String chromPosProbe = probeValues[2]
		mapWriter.write chromProbe + ' ' + snpName + ' 0 ' + chromPosProbe + '\n'

                probeLine = probeReader.readLine()
            }
        }
        mapWriter.close()

	BufferedWriter pedWriter = new BufferedWriter(new FileWriter(gwasFiles.pedFile))
        for (int idxSet = 1; idxSet <= patientNumListArray.length; idxSet++) {
            List<Long> patientNumList = patientNumListArray[idxSet - 1]
	    for (Long patientNum in patientNumList) {
		String gender = patientGenderMap[patientNum]
                String genderStr = 'other'
                if (gender != null) {
                    if (gender.equalsIgnoreCase('M')) {
                        genderStr = '1'
                    }
                    else if (gender.equalsIgnoreCase('F')) {
                        genderStr = '2'
                    }
                }

		SnpDataset[] snpDataArray = snpDatasetBySubjectMap[patientNum]
		for (SnpDataset dataset in snpDataArray) {
                    if (dataset == null) {
			// snpDataArray is means to contain [normal, disease]
                        continue
		    }

		    pedWriter.write dataset.datasetName + ' ' + dataset.datasetName + ' 0 0 ' + genderStr + '  ' + idxSet
		    Map<String, String> dataByChromMap = snpDataByDatasetByChrom[dataset.id]
		    for (String chrom in neededChroms) {
			String dataByChrom = dataByChromMap[chrom]
                        StringLineReader dataReader = new StringLineReader(dataByChrom)
                        String dataLine = dataReader.readLine()
			while (dataLine) {
                            String[] dataValues = dataLine.split('\t')
                            String genotype = dataValues[1].trim()
			    String genotype1 = genotype.substring(0, 1)
			    String genotype2 = genotype.substring(1)
			    if (genotype.equalsIgnoreCase('NC')) {
				genotype1 = '0'
				genotype2 = '0'
                            }
			    pedWriter.write '  ' + genotype1 + ' ' + genotype2

                            dataLine = dataReader.readLine()
                        }
                    }
		    pedWriter.write '\n'
                }
            }
        }

        pedWriter.close()
    }

    void runPlink(GwasFiles gwasFiles) {
	Assert.notNull gwasFiles, 'The object GwasFiles is not instantiated'

	File pedFile = gwasFiles.pedFile
        String pedFilePath = pedFile.absolutePath
	String mapFilePath = gwasFiles.mapFile.absolutePath
        String outputFileRoot = pedFile.parent + File.separator + gwasFiles.fileNameRoot

	String[] cmdLineArray = [plinkExecutable, '--ped', pedFilePath, '--map', mapFilePath,
		                 '--out', outputFileRoot, '--assoc', '--noweb']
	Process p = cmdLineArray.execute()
	OutputStream errStream = new ByteArrayOutputStream(4096)
        p.consumeProcessOutput()
	p.consumeProcessErrorStream errStream
        p.waitFor()

        File assocFile = new File(outputFileRoot + '.assoc')
	if (!assocFile.isFile()) {
            throw new Exception('PLINK failed to run')
        }
        gwasFiles.assocFile = assocFile
    }

    void reportGwas(String userName, GwasFiles gwasFiles, String querySummary1, String querySummary2) {
	Assert.notNull gwasFiles, 'The object GwasFiles is not instantiated'
        File assocFile = gwasFiles.assocFile
	Assert.isTrue assocFile != null && assocFile.isFile(), 'The PLINK output .assoc file does not exist'

	SortedMap<Float, String[]> mostSignificantSnps = new TreeMap<>()
	SortedMap<Float, String[]> significantSnps = new TreeMap<>()
	float pValueMostSignificant = 0.000001
	float pValueSignificant = 0.01
	Map<String, String[]> snpNameDataMap = [:]
	for (line in assocFile.readLines()) {
	    if (!line.startsWith(' CHR')) {
                String chrom = line.substring(0, 4).trim()
                String snpName = line.substring(4, 17).trim()
                String chromPos = line.substring(17, 28).trim()
                String chiSquare = line.substring(56, 69).trim()
                String pValue = line.substring(69, 82).trim()
                String oddsRatio = line.substring(82, 95).trim()

                Float pValueFloat = null
                try {
		    pValueFloat = Float.valueOf(pValue)
                }
		catch (ignored) {} // pValue could be 'NA'

                if (pValueFloat != null) {
		    String[] snpData = [snpName, chrom, chromPos, pValue, chiSquare, oddsRatio]
                    if (Float.compare(pValueFloat, pValueMostSignificant) < 0) {
			mostSignificantSnps[pValueFloat] = snpData
                    }
                    else if (Float.compare(pValueFloat, pValueSignificant) < 0) {
			significantSnps[pValueFloat] = snpData
                    }
		    snpNameDataMap[snpName] = snpData
                }
            }
        }

	SortedMap<Double, Set<String>> entrezScoreNegativeMap = new TreeMap<>()
        // It is difficult to sort descending in JDK 1.5. So sort the negative in ascending
	Map<String, Set<String>> entrezSnpMap = [:]
	Map<String, Set<String>> snpEntrezMap = [:]
	Map<String, String> entrezNameMap = [:]
	Map<String, String[]> neededSnpNameDataMap = [:]
        int neededCount = 1000
        int entryCount = 0

	for (String[] snpData in mostSignificantSnps.values()) {
            entryCount++
            if (entryCount > neededCount) {
                break
            }
	    neededSnpNameDataMap[snpData[0]] = snpData
        }

	for (String[] snpData in significantSnps.values()) {
            entryCount++
            if (entryCount > neededCount) {
                break
            }
	    neededSnpNameDataMap[snpData[0]] = snpData
        }

	getSnpGeneGwasScore neededSnpNameDataMap, entrezScoreNegativeMap, entrezSnpMap, snpEntrezMap, entrezNameMap

	StringBuilder buf = new StringBuilder()
	buf << '<html><head><title>Genome-Wide Association Study using PLINK</title></head>'
	buf << '<body><h2>Genome-Wide Association Study</h2>'
	String countStr1 = ''
	String countStr2 = ''
        List<Integer> patientCountList = gwasFiles.patientCountList
        if (patientCountList != null) {
	    if (patientCountList && patientCountList[0] != null) {
		countStr1 += '<br/>(' + patientCountList[0] + ' patients'
            }
	    if (patientCountList.size() > 1 && patientCountList[1] != null) {
		countStr2 += '<br/>(' + patientCountList[1] + ' patients'
            }
        }
        List<Integer> datasetCountList = gwasFiles.datasetCountList
        if (datasetCountList != null) {
	    if (datasetCountList && datasetCountList[0] != null) {
		countStr1 += ', ' + datasetCountList[0] + ' datasets)'
            }
	    if (datasetCountList.size() > 1 && datasetCountList[1] != null) {
		countStr2 += ', ' + datasetCountList[1] + ' datasets)'
            }
        }

	String pedFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.pedFile, userName)
	String mapFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.mapFile, userName)
	String assocFileUrl = gwasFiles.getFileUrlWithSecurityToken(gwasFiles.assocFile, userName)

	buf << '<table border="1" width="100%"><tr><th>Subset 1 Query ' << countStr1
	buf << '</th><th>Subset 2 Query ' << countStr2 << '</th></tr><tr><td>' << querySummary1
	buf << '</td><td>' << querySummary2 << '</td></tr></table>'

	List<String> chromList = gwasFiles.chromList
	StringBuilder chromBuf = new StringBuilder()
	for (String chrom in chromList) {
	    if (chromBuf) {
		chromBuf << ', '
            }
	    chromBuf << chrom
        }
	buf << '<h3>Chromosomes:  ' << chromBuf << '</p>'

	buf << '<h3>Data Files</h3>'
	buf << '<table cellpadding="2"><tr><td><a href="' << pedFileUrl
	buf << '">PED File</a></td><td><a href="' << mapFileUrl
	buf << '">MAP File</a></td><td><a href="' << assocFileUrl << '">Association File</a></td></tr></table>'

	buf << '<h3>Most Significantly Associated Genes</h3>'
	buf << '<table border="1" cellpadding="2"><tr><th>Gene</th><th>Total p-Value Score</th><th>SNP (p-Value)</th></tr>'
	for (Map.Entry<Double, Set<String>> scoreEntry in entrezScoreNegativeMap.entrySet()) {
	    double scoreNegative = scoreEntry.key
	    double score = -scoreNegative
	    Set<String> entrezSet = scoreEntry.value
	    for (String entrezId in entrezSet) {
		StringBuilder snpBuf = new StringBuilder()
		for (String snpName in entrezSnpMap[entrezId]) {
		    if (snpBuf) {
			snpBuf << ', '
                    }
		    snpBuf << snpName << ' (' << snpNameDataMap[snpName][3] << ')'
                }
		String geneName = entrezNameMap[entrezId]
                if (geneName == null) {
                    // This Entrez ID does not have a Gene:xxxx name in the search_keyword table
                    geneName = '(Gene: Entrez ID ' + entrezId + ')'
                }
		buf << '<tr><td>' << geneName << '</td><td>' << String.format('%.2f', score)
		buf << '</td><td>' << snpBuf << '</td></tr>'
            }
        }
	buf << '</table>'

	buf << '<h3>Most Significant SNPs</h3>'
	if (mostSignificantSnps) {
	    buf << '<table border="1" cellpadding="2">'
	    buf << '<tr><th>SNP</th><th>Chrom</th><th>Position</th><th>P Value</th>'
	    buf << '<th>Chi Square</th><th>Odds Ratio</th><th>Mapped to Genes</th></tr>'
	    for (String[] snpData in mostSignificantSnps.values()) {
                String snpName = snpData[0]
		StringBuilder geneBuf = new StringBuilder()
		for (String entrezId in snpEntrezMap[snpName]) {
		    if (geneBuf) {
			geneBuf << ', '
		    }
		    String geneName = entrezNameMap[entrezId]
                    if (geneName == null) {
                        // This Entrez ID does not have a Gene:xxxx name in the search_keyword table
                        geneName = '(Gene: Entrez ID ' + entrezId + ')'
                    }
		    geneBuf << geneName
                }
		buf << '<tr><td>' << snpData[0] << '</td><td>' << snpData[1] << '</td><td>'
		buf << snpData[2] << '</td><td>' << snpData[3] << '</td><td>' << snpData[4] << '</td><td>'
		buf << snpData[5] << '</td><td>' << geneBuf << '</td></tr>'
            }
	    buf << '</table>'
        }
        else {
	    buf << '<p>None</p>'
        }

	buf << '<h3>Significant SNPs</h3>'
	if (significantSnps) {
	    buf << '<table border="1" cellpadding="2"><tr><th>SNP</th><th>Chrom</th>'
	    buf << '<th>Position</th><th>P Value</th><th>Chi Square</th>'
	    buf << '<th>Odds Ratio</th><th>Mapped to Genes</th></tr>'
	    for (String[] snpData in significantSnps.values()) {
                String snpName = snpData[0]
		StringBuilder geneBuf = new StringBuilder()
		for (String entrezId in snpEntrezMap[snpName]) {
		    if (geneBuf) {
			geneBuf << ', '
		    }
		    String geneName = entrezNameMap[entrezId]
                    if (geneName == null) {
                        // This Entrez ID does not have a Gene:xxxx name in the search_keyword table
                        geneName = '(Gene: Entrez ID ' + entrezId + ')'
                    }
		    geneBuf << geneName
                }
		buf << '<tr><td>' << snpData[0] << '</td><td>' << snpData[1] << '</td><td>'
		buf << snpData[2] << '</td><td>' << snpData[3] << '</td><td>' << snpData[4]
		buf << '</td><td>' << snpData[5] << '</td><td>' << geneBuf << '</td></tr>'
            }
	    buf << '</table>'
        }
        else {
	    buf << '<p>None</p>'
        }
	buf << '</body></html>'

        gwasFiles.reportFile << buf.toString()
    }

    private void getSnpGeneGwasScore(Map<String, String[]> snpNameDataMap,
	                             SortedMap<Double, Set<String>> entrezScoreNegativeMap,
	                             Map<String, Set<String>> entrezSnpMap,
	                             Map<String, Set<String>> snpEntrezMap,
	                             Map<String, String> entrezNameMap) {
	Assert.notNull snpNameDataMap, 'The object snpNameDataMap is not instantiated'
	if (!snpNameDataMap) {
            return
        }

	Assert.notNull entrezScoreNegativeMap, 'The object entrezScoreMap is not instantiated'
	Assert.notNull entrezSnpMap, 'The object entrezSnpMap is not instantiated'
	Assert.notNull snpEntrezMap, 'The object snpEntrezMap is not instantiated'
	Assert.notNull entrezNameMap, 'The object entrezNameMap is not instantiated'

	StringBuilder snpNamesBuf = new StringBuilder()
	for (String snpName in snpNameDataMap.keySet()) {
	    if (snpNamesBuf) {
		snpNamesBuf << ','
            }
	    snpNamesBuf << QUOTE << snpName << QUOTE
        }

        Sql sql = new Sql(dataSource)
	String sqlStr = 'select * from DEAPP.de_snp_gene_map where snp_name in (' + snpNamesBuf + ')'
	eachRow(sql, sqlStr) { row ->
	    String snpName = rowGet(row, 'snp_name', String)
	    String entrezId = rowGet(row, 'entrez_gene_id', String)

	    Set<String> snpSet = entrezSnpMap[entrezId]
            if (snpSet == null) {
		snpSet = []
		entrezSnpMap[entrezId] = snpSet
            }
	    snpSet << snpName

	    Set<String> entrezSet = snpEntrezMap[snpName]
            if (entrezSet == null) {
		entrezSet = []
		snpEntrezMap[snpName] = entrezSet
            }
	    entrezSet << entrezId
        }

        // Contruct the entrezId list string, and get entrezNameMap
	StringBuilder entrezListBuf = new StringBuilder()
	for (String entrezId in entrezSnpMap.keySet()) {
	    if (entrezListBuf) {
		entrezListBuf << ','
	    }
	    entrezListBuf << QUOTE << 'GENE:' << entrezId << QUOTE
	}
	sqlStr = 'select keyword, unique_id from SEARCHAPP.search_keyword where unique_id in (' + entrezListBuf + ')'
	eachRow(sql, sqlStr) { row ->
	    String geneName = rowGet(row, 'keyword', String)
	    String entrezStr = rowGet(row, 'unique_id', String)
            String entrezId = entrezStr.substring(5)
	    entrezNameMap[entrezId] = geneName
        }

        // Calculate the total p-value score for each gene
	for (Map.Entry<String, Set<String>> entrezEntry in entrezSnpMap.entrySet()) {
	    String entrezId = entrezEntry.key
	    Set<String> snpSet = entrezEntry.value
            double score = 0
	    for (String snpName in snpSet) {
		String[] snpData = snpNameDataMap[snpName]
		if (snpData?.size() > 3) {
                    String pValueStr = snpData[3]
                    try {
                        double pValue = Double.parseDouble(pValueStr)
                        double pLog = Math.log10(pValue)
                        score += pLog
                    }
		    catch (ignored) {}
                }
            }

	    Set<String> entrezSet = entrezScoreNegativeMap[score]
            if (entrezSet == null) {
		entrezSet = []
		entrezScoreNegativeMap[score] = entrezSet
            }
	    entrezSet << entrezId
        }
    }

    private Map<Long, Map<String, String>> getSNPDataByDatasetByChrom(String subjectIds, String chroms) {
	if (!subjectIds) {
            return null
        }

	Map<Long, Map<String, String>> snpDataByDatasetByChrom = [:]
	// Map<[datasetId], Map<chrom, data>>

        // Get the list of dataset first, SNP data will be fetched later
	String sql = '''
		SELECT a.*, b.chrom as chrom, b.data_by_patient_chr as data
		FROM DEAPP.de_subject_snp_dataset a, DEAPP.de_snp_data_by_patient b
		WHERE a.subject_snp_dataset_id = b.snp_dataset_id
		  and a.patient_num in (''' + subjectIds + ''')
		  and b.chrom in (''' + getSqlStrFromChroms(chroms) + ')'

	eachRow(sql) { row ->
	    Long datasetId = rowGet(row, 'subject_snp_dataset_id', Long)
	    String chrom = rowGet(row, 'chrom', String)
	    String data = clobToString(rowGet(row, 'data', Object))

	    Map<String, String> dataByChromMap = snpDataByDatasetByChrom[datasetId]
            if (dataByChromMap == null) {
		dataByChromMap = [:]
		snpDataByDatasetByChrom[datasetId] = dataByChromMap
            }
	    dataByChromMap[chrom] = data
        }

	snpDataByDatasetByChrom
    }

    private List<String> getSortedChromList(String chromListStr) {
	Set<String> chromSet = []
	for (String chrom in chromListStr.split(',')) {
	    chromSet << chrom.trim()
        }

	getSortedChromList chromSet
    }

    private List<String> getSortedChromList(Set<String> chromSet) {
	if (!chromSet) {
            return null
	}

	if (chromSet.size() == 1) {
	    return chromSet as List
	}

	SortedMap<Integer, String> chromIndexMap = new TreeMap<>()
	for (String chrom in chromSet) {
	    for (int i = 0; i < ALL_CHROMS.length; i++) {
		if (chrom == ALL_CHROMS[i]) {
		    chromIndexMap[i] = chrom
		}
            }
        }

	chromIndexMap.values() as List
    }

    private String getSqlStrFromChroms(String chroms) {
	if (!chroms?.trim()) {
            return "'ALL'"
        }

        String[] values = chroms.split(',')
	StringBuilder buf = new StringBuilder()
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
		buf << ','
            }
	    buf << QUOTE << values[i] << QUOTE
        }

	buf
    }

    List<Long> getSNPDatasetIdList(String subjectIds) {
	if (!subjectIds) {
            return null
        }

	List<Long> idList = []
	String sql = '''
		SELECT subject_snp_dataset_id as id
		FROM DEAPP.de_subject_snp_dataset
		WHERE patient_num in (''' + subjectIds + ')'
	eachRow(sql) { row ->
	    idList << rowGet(row, 'id', Long)
        }

	idList ?: null
    }

    /**
     * Original example data files for SNPViewer and IGV use probe name such as 'SNP_A-1780419'.
     * It is better to use the target SNP id like 'rs6576700' in the data file,
     * so the tooltip in IGV will show the SNP rs id.
     */
    private Map<String, SnpProbeSortedDef> getSNPProbeDefMap(String platformName, String chroms) {
	if (!platformName?.trim()) {
            return null
        }

	Map<String, SnpProbeSortedDef> snpProbeDefMap = [:]
	String sql = '''
		SELECT snp_probe_sorted_def_id, platform_name, num_probe, chrom, snp_id_def
		FROM DEAPP.de_snp_probe_sorted_def
		WHERE platform_name = ?
		  and chrom in (''' + getSqlStrFromChroms(chroms) + ''')
		order by chrom'''
	eachRow(sql) { row ->
	    SnpProbeSortedDef probeDef = new SnpProbeSortedDef(
		platformName: rowGet(row, 'platform_name', String),
		numProbe: rowGet(row, 'num_probe', Integer),
		chrom: rowGet(row, 'chrom', String),
		snpIdDef: clobToString(rowGet(row, 'snp_id_def', Object)))
	    probeDef.id = rowGet(row, 'snp_probe_sorted_def_id', Long)
	    snpProbeDefMap[probeDef.chrom] = probeDef
	}

	snpProbeDefMap
    }

    private void fillCensoringToSurvivalData(Concept conceptCensoring, Map<String, SurvivalData> dataMap,
	                                     List<String> subjectStrList) {
	if (conceptCensoring == null || conceptCensoring.baseCode == null || !dataMap) {
	    return
	}

	String sql = 'SELECT * FROM I2B2DEMODATA.observation_fact WHERE CONCEPT_CD = ?'
	String subjectIdListInStr = DBHelper.listToInString(subjectStrList)
	if (subjectIdListInStr) {
	    sql += ' and PATIENT_NUM in (' + subjectIdListInStr + ')'
        }
	eachRow(sql, [conceptCensoring.baseCode]) { row ->
	    String subjectId = rowGet(row, 'patient_num', String)
	    String censoringStr = rowGet(row, 'tval_char', String)
	    if (censoringStr && (censoringStr.equalsIgnoreCase('Censoring') ||
			         censoringStr.equalsIgnoreCase('Yes'))) {
		// This patient is censored
		SurvivalData survivalData = dataMap[subjectId]
		if (survivalData) {
		    survivalData.isEvent = false
		}
            }
        }
    }

    private void fillEventToSurvivalData(Concept conceptEvent, Map<String, SurvivalData> dataMap,
	                                 List<String> subjectStrList) {
	if (conceptEvent == null || conceptEvent.baseCode == null || !dataMap) {
            return
        }

	String sql = 'SELECT * FROM I2B2DEMODATA.observation_fact WHERE CONCEPT_CD = ?'
        String subjectIdListInStr = DBHelper.listToInString(subjectStrList)
	if (subjectIdListInStr) {
	    sql += ' and PATIENT_NUM in (' + subjectIdListInStr + ')'
	}
	eachRow(sql, [conceptEvent.baseCode]) { row ->
	    String subjectId = rowGet(row, 'patient_num', String)
	    String censoringStr = rowGet(row, 'tval_char', String)
	    if (censoringStr && (censoringStr.equalsIgnoreCase('Event')) ||
		censoringStr.equalsIgnoreCase('No')) {
                // This patient is censored
		SurvivalData survivalData = dataMap[subjectId]
		if (survivalData) {
		    survivalData.isEvent = true
		}
            }
        }
    }

    private String getTrialName(String ids) {

	String sql = '''
		select distinct s.trial_name
		from DEAPP.de_subject_sample_mapping s
		where s.patient_id in (''' + ids + ''')
		  and s.platform = 'MRNA_AFFYMETRIX' '''
	StringBuilder trialNames = new StringBuilder()
	eachRow(sql) { row ->
	    if (trialNames) {
		trialNames << ','
            }
	    String tName = rowGet(row, 'trial_name', String)
            if (tName.equalsIgnoreCase('BRC Antidepressant Study')) {
                tName = 'BRC:mRNA:ADS'
            }
	    else if (tName.equalsIgnoreCase('BRC Depression Study')) {
                tName = 'BRC:mRNA:DS'
            }
	    trialNames << QUOTE << tName << QUOTE
        }

	trialNames
    }

    private String getAssayIds(String ids, String sampleTypes, String timepoint) {

	StringBuilder sql = new StringBuilder()
	sql << 'select distinct s.assay_id from DEAPP.de_subject_sample_mapping s where s.patient_id in ('
	sql << ids << ')'
	// check sample type
	if (sampleTypes) {
	    sql << ' AND s.sample_type_cd IN ' << convertStringToken(sampleTypes)
        }
	if (timepoint?.trim()) {
	    sql << ' AND s.timepoint_cd IN ' << convertStringToken(timepoint)
        }
	sql << ' ORDER BY s.assay_id'

	List assayIds = []
	eachRow(sql.toString()) { row ->
	    def assayId = rowGet(row, 'assay_id', Object)
	    if (assayId != null) {
		assayIds << assayId
            }
	}

	convertList assayIds
    }

    private String convertStringToken(String t) {
	String[] ts = t.split(',')
	StringBuilder s = new StringBuilder('(')
	for (int i = 0; i < ts.length; i++) {
	    if (i > 0) {
		s << ','
            }
	    s << QUOTE
	    s << ts[i]
	    s << QUOTE
        }
	s << ')'

	s
    }

    private String getGenes(String pathwayName) {

	String sql
        if (pathwayName.startsWith('GENESIG') || pathwayName.startsWith('GENELIST')) {
	    sql = '''
			select distinct bm.primary_external_id as gene_id
			from SEARCHAPP.search_keyword sk,
			     SEARCHAPP.search_bio_mkr_correl_fast_mv sbm,
			     BIOMART.bio_marker bm
			where sk.bio_data_id = sbm.domain_object_id
			and sbm.asso_bio_marker_id = bm.bio_marker_id
			and sk.unique_id = ?'''
        }
	else {
	    sql = '''
			select distinct bm.primary_external_id as gene_id
			from SEARCHAPP.search_keyword sk,
			     BIOMART.bio_marker_correl_mv sbm,
			     BIOMART.bio_marker bm
			where sk.bio_data_id = sbm.bio_marker_id
			and sbm.asso_bio_marker_id = bm.bio_marker_id
			and sk.unique_id = ?'''
        }

	List<String> genesArray = []
	eachRow(sql, [pathwayName]) { row ->
	    String geneId = rowGet(row, 'gene_id', String)
	    if (geneId != null) {
		genesArray << geneId
	    }
	}

	convertList genesArray
    }

    private String quoteCSV(String val) {
        StringBuilder s = new StringBuilder()

	if (val) {
	    String[] inArray = val.split(',')
	    s << QUOTE << inArray[0] << QUOTE
            for (int i = 1; i < inArray.length; i++) {
		s << ",'" << inArray[i] << QUOTE
            }
	}

	s
    }

    private String getSubjectIds1(String ids1, String ids2, String prefix1, String prefix2) {

        StringBuilder s = new StringBuilder()

	if (ids1) {
	    for (String id in ids1.split(',')) {
		if (s) {
		    s << ','
		}
		s << QUOTE << prefix1 << id << "' as " << prefix1 << id
            }
        }

	if (ids2) {
	    for (String id in ids2.split(',')) {
		if (s) {
		    s << ','
		}
		s << QUOTE << prefix2 << id << "' as " << prefix2 << id << ','
            }
        }

	s
    }

    // It is more meaningful to the scientists to use subject name such
    // as S1_GSE19539_IC022 in the heatmap, and to be consistent with genomic data

    private String[] getSubjectNameArray(String ids1, String ids2, String prefix1, String prefix2) {
	List<String> nameList = []

	if (ids1) {
	    nameList.addAll getSubjectNameList(ids1, prefix1)
        }

	if (ids2) {
	    nameList.addAll getSubjectNameList(ids2, prefix2)
	}

	nameList as String[]
    }

    private List<String> getSubjectNameList(String ids, String prefix) {

	String sql = '''
		SELECT sourcesystem_cd, patient_num
		FROM I2B2DEMODATA.patient_dimension
		WHERE patient_num IN (''' + ids + ''')
		order by patient_num'''

	List<String> nameList = []
	eachRow(sql) { row ->
	    String sourceSystemCd = rowGet(row, 'sourcesystem_cd', String)
	    Long patientNum = rowGet(row, 'patient_num', Long)
	    if (sourceSystemCd) {
		nameList << prefix + sourceSystemCd
            }
            else {
		nameList << prefix + patientNum
            }
        }

	nameList
    }

    private String createRBMHeatmapQuery(String prefix, String ids, String pathwayName,
	                                 String timepoint, String rbmPanels) {

	StringBuilder s = new StringBuilder()

	if (timepoint) {
	    s << "SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '"
	    s << prefix << "'|| t1.patient_id as subject_id "
	    s << 'FROM DEAPP.DE_SUBJECT_RBM_DATA t1, DEAPP.de_subject_sample_mapping t2 '
	    s << 'WHERE '
	    s << 't2.patient_id IN (' << ids << ') and '
	    s << 't2.timepoint_cd IN (' << quoteCSV(timepoint) << ') and '
	    s << 't1.data_uid = t2.data_uid and t1.assay_id=t2.assay_id'
        }
        else {
	    s << "SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '"
	    s << prefix << "'|| t1.patient_id as subject_id "
	    s << 'FROM DEAPP.DE_SUBJECT_RBM_DATA t1, DEAPP.de_subject_sample_mapping t2 '
	    s << 'WHERE t1.patient_id = t2.patient_id and t1.patient_id IN (' << ids << ')'
        }

	if (rbmPanels) {
	    s << ' and t2.rbm_panel IN (' << quoteCSV(rbmPanels) << ')'
        }

	if (pathwayName && !'SHOWALLANALYTES'.equalsIgnoreCase(pathwayName)) {
	    String genes = getGenes(pathwayName)
	    s << ' AND t1.gene_id IN (' << genes << ')'
        }

	s
    }

    private String createProteinHeatmapQuery(String prefix, String pathwayName,
                                             String ids, String concepts, String timepoint) {

	String sql = '''
			SELECT COUNT(*) as N
			FROM DEAPP.DE_SUBJECT_SAMPLE_MAPPING
			WHERE concept_code IN (''' + quoteCSV(concepts) + ')'

	int count = 0
	new Sql(dataSource).query(sql) { ResultSet rs ->
            while (rs.next()) {
		count = rs.toRowResult().N as int
            }
        }

        StringBuilder s = new StringBuilder()

	if (count == 0) {
	    if (timepoint) {
		s << "SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << 'FROM DEAPP.DE_SUBJECT_PROTEIN_DATA a, DEAPP.DE_pathway_gene c, DEAPP.de_pathway p, '
		s << 'DEAPP.DE_subject_sample_mapping b '
		s << 'WHERE c.pathway_id= p.id and '
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << 'a.gene_symbol = c.gene_symbol and '
		s << 'a.patient_id IN (' << ids << ') and '
		s << 'b.TIMEPOINT_CD IN (' << quoteCSV(timepoint) << ') and '
		s << 'a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint and '
		s << 'a.assay_id=b.assay_id '
	    }
	    else {
		s << "SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << 'FROM DEAPP.DE_SUBJECT_PROTEIN_DATA a, DEAPP.DE_pathway_gene c, DEAPP.de_pathway p '
		s << 'WHERE c.pathway_id= p.id and '
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << 'a.gene_symbol = c.gene_symbol and '
		s << 'a.patient_id IN (' + ids + ')'
	    }
	}
	else {
	    if (timepoint) {
		s << "select distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << 'FROM DEAPP.DE_SUBJECT_PROTEIN_DATA a, DEAPP.DE_pathway_gene c, DEAPP.de_pathway p, '
		s << 'DEAPP.DE_subject_sample_mapping b '
		s << 'WHERE c.pathway_id= p.id and '
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << 'a.gene_symbol = c.gene_symbol and '
		s << 'a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and '
		s << 'b.concept_code IN (' << quoteCSV(concepts) << ') and '
		s << 'a.patient_id IN (' << ids << ') and '
		s << 'b.TIMEPOINT_CD IN (' << quoteCSV(timepoint) << ') and '
		s << 'a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint '
	    }
	    else {
		s << "select distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << 'FROM DEAPP.DE_SUBJECT_PROTEIN_DATA a, DEAPP.DE_pathway_gene c, DEAPP.de_pathway p, '
		s << 'DEAPP.DE_subject_sample_mapping b '
		s << 'WHERE c.pathway_id= p.id and '
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << 'a.gene_symbol = c.gene_symbol and '
		s << 'a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and '
		s << 'b.concept_code IN (' << quoteCSV(concepts) << ') and '
		s << 'a.patient_id IN (' << ids << ')'
	    }
	}

	s
    }

    private String createProteinHeatmapQuery(String pathwayName, String ids1, String ids2, String concepts1,
	                                     String concepts2, String timepoint1, String timepoint2) {

        String columns = listHeatmapColumns('component', ids1, ids2, 'S1_', 'S2_') + ', star'

	String s1 = null
	if (ids1) {
            s1 = createProteinHeatmapQuery('S1_', pathwayName, ids1, concepts1, timepoint1)
        }
	String s2 = null
	if (ids2) {
            s2 = createProteinHeatmapQuery('S2_', pathwayName, ids2, concepts2, timepoint2)
        }

	String subjects = getSubjectIds1(ids1, ids2, 'S1_', 'S2_') + ", '*' as star"

	String sql
	if (s1) {
	    if (s2) {
		sql = 'SELECT ' + columns + ' FROM (' +
                    s1.replace('distinct ', ' ') + ' UNION ' + s2.replace('distinct ', ' ') +
                    ') PIVOT (avg(zscore) for subject_id IN (' + subjects +
                    ')) ORDER BY component, GENE_SYMBOL'
            }
            else {
		sql = 'SELECT ' + columns + ' FROM (' + s1 +
                    ') PIVOT (avg(zscore) for subject_id IN (' + subjects +
                    ')) ORDER BY component, GENE_SYMBOL'
            }
        }
        else {
	    sql = 'SELECT ' + columns + ' FROM (' + s2 +
                ') PIVOT (avg(zscore) for subject_id IN (' + subjects +
                ')) ORDER BY component, GENE_SYMBOL'
        }

	sql
    }

    private String createRBMHeatmapQuery(String pathwayName, String ids1, String ids2, String timepoint1,
	                                 String timepoint2, String rbmPanels1, String rbmPanels2) {

        String columns = listHeatmapColumns('antigen_name', ids1, ids2, 'S1_', 'S2_') + ', star'

	String s1 = null
	if (ids1) {
	    s1 = createRBMHeatmapQuery('S1_', ids1, pathwayName, timepoint1, rbmPanels1)
	}
	String s2 = null
	if (ids2) {
	    s2 = createRBMHeatmapQuery('S2_', ids2, pathwayName, timepoint2, rbmPanels2)
	}
	String subjects = getSubjectIds1(ids1, ids2, 'S1_', 'S2_') + ", '*' as star"

	String sql
	if (s1) {
	    if (s2) {
		sql = 'SELECT ' + columns + ' FROM (' +
                    s1.replace('distinct ', ' ') + ' UNION ' + s2.replace('distinct ', ' ') +
                    ') PIVOT (avg(value) for subject_id IN (' + subjects +
                    ')) ORDER BY ANTIGEN_NAME, GENE_SYMBOL'
            }
            else {
		sql = 'SELECT ' + columns + ' FROM (' + s1 +
                    ') PIVOT (avg(value) for subject_id IN (' + subjects +
                    ')) ORDER BY ANTIGEN_NAME, GENE_SYMBOL'
            }
        }
        else {
	    sql = 'SELECT ' + columns + ' FROM (' + s2 +
                ') PIVOT (avg(value) for subject_id IN (' + subjects +
                ')) ORDER BY ANTIGEN_NAME, GENE_SYMBOL'
        }

	sql
    }

    /**
     *  Compose a list of columns used by Heatmap and then trim average value
     *
     * @param biomarker probeset (mRNA), component (Protein) and antigen_name (RBM)
     * @param prefix1 usually use 'S1_'
     * @param prefix2 usually use 'S2_'
     */
    private String listHeatmapColumns(String biomarker, String ids1, String ids2, String prefix1, String prefix2) {

        StringBuilder s = new StringBuilder()
	s << ' ' << biomarker << ', gene_symbol '

	if (ids1) {
	    for (String id in ids1.split(',')) {
		s << ', round(' << prefix1 << id << ', 4) as ' << prefix1 << id
            }
        }

	if (ids2) {
	    for (String id in ids2.split(',')) {
		s << ', round(' << prefix2 << id << ', 4) as ' << prefix2 << id
            }
        }

	s
    }

    /**
     * heatmap query that takes intensity type
     */
    private String createMRNAHeatmapBaseQuery(String pathwayName, String ids1, String ids2,
	                                      String timepoint1, String timepoint2, String sample1,
	                                      String sample2, String intensityType, boolean count = false) {

	String columns = count ? ' COUNT(*) ' :
	    listHeatmapColumns('probeset', ids1, ids2, 'S1_', 'S2_') + ', star'

	String s1 = null
	if (ids1) {
	    s1 = createMRNAHeatmapPathwayQuery('S1_', ids1, pathwayName, timepoint1, sample1, intensityType)
	}
	String s2 = null
	if (ids2) {
	    s2 = createMRNAHeatmapPathwayQuery('S2_', ids2, pathwayName, timepoint2, sample2, intensityType)
        }

        // we have to use the log2_intensity to make the analysiscontroller happy..
        String intensityColumn = 'LOG2_INTENSITY'
	String subjects = getSubjectIds1(ids1, ids2, 'S1_', 'S2_') + ", '*' as star"

	String sql
	if (s1) {
	    if (s2) {
		sql = 'SELECT ' + columns + ' FROM (' +
                    s1 + ' UNION ' + s2 +
                    ') PIVOT (avg(' + intensityColumn + ') for subject_id IN (' + subjects +
                    ')) '
                if (!count) {
		    sql += ' ORDER BY PROBESET, GENE_SYMBOL'
                }
            }
            else {
		sql = 'SELECT ' + columns + ' FROM (' + s1 +
                    ') PIVOT (avg(' + intensityColumn + ') for subject_id IN (' + subjects +
                    '))'
                if (!count) {
		    sql += ' ORDER BY PROBESET, GENE_SYMBOL'
                }
            }
        }
        else {
	    sql = 'SELECT ' + columns + ' FROM (' + s2 +
                ') PIVOT (avg(' + intensityColumn + ') for subject_id IN (' + subjects +
                '))'
            if (!count) {
		sql += ' ORDER BY PROBESET, GENE_SYMBOL'
            }
        }

	sql
    }

    private String createMRNAHeatmapPathwayQuery(String prefix, String ids, String pathwayName,
	                                         String timepoint, String sampleTypes, String intensityType) {

        //Get the list of trial names based on 
        String trialNames = getTrialName(ids)
        String assayIds = getAssayIds(ids, sampleTypes, timepoint)

	Assert.notEmpty assayIds, 'No heatmap data for the specified parameters.'

        String intensityCol = 'zscore'
        if ('RAW' == intensityType) {
            intensityCol = 'RAW_INTENSITY'

            //check if we have sufficient raw data to run gp query
	    float goodPct = 0
	    String sql = '''
			select DISTINCT
			/*+ parallel(de_subject_microarray_data,4) */
			/*+ parallel(de_mrna_annotation,4) */
			count(distinct a.raw_intensity)/count(*) as pct_good
			FROM DEAPP.de_subject_microarray_data a,
			     DEAPP.de_mrna_annotation b
			WHERE a.probeset_id = b.probeset_id
			  AND a.trial_name IN (''' + trialNames + ''')
			  AND a.assay_id IN (''' + assayIds + ')'

	    eachRow(sql) { row ->
		goodPct = rowGet(row, 0, Float)
	    }

            if (goodPct == 0) {
                throw new Exception('No raw data for Comparative Marker Selection.')
            }
        }

	String s = '''
		select DISTINCT
		/*+ parallel(de_subject_microarray_data,4) */
		/*+ parallel(de_mrna_annotation,4) */
		b.PROBE_ID || ':' || b.GENE_SYMBOL as PROBESET,
		b.GENE_SYMBOL, a.''' + intensityCol + " as LOG2_INTENSITY, '" + prefix + "' || a.patient_ID as subject_id" + '''
		FROM DEAPP.de_subject_microarray_data a,
		     DEAPP.de_mrna_annotation b
		WHERE a.probeset_id = b.probeset_id
		  AND a.trial_name IN (''' + trialNames + ''')
		  AND a.assay_id IN (''' + assayIds + ')'
	if (pathwayName) {
	    s + ' AND b.gene_id IN (' + getGenes(pathwayName) + ')'
        }
	else {
	    s
        }
    }

    private String convertList(List idList) {
        StringBuilder s = new StringBuilder()
        int i = 0
        for (id in idList) {
	    if (i < 1000) {
		if (s) {
		    s << ','
                }
		s << id
            }
            else {
                break
            }
            i++
        }

	s
    }

    /**
     * Minimal conversion of metadataxml to JSON format
     */
    Map metadataxmlToJSON(String xml) {

	boolean okToUseValues = false
	String normalunits = ''
	if (xml) {
            try {
		Document doc = parseXml(xml)
		XPath xpath = newXPath()
		String key = evaluateContent(xpath, '//ValueMetadata/Oktousevalues', doc)
                if (key.equalsIgnoreCase('Y')) {
		    okToUseValues = true
                }

		normalunits = evaluateContent(xpath, '//ValueMetadata/UnitValues/NormalUnits', doc)
            }
	    catch (ignored) {
		logger.error 'BAD METADATAXML FOUND'
            }
	}

	[oktousevalues: okToUseValues, normalunits: normalunits]
    }

    List<String> getGenesForHaploviewFromResultInstanceId(String resultInstanceId) {
	checkQueryResultAccess resultInstanceId

	List<String> genes = []
	String sql = '''
                select distinct gene
                from DEAPP.haploview_data a
                inner join I2B2DEMODATA.qt_patient_set_collection b on a.I2B2_ID=b.patient_num
                where result_instance_id = ?
                order by gene asc'''
	eachRow(sql, [resultInstanceId as Long]) { row ->
	    String gene = rowGet(row, 'gene', String)
	    genes << gene
        }

	genes
    }

    /**
     * Gets the access level for a list of concept keys
     */
    Map<String, String> getConceptPathAccessCascadeForUser(List<String> paths) {
	Map<String, String> access = [:]

        //1)put all the children into the access list with default unlocked
	for (String e in paths) {
	    access[e] = 'Unlocked'
        }

        //2)if we are not an admin
	boolean admin = securityService.principal().isAdminOrDseAdmin()
	if (admin) {
	    logger.trace 'ADMINISTRATOR, SKIPPING PERMISSION CHECKING'
        }
	else { //level of nodes and not an admin
	    logger.trace 'NOT AN ADMINISTRATOR CHECKING PERMISSIONS'
            //3) get the secure paths that are in the list and secure them for later unlocking if necessary
	    for (SecureObjectPath sop in SecureObjectPath.executeQuery('SELECT DISTINCT s FROM SecureObjectPath s')) {
		setChildrenAccess access, sop.conceptPath, 'Locked'
            }
            //4) get the access levels this user has and unlock the locked resources available to him
	    List<Object[]> results = AuthUserSecureAccess.executeQuery('''
			SELECT DISTINCT ausa.accessLevel, sop.conceptPath
			FROM AuthUserSecureAccess ausa
			JOIN ausa.accessLevel
			JOIN ausa.secureObject.conceptPaths sop
			WHERE ausa.authUser is NULL
			   or ausa.authUser.id = :userId
			ORDER BY sop.conceptPath''',
			[userId: securityService.currentUserId()]) as List<Object[]>

		//return access levels for the children of this path that have them
		//for each of the ones that were found with access put their access levels into the object
		for (Object[] result in results) {
		SecureAccessLevel accessLevel = (SecureAccessLevel) result[0]
		String accessPath = result[1]
		setChildrenAccess access, accessPath, accessLevel.accessLevelName
            }
        }

	access
    }

    private void setChildrenAccess(Map<String, String> map, String path, String access) {
	for (Map.Entry<String, String> entry in map.entrySet()) {
	    if (entry.key.startsWith(path)) {
		entry.value = access
            }
	    logger.trace 'Setting key: {} set to value: {}', entry.key, access
	}
    }

    /****************************************New security stuff*************************/

    /**
     * check whether the current user is permitted to view Across Trials data.
     */
    private boolean isXTrials() {
	for (GrantedAuthority role in securityService.principal().authorities) {
	    if (role.authority == Roles.ACROSS_TRIALS.authority) {
		return true
            }
	}

	false
    }

    /**
     * Gets a list of studies the user is authorized to view
     */
    List<String> getAuthorizedStudies() {
        boolean admin = securityService.principal().isAdminOrDseAdmin()
        Map<String,String>  allStudies = getAllStudiesWithTokens()
        Map<String, String> tokenmap = getSecureTokensWithAccessForUser()
        List<String> authStudies = []

        if (admin) {
            allStudies.each { key, value ->
                authStudies << key
            }
        }
        else {
            allStudies.each { key, value ->
                if (value == 'EXP:PUBLIC') {
                    authStudies<< key
                } else if (tokenmap.containsKey('EXP:' + key)) {
                    authStudies << key
                }
            }
        }
        return authStudies
    }

    String getSqlInString(inList) {
        if (inList.getAt(0).isNumber())
            return inList.join(',')
        else
            return inList.collect{"'$it'"}.join(',')
    }

    Map<String,String> getAllStudiesWithTokens() {
        def studies = [:]
        Sql sql = new Sql(dataSource)
        String sqlt = 'SELECT sourcesystem_cd, secure_obj_token FROM i2b2metadata.i2b2_secure WHERE c_hlevel = 1'
        sql.eachRow(sqlt, [], { row ->
            studies.put(row.sourcesystem_cd, row.secure_obj_token);
        })
        return studies;
    }

    /**
     * Gets the children paths concepts of a parent key
     */
    private Map<String, String> getChildPathsWithTokensFromParentKey(String conceptKey) {
	String prefix = conceptKey.substring(0, conceptKey.indexOf('\\', 2))
        //get the prefix to put on to the fullname to make a key
	String fullname = conceptKey.substring(conceptKey.indexOf('\\', 2), conceptKey.length())

	Map<String, String> map = [:]
	int i = getLevelFromKey(conceptKey) + 1
	String sql = '''
			SELECT C_FULLNAME, SECURE_OBJ_TOKEN
			FROM i2b2metadata.i2b2_SECURE
			WHERE C_FULLNAME LIKE ? escape '\\'
			  AND c_hlevel = ?
			ORDER BY C_FULLNAME'''
	eachRow(sql, [Utils.asLikeLiteral(fullname) + '%', i]) { row ->
	    String conceptkey = prefix + rowGet(row, 'c_fullname', String)
	    map[keyToPath(conceptkey)] = rowGet(row, 'secure_obj_token', String)
	    logger.trace '@@found{}', conceptkey
	}

	map
    }

    Map<String, String> getSecureTokensForStudies(Collection<String> studyIds) {
	if (!studyIds) {
	    return [:]
        }

	Map<String, String> tokens = [:]
	String sql = '''
			SELECT sourcesystem_cd, secure_obj_token
			FROM i2b2metadata.i2b2_SECURE
			WHERE sourcesystem_cd IN (''' + listToIN(studyIds) + ''')
			AND c_hlevel = 1'''
	eachRow(sql) { row ->
	    String code = rowGet(row, 'sourcesystem_cd', String)
	    tokens[code] = rowGet(row, 'secure_obj_token', String)
        }

        tokens
    }

    Map<String, String> getSecureTokensWithAccessForUser() {
	List<String[]> results = AuthUserSecureAccess.executeQuery('''
				SELECT DISTINCT so.bioDataUniqueId, ausa.accessLevel.accessLevelName
            FROM AuthUserSecureAccess ausa
            JOIN ausa.accessLevel
            JOIN ausa.secureObject so
				WHERE ausa.authUser IS NULL
				   OR ausa.authUser.id = :userId''',
				[userId: securityService.currentUserId()]) as List<String[]>

	    Map<String, String> map = results.collectEntries { String[] result ->
	    String bioDataUniqueIdToken = result[0]
	    String accessLevelName = result[1]
	    [bioDataUniqueIdToken, accessLevelName]
	}

	map['EXP:PUBLIC'] = SecureAccessLevel.OWN

	map
    }

    /**
     * Gets the children with access for a concept
     */
    Map<String, String> getChildrenWithAccessForUserNew(String conceptKey) {
        logger.debug '----------------- getChildrenWithAccessForUserNew'

	String xTrialsTopNode = '\\\\' + ACROSS_TRIALS_TABLE_CODE + '\\' + ACROSS_TRIALS_TOP_TERM_NAME + '\\'
	boolean xTrialsCaseFlag = isXTrialsConcept(conceptKey) || (conceptKey == xTrialsTopNode)

	Map<String, String> results = [:]

	logger.trace 'input conceptKey = {}', conceptKey
	logger.trace 'user = {}', securityService.currentUsername()

        if (xTrialsCaseFlag) {
	    logger.trace 'XTrials for getChildrenWithAccessForUserNew'
	    logger.warn 'getChildrenWithAccessForUserNew - For cross trials, make no check at this time!!'

	    OntologyTerm node = conceptsResourceService.getByKey(conceptKey)
	    for (OntologyTerm term in node.children) {
		results[term.fullName] = 'view'
            }
        }
        else {
	    results = getAccess(getChildPathsWithTokensFromParentKey(conceptKey))
	}

	results
    }

    /**
     * Checks an arbitrary list of paths with tokens against users access list map (merge)
     */
    Map<String, String> getAccess(Map<String, String> pathsWithTokens) {
	Map<String, String> access = [:] //new map to merge the other two

	boolean admin = securityService.principal().isAdminOrDseAdmin()
	if (admin) {
	    logger.trace 'ADMINISTRATOR, SKIPPING PERMISSION CHECKING'
            //1)If we are an admin then grant admin to all the paths
	    for (String key in pathsWithTokens.keySet()) {
		access[key] = 'Admin'
		logger.trace 'putting {} with admin access', key
            }
	    return access //just set everything to admin and return it all
        }

	//if not admin merge the data from the two maps
	Map<String, String> tokens = getSecureTokensWithAccessForUser()
	for (String key in pathsWithTokens.keySet()) {
	    String childtoken = pathsWithTokens[key]
	    logger.trace 'Key:{} Token:{}', key, childtoken
            if (childtoken == null) {
		access[key] = 'VIEW' //give read access if no security token
            }
	    else if (tokens.containsKey(childtoken)) { //null tokens are assumed to be unlocked
		access[key] = tokens[childtoken] // found access for this token so put in access level
            }
	    else if (isXTrialsTopLevel(key) && isXTrials()) {
		access[key] = 'VIEW'
            }
	    else {
		access[key] = 'Locked' //didn't find authorization for this token
            }
        }

	logger.debug 'In getAccess: {}', access

	access
    }

    private boolean isXTrialsTopLevel(String nodeName) {
	'\\' + ACROSS_TRIALS_TOP_TERM_NAME + '\\' == nodeName
    }

    /**
     * renderQueryDefinition provides an XML based string given a result instance ID
     *
     * @param resultInstanceId - the result instance ID
     * @param title - the title for the query (e.g. subset 2)
     * @param pw - the StringWriter used to build the XML string
     */
    void renderQueryDefinition(String resultInstanceId, String title, Writer pw) {
	logger.debug 'renderQueryDefinition called with {} and {}', resultInstanceId, title
	if (resultInstanceId == null) {
	    return
        }

        try {
            String xmlrequest = getQueryDefinitionXML(resultInstanceId)
	    logger.debug xmlrequest

	    XPath xpath = newXPath()

	    Document doc = parseXml(xmlrequest)
	    NodeList panels = (NodeList) xpath.evaluate('//panel', doc, XPathConstants.NODESET)

	    pw.write '<table class="analysis">'
	    pw.write '<tr><th>' + title + '</th></tr>'
	    pw.write '<tr>'
	    pw.write '<td>'
	    logger.debug 'Iterating over the nodes...'
	    for (int p = 0; p < panels.length; p++) {
		Node panel = panels.item(p)
		Node panelnumber = evaluate(xpath, 'panel_number', panel)

		if (panelnumber?.textContent?.equalsIgnoreCase('21')) {
		    logger.debug 'Skipping the security panel in printing the output'
                    continue
                }

		if (p != 0 && p != panels.length) {
		    pw.write '<br><b>AND</b><br>'
                }

		Node invert = evaluate(xpath, 'invert', panel)
		if (invert?.textContent?.equalsIgnoreCase('1')) {
		    pw.write '<br><b>NOT</b><br>'
                }

		pw.write '<b>(</b>'

		NodeList items = (NodeList) xpath.evaluate('item', panel, XPathConstants.NODESET)
		for (int i = 0; i < items.length; i++) {
                    Node item = items.item(i)
		    if (i != 0 && i != (items.length)) {
			pw.write '<br><b>OR</b><br>'
                    }

		    Node key = evaluate(xpath, 'item_key', item)

		    String textContent = key.textContent
		    logger.debug 'Found item {}', textContent

		    Node valueinfo = evaluate(xpath, 'constrain_by_value', item)

		    pw.write textContent

                    if (valueinfo != null) {
			String operator = evaluateContent(xpath, 'value_operator', valueinfo)
			String constraints = evaluateContent(xpath, 'value_constraint', valueinfo)
			pw.write ' ' + operator + ' ' + constraints
                    }

		    valueinfo = evaluate(xpath, 'constrain_by_omics_value', item)
		    if (valueinfo) {
			String valueType = evaluateContent(xpath, 'omics_value_type', valueinfo)
			String operator = evaluateContent(xpath, 'omics_value_operator', valueinfo)
			String constraints = evaluateContent(xpath, 'omics_value_constraint', valueinfo)
			String selector = evaluateContent(xpath, 'omics_selector', valueinfo)
			String projection = evaluateContent(xpath, 'omics_projection_type', valueinfo)
			pw.write selector
			if (valueType == 'VCF') {
                            // TBD
                        }
                        // else if (value_type.equals {}   // other non-standard high-dim types here
                        else {
			    pw.write ' - ' + Projection.prettyNames.get(projection, projection) + ' ' + operator + ' '
			    if (operator == 'BETWEEN') {
                                String[] bounds = constraints.split(':')
                                if (bounds.length != 2) {
                                    logger.error "BETWEEN constraint type found with values not seperated by ':'"
				    pw.write constraints
                                }
                                else {
				    pw.write bounds.join(' and ')
                                }
                            }
                            else {
				pw.write constraints
                            }
                        }
                    }

                }
		pw.write '<b>)</b>'
            }
	    pw.write '</td></tr></table>'
        }
	catch (e) {
	    logger.error e.message, e
        }
    }

    String getSecureTokensCommaSeparated() {
        StringBuilder sb = new StringBuilder()
	for (String v in getSecureTokensWithAccessForUser().keySet()) { //have some kind of access to each of these tokens
	    if (sb) {
		sb << ','
            }
	    sb << QUOTE << v << QUOTE
        }

	sb
    }

    /**
     * Gets the children paths concepts of a parent key
     */
    Map<String, String> getRootPathsWithTokens() {
	Map<String, String> ls = [:]
	String sql = '''
			SELECT C_FULLNAME, SECURE_OBJ_TOKEN
			FROM i2b2metadata.i2b2_SECURE
			WHERE c_hlevel IN (-1, 0)
			ORDER BY C_FULLNAME'''
	eachRow(sql) { row ->
	    String fullname = rowGet(row, 'c_fullname', String)
	    // get the prefix to put on to the fullname to make a key
	    String prefix = fullname.substring(0, fullname.indexOf('\\', 2))
            String conceptkey = prefix + fullname
	    ls[keyToPath(conceptkey)] = rowGet(row, 'secure_obj_token', String)
	    logger.trace '@@found{}', conceptkey
	}

	// for across trials - mark top level know with special token for downstream access control
	ls['\\Across Trials\\'] = 'EXP:ACROSS_TRIALS'

	ls
    }

    private String listToIN(Collection<String> list) {
        StringBuilder sb = new StringBuilder()
        // need to make it less than 1000! -- temp solution
        int i = 0
        for (c in list) {
            //If the only thing submitted was 'ALL' we return an empty string just like there was nothinbg in the box.
	    if (c == 'ALL' && list.size() == 1) {
                break
            }

	    if (sb) {
		sb << ','
	    }
	    sb << QUOTE
	    sb << c.replace(QUOTE, "''")
	    sb << QUOTE
            i++
            if (i >= 1000) {
                break
            }
        }

	sb
    }

    /**
     * Gets the platforms found
     * For now, subids could be null due to complexity of workflow and user error
     */
    void fillHeatmapValidator(List<String> subids, List<String> conids, HeatmapValidator hv) {

        //If the list of subids does not have any elements, or it has only one element which is 'ALL'
	if (!subids || (subids.size() == 1 && subids[0] == 'ALL')) {
            subids = null
        }
	logger.trace 'validating heatmap: {} : {}', conids, subids

        Sql sql = new Sql(dataSource)

	fillHeatmapValidatorFor 'SAMPLE_TYPE_CD', subids, conids, hv, sql
        if (hv.validate()) {
            return
        }

	fillHeatmapValidatorFor 'TIMEPOINT_CD', subids, conids, hv, sql
        if (hv.validate()) {
            return
        }

	fillHeatmapValidatorFor 'CONCEPT_CODE', subids, conids, hv, sql
        if (hv.validate()) {
            return
        }

	fillHeatmapValidatorFor 'PLATFORM_CD', subids, conids, hv, sql
        if (hv.validate()) {
            return
        }

	fillHeatmapValidatorFor 'TISSUE_TYPE_CD', subids, conids, hv, sql
    }

    private void fillHeatmapValidatorFor(String column, List<String> subids, List<String> conids,
	                                 HeatmapValidator hv, Sql sql) {
	String sqlt = '''
			SELECT TISSUE_TYPE, TISSUE_TYPE_CD, PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD, SAMPLE_TYPE
			FROM DEAPP.DE_SUBJECT_SAMPLE_MAPPING
			WHERE '''
	if (subids) {
	    sqlt += 'PATIENT_ID IN (' + listToIN(subids) + ') AND '
        }
	sqlt += column + ' IN (' + listToIN(conids) + ') ' +
	    'GROUP BY PLATFORM, TIMEPOINT, TIMEPOINT_CD, SAMPLE_TYPE_CD,' +
	    ' SAMPLE_TYPE, TISSUE_TYPE, TISSUE_TYPE_CD'
	eachRow(sql, sqlt) { row ->
	    String platform = rowGet(row, 'PLATFORM', String)
	    if (platform != null) {
		hv.platforms << platform
	    }
	    String timePointCode = rowGet(row, 'TIMEPOINT_CD', String)
	    if (platform != null && timePointCode != null &&
		!(platform == 'RBM' && !timePointCode.contains(':Z:'))) {
		if (timePointCode != null) {
		    hv.timepoints << timePointCode
		}
		String timepoint = rowGet(row, 'TIMEPOINT', String)
		if (timepoint != null) {
		    hv.timepointLabels << timepoint
		}
	    }
	    String sampleTypeCode = rowGet(row, 'SAMPLE_TYPE_CD', String)
	    if (sampleTypeCode != null) {
		hv.samples << sampleTypeCode
	    }
	    String sampleType = rowGet(row, 'SAMPLE_TYPE', String)
	    if (sampleType != null) {
		hv.sampleLabels << sampleType
	    }
	    String tissueTypeCode = rowGet(row, 'TISSUE_TYPE_CD', String)
	    if (tissueTypeCode != null) {
		hv.tissues << tissueTypeCode
	    }
	    String tissueType = rowGet(row, 'TISSUE_TYPE', String)
	    if (tissueType != null) {
		hv.tissueLabels << tissueType
            }
        }
    }

    /**
     * Fill the cohort information requested.
     * For now, subids could be null due to complexity of workflow and user error
     * Incoming ci contains a list of codes. Outgoing ci contains codes:label maps
     */
    void fillCohortInformation(List<String> subids, List<String> conids, CohortInformation ci, int infoType) {
        //If the list of subids does not have any elements, or it has only one element which is 'ALL'
	if (!subids || (subids.size() == 1 && subids[0] == 'ALL')) {
            subids = null
        }

        Sql sql = new Sql(dataSource)
	String sqlt
        switch (infoType) {
            case CohortInformation.TRIALS_TYPE:
		ci.trials = []
		sqlt = 'select distinct modifier_cd from I2B2DEMODATA.observation_fact where '
		if (subids) {
                    sqlt += 'PATIENT_NUM in (' + listToIN(subids) + ') and '
                }
                sqlt += 'concept_cd in (' + listToIN(conids) + ')'
		eachRow(sql, sqlt) { row ->
		    ci.trials << rowGet(row, 'modifier_cd', String)
		}

		if (!ci.trials) {
		    sqlt = 'select distinct sourcesystem_cd from I2B2METADATA.i2b2 where c_basecode in (' + listToIN(conids) + ')'
		    eachRow(sql, sqlt) { row ->
			ci.trials << rowGet(row, 'sourcesystem_cd', String)
		    }
                }

                break
            case CohortInformation.PLATFORMS_TYPE:
		ci.platforms = []
		sqlt = '''
				select distinct platform
				from DEAPP.de_subject_sample_mapping
				where trial_name in (''' + listToIN(ci.trials) + ''')
				order by platform'''
		eachRow(sql, sqlt) { row ->
		    String platform = rowGet(row, 'platform', String)
		    ci.platforms << [platform     : platform,
				     platformLabel: 'MRNA_AFFYMETRIX' == platform ? 'MRNA' : platform]
		}
                break
            case CohortInformation.TIMEPOINTS_TYPE:
		ci.timepoints = []
		sqlt = '''
				select distinct timepoint, timepoint_cd
				from DEAPP.de_subject_sample_mapping
				where trial_name in (''' + listToIN(ci.trials) + ''')
				and platform in (''' + listToIN(ci.platforms) + ')'
		if (ci.platforms[0] == 'RBM') {
		    sqlt += ''' and instr(timepoint_cd, ':Z:') > 0'''
                }
		if (ci.gpls) {
                    sqlt += ' and gpl_id in(' + listToIN(ci.gpls) + ')'
                }
		if (ci.tissues) {
                    sqlt += ' and tissue_type_cd in(' + listToIN(ci.tissues) + ')'
                }
		if (ci.samples) {
                    sqlt += ' and sample_type_cd in (' + listToIN(ci.samples) + ')'
                }
		if (ci.rbmpanels) {
                    sqlt += ' and rbm_panel in (' + listToIN(ci.rbmpanels) + ')'
                }
                sqlt += ' order by timepoint'
		eachRow(sql, sqlt) { row ->
		    String timepointCode = rowGet(row, 'timepoint_cd', String)
		    if (timepointCode != null) {
			ci.timepoints << [timepointLabel: rowGet(row, 'timepoint', String),
					  timepoint     : timepointCode]
		    }
                }
                break
            case CohortInformation.SAMPLES_TYPE:
		ci.samples = []
		sqlt = '''
			select distinct sample_type, sample_type_cd
			from DEAPP.de_subject_sample_mapping
			where trial_name in (''' + listToIN(ci.trials) + ''')
			  and platform in (''' + listToIN(ci.platforms) + ')'
		if (ci.gpls) {
                    sqlt += ' and gpl_id in(' + listToIN(ci.gpls) + ')'
                }
                sqlt += ' order by sample_type'
		eachRow(sql, sqlt) { row ->
		    ci.samples << [sample     : rowGet(row, 'sample_type_cd', String),
				   sampleLabel: rowGet(row, 'sample_type', String)]
		}
                break
            case CohortInformation.TISSUE_TYPE:
		ci.tissues = []
		sqlt = '''
			select distinct tissue_type, tissue_type_cd
			from DEAPP.de_subject_sample_mapping
			where trial_name in (''' + listToIN(ci.trials) + ''')
			  and platform in (''' + listToIN(ci.platforms) + ')'
		if (ci.gpls) {
                    sqlt += ' and gpl_id in(' + listToIN(ci.gpls) + ')'
                }
		if (ci.samples) {
                    sqlt += ' and sample_type_cd in (' + listToIN(ci.samples) + ')'
                }
                sqlt += ' order by tissue_type'
		eachRow(sql, sqlt) { row ->
		    String tissueTypeCode = rowGet(row, 'tissue_type_cd', String)
		    if (tissueTypeCode != null) {
			ci.tissues << [tissue     : tissueTypeCode,
				       tissueLabel: rowGet(row, 'tissue_type', String)]
		    }
                }
                break
            case CohortInformation.GPL_TYPE:
		ci.gpls = []
		sqlt = '''
			select distinct rgi.platform, rgi.title
			from DEAPP.de_subject_sample_mapping dssm, de_gpl_info rgi
			where dssm.trial_name in (''' + listToIN(ci.trials) + ''')
			  and dssm.platform in (''' + listToIN(ci.platforms) + ''')
			  and dssm.gpl_id=rgi.platform'''
                sqlt += ' order by rgi.title'
		eachRow(sql, sqlt) { row ->
		    ci.gpls << [gpl: rowGet(row, 'platform', String), gplLabel: rowGet(row, 'title', String)]
		}
                break
            case CohortInformation.RBM_PANEL_TYPE:
		ci.rbmpanels = []
		sqlt = '''
			select distinct dssm.rbm_panel
			from DEAPP.de_subject_sample_mapping dssm
			where dssm.trial_name in (''' + listToIN(ci.trials) + ''')
			  and dssm.platform in (''' + listToIN(ci.platforms) + ')'
		eachRow(sql, sqlt) { row ->
		    String panel = rowGet(row, 'rbm_panel', String)
		    ci.rbmpanels << [rbmpanel: panel, rbmpanelLabel: panel]
		}
                break
            default:
		logger.trace 'No Info Type selected'
        }
    }

    /**
     * First search by trials and platform.
     * If there is only one, that is the default.
     *
     * If there are multiple, search by concept_code. Return none to multiple defaults.
     */
    void fillDefaultGplInHeatMapValidator(HeatmapValidator hv, CohortInformation ci, List<String> concepts) {
	ci.platforms << hv.firstPlatform
	fillCohortInformation null, null, ci, CohortInformation.GPL_TYPE
        if (ci.gpls.size() == 1) {
	    hv.gpls << ((Map) ci.gpls[0]).gpl
	    hv.gplLabels << ((Map) ci.gpls[0]).gplLabel
        }
        else if (ci.gpls.size() > 1) {
	    String sql = '''
		select distinct rgi.platform, rgi.title
		from DEAPP.de_subject_sample_mapping dssm, DEAPP.de_gpl_info rgi
		where dssm.trial_name in (''' + listToIN(ci.trials) + ''')
		  and dssm.platform in (''' + listToIN(ci.platforms) + ''')
		  and dssm.concept_code in (''' + listToIN(concepts) + ''')
		  and dssm.gpl_id=rgi.platform
		order by rgi.title'''
	    eachRow(sql) { row ->
		hv.gpls << rowGet(row, 'platform', String)
		hv.gplLabels << rowGet(row, 'title', String)
	    }
        }
    }

    /**
     * First search by trials and platform.
     * If there is only one, that is the default.
     *
     * If there are multiple, search by concept_code. Return none to multiple defaults.
     */
    void fillDefaultRbmpanelInHeatMapValidator(HeatmapValidator hv, CohortInformation ci, List<String> concepts) {
	ci.platforms << hv.firstPlatform
	fillCohortInformation null, null, ci, CohortInformation.RBM_PANEL_TYPE
        if (ci.rbmpanels.size() == 1) {
	    hv.rbmpanels << ((Map) ci.rbmpanels[0]).rbmpanel
	    hv.rbmpanelsLabels << ((Map) ci.rbmpanels[0]).rbmpanelLabel
        }
        else if (ci.rbmpanels.size() > 1) {
	    String sql = '''
		select distinct dssm.rbm_panel
		from DEAPP.de_subject_sample_mapping dssm
		where dssm.trial_name in (''' + listToIN(ci.trials) + ''')
		and dssm.platform in (''' + listToIN(ci.platforms) + ''')
		and dssm.CONCEPT_CODE IN (''' + listToIN(concepts) + ')'

	    eachRow(sql) { row ->
		String panel = rowGet(row, 'rbm_panel', String)
		hv.rbmpanels << panel
		hv.rbmpanelsLabels << panel
	    }
        }
    }

    List<String> getDistinctTrialsInPatientSets(String rid1, String rid2) {
        checkQueryResultAccess rid1, rid2

	logger.debug 'Checking patient sets'
	List<String> trials = []

	logger.debug '{} {}', rid1, rid2
        if (rid2 == null) {
	    logger.debug 'TESTED AS NULL'
        }

	String sql = '''
		SELECT DISTINCT SECURE_OBJ_TOKEN
		FROM I2B2DEMODATA.PATIENT_TRIAL t
		WHERE t.PATIENT_NUM IN (
			select distinct patient_num
			from I2B2DEMODATA.qt_patient_set_collection
			where result_instance_id'''

	if (rid1 || rid2) {
	    List args
	    if (rid1 && rid2) {
		sql += ' IN (?, ?))'
		args = [rid1, rid2]
            }
            else {
		logger.debug 'one or the other was null'
		sql += ' = ?)'
		args = [rid1 ?: rid2]
            }
	    eachRow(sql, args) { row ->
		String token = rowGet(row, 'SECURE_OBJ_TOKEN', String)
		if (token != null) {
		    trials << token
                }
            }
	}

	trials
    }

    List<String> trialsForResultSet(String resultInstanceId) {
	checkQueryResultAccess resultInstanceId
        Map<String, String> trials = [:]
        List<String> authTrials = []

	String sql = '''
		SELECT distinct trial, SECURE_OBJ_TOKEN
		FROM I2B2DEMODATA.patient_trial pt
		JOIN I2B2DEMODATA.qt_patient_set_collection psc
                    ON pt.patient_num=psc.patient_num
            WHERE psc.result_instance_id = ?
				ORDER BY trial'''
	eachRow(sql, [resultInstanceId]) { row ->
	    trials << [rowGet(row, 'trial', String), rowGet(row, 'secure_obj_token', String)]
	}

        def trialsAccess = getAccess(trials, user)
        trialsAccess.each { trial, access ->
            if (access != 'Locked')
                authTrials << trial
        }
	authTrials
    }

    private XPath newXPath() {
	XPathFactory.newInstance().newXPath()
    }

    private Document parseXml(String xml) {
	DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance()
	domFactory.setNamespaceAware(true) // never forget this!
	domFactory.newDocumentBuilder().parse new InputSource(new StringReader(xml))
    }

    private String evaluateContent(XPath xPath, String expression, item) {
	evaluate(xPath, expression, item).textContent
    }

    private Node evaluate(XPath xPath, String expression, item) {
	(Node) xPath.evaluate(expression, item, XPathConstants.NODE)
    }

    private void eachRow(String sql, List params = Collections.emptyList(), Closure closure) {
	eachRow new Sql(dataSource), sql, params, closure
    }

    private void eachRow(Sql sql, String sqlString, List params = Collections.emptyList(), Closure closure) {
	sql.eachRow sqlString, params, closure
    }

    @CompileDynamic
    private <T> T rowGet(row, String name, Class<T> clazz) {
	row[name].asType clazz
    }

    @CompileDynamic
    private <T> T rowGet(row, int index, Class<T> clazz) {
	row[index].asType clazz
    }

    @CompileDynamic
    private String encodeAsSHA1(String s) {
	s.encodeAsSHA1()
    }

    @CompileDynamic
    void afterPropertiesSet() {
	censorFlagList = grailsApplication.config.com.recomdata.analysis.survival.censorFlagList as List<String>
	    survivalDataList = grailsApplication.config.com.recomdata.analysis.survival.survivalDataList as List<String>
	}
}

@CompileStatic
class SurvivalConcepts {
    Concept conceptSurvivalTime
    Concept conceptCensoring
    Concept conceptEvent
}

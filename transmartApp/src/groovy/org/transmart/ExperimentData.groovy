package org.transmart

import com.recomdata.export.GenePatternFiles
import groovy.sql.Sql
import i2b2.SampleInfo
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction

import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class ExperimentData {

    private static final String QUOTE = "'"

    private DataSource dataSource
    private SessionFactory sessionFactory
    private sampleInfoService

    private String concepts1 = ''
    private String concepts2 = ''
    private String distinctSubjectIds1 = ''
    private String distinctSubjectIds2 = ''
    private String experimentDataQuery = ''
    private String rbmPanels1 = ''
    private String rbmPanels2 = ''
    private String timepoints1 = ''
    private String timepoints2 = ''
    private String trialNames = ''

    private int startingOutputColumn = 0

    private Map<Long, SampleInfo> sampleInfoMap = [:]

    // passed to the create header method of the GCT files
    private String[] subjectNameArray

    ExperimentData(DataSource dataSource, SessionFactory sessionFactory, sampleInfoService) {
	this.dataSource = dataSource
	this.sessionFactory = sessionFactory
	this.sampleInfoService = sampleInfoService
    }

    String analysisType = ''
    String dataType = ''
    String pathwayName = ''
    String whiteString = ''

    // a JSON Object representing the selected subsets: {"SampleIdList":{"1":["Sample1"],"2":[],"3":[]}}
    def sampleIdList

    GenePatternFiles gpf

    boolean fixlast
    boolean rawdata

    void getHeatMapDataSample() {

        String intensityType = 'LOG2'

        if (rawdata) {
            intensityType = 'RAW'
        }

	// fill in some properties
        gatherSampleData()
        dataType = 'MRNA_AFFYMETRIX'

        //Build the query to retrieve the sample data.
        if (dataType.toUpperCase() == 'MRNA_AFFYMETRIX') {
            experimentDataQuery = buildQuerySampleMRNA(intensityType)
        }
        else if (dataType.toUpperCase() == 'RBM') {
	    experimentDataQuery = createRBMHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2,
							concepts1, concepts2, timepoints1, timepoints2, rbmPanels1, rbmPanels2)
        }
        else if (dataType.toUpperCase() == 'PROTEIN') {
	    experimentDataQuery = createProteinHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2,
							    concepts1, concepts2, timepoints1, timepoints2)
        }
    }

    /**
     * build the query as a string to retrieve sample data.
     */
    private void gatherSampleData() {
        //First, create a string that has all the sample ID's. We will use this to get the distinct Trials.
        //While doing this we will also construct a map that has Subset:[List of SampleInfo objects].
        //This is the list of all Sample IDs.
	StringBuilder sb = new StringBuilder()

        //Create a list that has S#_SAMPLE_CD (Sample_CD is from de_subject_sample_mapping).
	List<String> sampleNameListWithPrefix = []

        //Loop over the JSON object and grab the ID's.
	for (subsetItem in sampleIdList) {

            def subsetSampleList = subsetItem.value

            //Each subset has a list of IDs.
	    for (it in subsetSampleList) {
		if (sb) {
		    sb << ','
		}

                //Add the sample ID to the complete list.
		sb << it            }

        }
	String sampleIdAllListStr = sb.toString()

        //Get the trial names based on sample_id.
        trialNames = getTrialNameBySampleID(sampleIdAllListStr)
	if (!trialNames) {
	    throw new Exception('Could not find trial names for the given subjects!')
	}

        //We need to build a map of the sample info objects so that we can easily access the sample info objects by key without going to the DB.
        List<SampleInfo> sampleInfoList = sampleInfoService.getSampleInfoListInOrder(sampleIdAllListStr)
	for (SampleInfo sampleInfo in sampleInfoList) {
	    sampleInfoMap[sampleInfo.id] = sampleInfo
        }

        //After we build the map we can build the sample name list.

	for (subsetItem in sampleIdList) {
            def subsetSampleList = subsetItem.value

            //We pass in the the list of sample ids for each subset.
	    getSampleNameListFromIds subsetSampleList, 'S' + subsetItem.key + '_', sampleNameListWithPrefix
        }

        //Build the array for the header for our gct file.
	subjectNameArray = sampleNameListWithPrefix as String[]
    }

    private String buildQuerySampleMRNA(String intensityType) {
        //This is the list of columns in our select statement.
	//We need to get the list of columns. The list of columns is based on what
	// we name them in the subquery creation methods below. Which is subject ID.
	String columns = listHeatmapColumnsFromMap('probeset', 'S', true) + ', star'

        //For each of the susbsets we need to build a query which we union together with the other subsets.
	List<String> subsetQueries = []

	// We need to pass the ordered list of AssayIds to this function.
	// The Assay ID is stored in the map of sampleInfo objects.

	for (subsetItem in sampleIdList) {
            def subsetSampleList = subsetItem.value

            //Don't add a subset if there are no items in the subset.
	    if (subsetSampleList) {
                String currentAssayIds = buildAssayIdsFromSampleIds(subsetSampleList)
		String currentSubsetQuery = createMRNAHeatmapPathwayQuery(
		    'S' + subsetItem.key + '_', currentAssayIds, intensityType)

		subsetQueries << currentSubsetQuery
            }
        }

        //We have to use the log2_intensity to make the analysiscontroller happy..
        String intensityColumn = 'LOG2_INTENSITY'

        //I think this needs to be 'SAMPLE_ID' IN.
	String subjects = listHeatmapColumnsFromMap('', 'S', false) + ", '*' as star"

        //For the final query we need to UNION together all the subsets.
	StringBuilder r = new StringBuilder()
	r << 'SELECT ' << columns << ' FROM ('

	int subsetCounter = 1

        //TODO:CHECK FOR EMPTY
	for (subsetQuery in subsetQueries) {

	    if (subsetCounter > 1) {
		r << ' UNION '
	    }

	    r << subsetQuery

	    subsetCounter++
        }

	r << ') PIVOT (avg(' << intensityColumn << ') for subject_id IN (' << subjects << ')) '

	r << ' ORDER BY PROBESET, GENE_SYMBOL'

	r
    }

    private String buildAssayIdsFromSampleIds(sampleIds) {
	StringBuilder currentAssayIds = new StringBuilder()

	for (id in sampleIds) {
	    SampleInfo sampleInfo = sampleInfoMap[id]
            String currentAssayId = sampleInfo.assayId
	    if (currentAssayIds) {
		currentAssayIds << ','
            }
	    currentAssayIds << currentAssayId
	}

	currentAssayIds
    }

    void writeGpFiles() {
	switch (dataType.toUpperCase()) {
	    case 'MRNA_AFFYMETRIX': writeMrnaDataToFiles(); break
	    case 'RBM': writeDataToFile(); break
	    case 'PROTEIN': writeDataToFile()
        }
    }

    // File Creation Methods

    private void writeMrnaDataToFiles() {
        //Write the file that has our group information.
	gpf.writeClsFileManySubsets sampleIdList

        gpf.openGctFile()
        gpf.openCSVFile()

        //Get pretty names for the subjects.

        // handle *
        if (fixlast) {
            String[] newNameArray = new String[subjectNameArray.length + 1]
            newNameArray[subjectNameArray.length] = '*'
            System.arraycopy(subjectNameArray, 0, newNameArray, 0, subjectNameArray.length)
            subjectNameArray = newNameArray
        }

	int rows = 0

	StringBuilder s = new StringBuilder() //GCT File.
	StringBuilder cs = new StringBuilder() //CSV File.

	Session session = sessionFactory.currentSession

        Statement st = null
        ResultSet rs = null
	Transaction trans = null

        try {
            trans = session.beginTransaction()
	    Connection conn = session.connection()
            st = conn.createStatement()
	    st.execute 'alter session enable parallel dml'
	    st.fetchSize = 5000
            rs = st.executeQuery(experimentDataQuery)
	    int totalCol = rs.metaData.columnCount

            while (rs.next()) {
		cs.length = 0

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

                if (fixlast) {
		    s << '\t' << '0'
		    cs << ',' << '0'
                }

                rows++

		s << '\n'

		gpf.writeToCSVFile cs.toString()
            }
        }
        finally {
	    rs?.close()
	    st?.close()
            trans.commit()
        }

        //Write the gct header and gct file contents.
	gpf.createGctHeader rows, subjectNameArray, '\t'
	gpf.writeToGctFile s.toString()

	gpf.closeGctFile()
	gpf.closeCSVFile()
    }

    private void writeDataToFile() {
        gpf.openGctFile()
        gpf.openCSVFile()

	gpf.writeClsFile distinctSubjectIds1, distinctSubjectIds2

	StringBuilder s = new StringBuilder()

	Sql sql = new Sql(dataSource)
	int numCols
	def rows = sql.rows(experimentDataQuery, { meta -> numCols = meta.columnCount })

        // create header
	gpf.createGctHeader rows.size(), subjectNameArray, '\t'

	for (row in rows) {

	    s.length = 0

            if (dataType.toUpperCase() == 'PROTEIN') {
		if (row.component == null) {
		    s << row.GENE_SYMBOL
                }
                else {
		    s << row.component
                }

		s << '\t' << row.GENE_SYMBOL
            }

	    for (int count in startingOutputColumn..<numCols - 1) {
		String val = row[count]
		if (val == 'null' || val == null) {
		    val = whiteString
                }
		if (count) {
		    s << '\t'
		}
		s << val
            }

	    gpf.writeToGctFile s.toString()
	    gpf.writeToCSVFile s.toString().replaceAll('\t', ',')
        }

	gpf.closeGctFile()
	gpf.closeCSVFile()
    }

    // Query Creation Methods

    private String createMRNAHeatmapPathwayQuery(String prefix, String assayIds, String intensityType) {

	Sql sql = new Sql(dataSource)

        String genes
	if (pathwayName) {
            genes = getGenes(pathwayName)
        }

        String intensityCol = 'zscore'

        if ('RAW' == intensityType) {
            intensityCol = 'RAW_INTENSITY'

            //check if we have sufficient raw data to run gp query
	    int goodPct
	    String rawCountQuery = '''
				select DISTINCT
				/*+ parallel(DEAPP.de_subject_microarray_data,4) */ /*+ parallel(DEAPP.de_mrna_annotation,4) */
				count(distinct a.raw_intensity)/count(*) as pct_good
				FROM DEAPP.de_subject_microarray_data a, DEAPP.de_mrna_annotation b
				WHERE a.probeset_id = b.probeset_id AND a.trial_name IN (''' + trialNames + ')' + '''
				AND a.assay_id IN (''' + assayIds + ')'

            sql.eachRow(rawCountQuery, { row -> goodPct = row[0] })

	    if (goodPct == 0) {
		throw new Exception('No raw data for Comparative Marker Selection.')
	    }
        }

        // added hint here...
        StringBuilder s = new StringBuilder()
	s << "select DISTINCT /*+ parallel(DEAPP.de_subject_microarray_data,4) */ /*+ parallel(DEAPP.de_mrna_annotation,4) */  b.PROBE_ID || ':' || b.GENE_SYMBOL as PROBESET, b.GENE_SYMBOL, a." << intensityCol << " as LOG2_INTENSITY "
	s << " , '" << prefix << "' || a.patient_ID as subject_id "
	s << " FROM DEAPP.de_subject_microarray_data a, DEAPP.de_mrna_annotation b "
	s << " WHERE a.probeset_id = b.probeset_id AND a.trial_name IN (" << trialNames << ") "
	s << " AND a.assay_id IN (" << assayIds << ")"

	if (pathwayName) {
	    s << ' AND b.gene_id IN (' << genes << ')'
	}

	s
    }

    private String createRBMHeatmapQuery(String prefix, String ids, String concepts,
	                                 String pathwayName, String timepoint, String rbmPanels) {
        StringBuilder s = new StringBuilder()

	String genes
	if (pathwayName && 'SHOWALLANALYTES'.compareToIgnoreCase(pathwayName) != 0) {
            genes = getGenes(pathwayName)
        }

	if (!timepoint) {
	    s << "SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '"
	    s << prefix << "'|| t1.patient_id as subject_id "
	    s << "FROM DEAPP.DE_SUBJECT_RBM_DATA t1, DEAPP.de_subject_sample_mapping t2 "
	    s << "WHERE t1.patient_id = t2.patient_id and t1.patient_id IN (" << ids << ")"
        }
        else {
	    s << "SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '"
	    s << prefix << "'|| t1.patient_id as subject_id "
	    s << "FROM DEAPP.DE_SUBJECT_RBM_DATA t1, DEAPP.de_subject_sample_mapping t2 "
	    s << "WHERE "
	    s << "t2.patient_id IN (" << ids << ") and "
	    s << "t2.timepoint_cd IN (" << quoteCSV(timepoint) << ") and "
	    s << "t1.data_uid = t2.data_uid and t1.assay_id=t2.assay_id"
        }

	if (rbmPanels) {
	    s << ' and t2.rbm_panel IN (' << quoteCSV(rbmPanels) << ')'
        }

	if (pathwayName && 'SHOWALLANALYTES'.compareToIgnoreCase(pathwayName) != 0) {
	    s << ' AND t1.gene_id IN (' << genes << ')'
        }

	s
    }

    private String createProteinHeatmapQuery(String prefix, String pathwayName,
	                                     String ids, String concepts, String timepoint) {

	Sql sql = new Sql(dataSource)

        String cntQuery = 'SELECT COUNT(*) as N FROM DE_SUBJECT_SAMPLE_MAPPING WHERE concept_code IN (' + quoteCSV(concepts) + ')'

	Integer count

	sql.query(cntQuery) { ResultSet rs ->
	    while (rs.next()) {
		count = rs.toRowResult().N
	    }
        }

        StringBuilder s = new StringBuilder()

	if (count == 0) {
	    if (timepoint) {
		s << "SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << "FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, "
		s << "DE_subject_sample_mapping b "
		s << "WHERE c.pathway_id= p.id and "
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << "a.gene_symbol = c.gene_symbol and "
		s << "a.patient_id IN (" + ids + ") and "
		s << "b.TIMEPOINT_CD IN (" + quoteCSV(timepoint) + ") and "
		s << "a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint and "
		s << "a.assay_id=b.assay_id  "
	    }
	    else {
		s << "SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << "FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p "
		s << "WHERE c.pathway_id= p.id and "
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << "a.gene_symbol = c.gene_symbol and "
		s << "a.patient_id IN (" << ids << ")"
            }
        }
        else {
	    if (timepoint) {
		s << "select distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << "FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, "
		s << "DE_subject_sample_mapping b "
		s << "WHERE c.pathway_id= p.id and "
		if (pathwayName) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
		}
		s << "a.gene_symbol = c.gene_symbol and "
		s << "a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and "
		s << "b.concept_code IN (" << quoteCSV(concepts) << ") and "
		s << "a.patient_id IN (" << ids << ") and "
		s << "b.TIMEPOINT_CD IN (" << quoteCSV(timepoint) << ") and "
		s << "a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint "
	    }
	    else {
		s << "select distinct a.component, a.GENE_SYMBOL, a.zscore, '"
		s << prefix << "' || a.patient_ID as subject_id "
		s << "FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, "
		s << "DE_subject_sample_mapping b "
		s << "WHERE c.pathway_id= p.id and "
                if (pathwayName != null) {
		    s << " p.pathway_uid='" << pathwayName << "' and "
                }
		s << "a.gene_symbol = c.gene_symbol and "
		s << "a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and "
		s << "b.concept_code IN (" << quoteCSV(concepts) << ") and "
		s << "a.patient_id IN (" << ids << ")"
            }
        }

	s
    }

    private String createProteinHeatmapQuery(String pathwayName, String ids1, String ids2,
	                                     String concepts1, String concepts2,
	                                     String timepoint1, String timepoint2) {

        String columns = listHeatmapColumns('component', ids1, ids2, 'S1_', 'S2_') + ', star'

        String s1
	if (ids1) {
            s1 = createProteinHeatmapQuery('S1_', pathwayName, ids1, concepts1, timepoint1)
        }

	String s2
	if (ids2) {
            s2 = createProteinHeatmapQuery('S2_', pathwayName, ids2, concepts2, timepoint2)
        }

	String subjects = getSubjectSelectColumns(ids1, ids2, 'S1_', 'S2_') + ", '*' as star"

	if (s1) {
	    if (s2) {
		"SELECT " + columns +
		    " FROM (" + s1.replace("distinct ", " ") +
		    " UNION " + s2.replace("distinct ", " ") + ")" +
		    " PIVOT (avg(zscore) for subject_id IN (" + subjects + "))" +
		    " ORDER BY component, GENE_SYMBOL"
	    }
	    else {
		"SELECT " + columns +
		    " FROM (" + s1 + ")" +
		    " PIVOT (avg(zscore) for subject_id IN (" + subjects + "))" +
		    " ORDER BY component, GENE_SYMBOL"
	    }
	}
	else {
	    "SELECT " + columns +
		" FROM (" + s2 + ")" +
		" PIVOT (avg(zscore) for subject_id IN (" + subjects + "))" +
		" ORDER BY component, GENE_SYMBOL"
	}
    }

    private String createRBMHeatmapQuery(String pathwayName, String ids1, String ids2,
	                                 String concepts1, String concepts2,
	                                 String timepoint1, String timepoint2,
	                                 String rbmPanels1, String rbmPanels2) {

        String columns = listHeatmapColumns('antigen_name', ids1, ids2, 'S1_', 'S2_') + ', star'

        String s1
	if (ids1) {
            s1 = createRBMHeatmapQuery('S1_', ids1, concepts1, pathwayName, timepoint1, rbmPanels1)
	}

	String s2
	if (ids2) {
            s2 = createRBMHeatmapQuery('S2_', ids2, concepts2, pathwayName, timepoint2, rbmPanels2)
	}

	String subjects = getSubjectSelectColumns(ids1, ids2, 'S1_', 'S2_') + ", '*' as star"

	if (s1) {
	    if (s2) {
		"SELECT " + columns + " FROM (" + s1.replace("distinct ", " ") +
		    " UNION " + s2.replace("distinct ", " ") +
		    ") PIVOT (avg(value) for subject_id IN (" + subjects + ")) " +
		    "ORDER BY ANTIGEN_NAME, GENE_SYMBOL"
	    }
	    else {
		"SELECT " + columns +
		    " FROM (" + s1 + ")" +
		    " PIVOT (avg(value) for subject_id IN (" + subjects + "))" +
		    " ORDER BY ANTIGEN_NAME, GENE_SYMBOL"
	    }
	}
	else {
	    "SELECT " + columns +
		" FROM (" + s2 + ")" +
		" PIVOT (avg(value) for subject_id IN (" + subjects + "))" +
		" ORDER BY ANTIGEN_NAME, GENE_SYMBOL"
	}
    }

    // Helper methods

    /**
     *  Compose a list of columns used by Heatmap and then trim average value
     *
     * @param biomarker probeset (mRNA), component (Protein) and antigen_name (RBM)
     * @param prefix1 usually use 'S1_'
     * @param prefix2 usually use 'S2_'
     */
    private String listHeatmapColumns(String biomarker, String ids1, String ids2,
	                              String prefix1, String prefix2) {

        StringBuilder s = new StringBuilder()
	s << ' ' << biomarker << ', gene_symbol '

	if (ids1) {
	    for (id in ids1.split(',')) {
		s << ', round(' << prefix1 << id << ', 4) as ' << prefix1 << id
	    }
        }

	if (ids2) {
	    for (id in ids2.split(',')) {
		s << ', round(' << prefix2 << id << ', 4) as ' << prefix2 << id
	    }
        }

	s
    }

    /**
     *  Compose a list of columns based on an id and a prefix.
     *
     * @param biomarker probeset (mRNA), component (Protein) and antigen_name (RBM)
     * @param prefix usually use 'S'
     */
    private String listHeatmapColumnsFromMap(String biomarker, String prefix, boolean roundColumn) {
        StringBuilder s = new StringBuilder()
	if (biomarker) {
	    s << ' ' << biomarker << ', gene_symbol, '
	}

	for (subsetItem in sampleIdList) {
            def subsetItems = subsetItem.value

            //Each subset has a list of IDs.
	    for (item in subsetItems) {
		SampleInfo sampleInfo = sampleInfoMap[item]

                if (roundColumn) {
		    s << 'round(' << prefix << subsetItem.key << '_' << sampleInfo.patientId << ', 4) as '
		    s << prefix << subsetItem.key << '_' << sampleInfo.patientId << ','
                }
                else {
		    s << QUOTE << prefix << subsetItem.key << '_' << sampleInfo.patientId << "' as "
		    s << prefix << subsetItem.key << '_' << sampleInfo.patientId << ','
                }            }

        }

	s.substring 0, s.length() - 1
    }

    private String getSubjectSelectColumns(String ids1, String ids2, String prefix1, String prefix2) {
        StringBuilder s = new StringBuilder()

	if (ids1) {
	    for (id in ids1.split(',')) {
		s << QUOTE << prefix1 << id << "' as " << prefix1 << id << ','
            }
        }

	if (ids2) {
	    for (id in ids2.split(',')) {
		s << QUOTE << prefix2 << id << "' as " << prefix2 << id << ','
	    }
        }

	s.substring 0, s.length() - 1
    }

    private String quoteCSV(String val) {
	if (!val) {
	    return ''
	}

        StringBuilder s = new StringBuilder()

	String[] inArray = val.split(',')
	s << QUOTE << inArray[0] << QUOTE

        for (int i = 1; i < inArray.length; i++) {
	    s << ",'" << inArray[i] << QUOTE
	}

	s
    }

    private String convertList(idList, boolean isString, int max) {
        StringBuilder s = new StringBuilder()

        int i = 0

        for (id in idList) {
            if (i < max) {
		if (s) {
		    s << ','
                }

                if (isString) {
		    s << QUOTE
                }

		s << id

                if (isString) {
		    s << QUOTE
                }
            }
            else {
                break
            }

            i++
        }

	s
    }

    /**
     * Go to the i2b2DemoData.sample_categories table and gather the trial names for the list of sample IDs.
     */
    private String getTrialNameBySampleID(String ids) {

	String sql = '''
			select distinct s.trial_name
			from i2b2DemoData.sample_categories s
			where s.SAMPLE_ID in (''' + quoteCSV(ids) + ')'

	StringBuilder trialNames = new StringBuilder()

	new Sql(dataSource).eachRow sql.toString(), { row ->

            String tName = row.trial_name

            //These are some hardcoded study names.
            if (tName.equalsIgnoreCase('BRC Antidepressant Study')) {
                tName = 'BRC:mRNA:ADS'
            }

            if (tName.equalsIgnoreCase('BRC Depression Study')) {
                tName = 'BRC:mRNA:DS'
            }

	    if (trialNames) {
		trialNames << ','
            }
	    trialNames << QUOTE << tName << QUOTE
        }

	trialNames
    }

    private String getGenes(String pathwayName) {

        // build pathway sub query and get gene symbol list
        // gene sig or gene list
	String sql
        if (pathwayName.startsWith('GENESIG') || pathwayName.startsWith('GENELIST')) {
	    sql = '''
			select distinct bm.primary_external_id as gene_id
			from SEARCHAPP.search_keyword sk, SEARCHAPP.search_bio_mkr_correl_fast_mv sbm, BIOMART.bio_marker bm
			where sk.bio_data_id = sbm.domain_object_id
			  and sbm.asso_bio_marker_id = bm.bio_marker_id
			  and sk.unique_id ='''
        }
        else {
	    sql = '''
			select distinct bm.primary_external_id as gene_id
			from SEARCHAPP.search_keyword sk, BIOMART.bio_marker_correl_mv sbm, BIOMART.bio_marker bm
			where sk.bio_data_id = sbm.bio_marker_id
			  and sbm.asso_bio_marker_id = bm.bio_marker_id
			  and sk.unique_id ='''
        }
	sql += QUOTE + pathwayName.replaceAll(QUOTE, "''") + QUOTE

	List geneIds = []
	new Sql(dataSource).eachRow sql, { row ->
	    if (row.gene_id) {
		geneIds << row.gene_id
            }
        }

	convertList(geneIds, false, 1000)
    }

    private void getSampleNameListFromIds(sampleIds, String prefix, List<String> sampleNameList) {
	if (!sampleIds || !sampleInfoMap || sampleNameList == null) {
            return
	}

	for (sampleIdStr in sampleIds) {
	    SampleInfo sampleInfo = sampleInfoMap[sampleIdStr]
            String sampleName = sampleInfo.sampleName
	    if (prefix) {
                sampleName = prefix + sampleName
	    }
	    sampleNameList << sampleName
        }
    }
    //****************************************************************
}

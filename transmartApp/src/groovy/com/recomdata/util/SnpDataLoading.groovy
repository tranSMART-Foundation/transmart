package com.recomdata.util

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.util.Assert

import java.sql.Clob
import java.sql.DriverManager

@Slf4j('logger')
class SnpDataLoading {

    private static final String SAMPLE_TYPE_NORMAL = 'NORMAL'
    private static final String SAMPLE_TYPE_DISEASE = 'DISEASE'
    private static final List<String> chroms = ['1', '2', '3', '4', '5', '6', '7', '8', '9',
	                                        '10', '11', '12', '13', '14', '15', '16', '17',
	                                        '18', '19', '20', '21', '22', 'X', 'Y'].asImmutable()

    private Sql sql

    /**
     * Splits the sorted batch xcn file into multiple data file, one for each dataset.
     * File i/o in Groovy is extremely slow, perhaps because the file is close
     * after every write, and reopened again for writing the next line. Use BufferedWriter
     */
    void splitBatchXCN(String xcnFileName, String resultFolderName) {
        File xcnFile = new File(xcnFileName)

	String xcnFileNameSimple = xcnFile.name
        String fileRoot = xcnFileNameSimple.substring(0, xcnFileNameSimple.lastIndexOf('.'))

	List<String> patientList = []
	List<String> datasetList = []

	List<BufferedWriter> outAllWriterList = []
	List<BufferedWriter[]> outChromWriters = []

        BufferedWriter probeDefWriter = new BufferedWriter(new FileWriter(resultFolderName + File.separator + fileRoot + '_probe_def.txt'))

	File patientListFile = new File(resultFolderName, fileRoot + '_patient_list.txt')
	File datasetListFile = new File(resultFolderName, fileRoot + '_dataset_list.txt')

	for (String line in xcnFile.readLines()) {
	    if (line.contains('SNP\tChromosome\tPhysicalPosition')) {
                String[] headers = line.split('\t')
                for (int i = 3; i < headers.length; i = i + 2) {
                    String datasetName = headers[i]
                    String patientName = datasetName.substring(0, datasetName.length() - 1)
		    datasetList << datasetName
		    if (!patientList.contains(patientName)) {
			patientList << patientName
		    }

		    outAllWriterList << new BufferedWriter(new FileWriter(
			new File(resultFolderName, datasetName + '_all.xcn')))

		    BufferedWriter[] chromWriterArray = new BufferedWriter[chroms.size()]
		    outChromWriters << chromWriterArray
		    for (int index = 0; index < chroms.size(); index++) {
			chromWriterArray[index] = new BufferedWriter(new FileWriter(
			    new File(resultFolderName, datasetName + '_' + chroms[index] + '.xcn')))
                    }
                }

		for (String patientName in patientList) {
		    patientListFile << patientName + '\n'
                }
		for (String datasetName in datasetList) {
		    datasetListFile << datasetName + '\n'
                }
            }
            else {
                String[] values = line.split('\t')
                String probeName = values[0]
                String chrom = values[1]
                String chromPos = values[2]

		probeDefWriter.writeLine probeName + '\t' + chrom + '\t' + chromPos

                for (int i = 3; i < values.length; i = i + 2) {
                    // The fix width for each value is 7 like 'AB 1.34' or 'NC 0.24'. NC for NoCall
                    String cn = values[i]
                    String gt = values[i + 1]

                    try {
			Float cnd = Float.valueOf(cn)
			if (cnd != null && !cnd.isNaN()) {
			    if (cnd.doubleValue() > 99.99) {
                                cn = '99.99'
			    }
			    else {
                                cn = String.format('%5.2f', cnd.doubleValue())
                            }
			}
                    }
		    catch (ignored) {}

		    if (gt.equalsIgnoreCase('NoCall')) {
                        gt = 'NC'
		    }
		    int index = (i - 3) / 2
		    BufferedWriter outAllWriter = outAllWriterList[index]
		    outAllWriter.writeLine cn + '\t' + gt
		    BufferedWriter[] outChromWriterArray = outChromWriters[index]
		    BufferedWriter outChromWriter = outChromWriterArray[getChromNumberFromString(chrom) - 1]
		    outChromWriter.writeLine cn + '\t' + gt
                }
            }
        }

        probeDefWriter.close()

        for (int i = 0; i < datasetList.size(); i++) {
	    outAllWriterList[i].close()
	    BufferedWriter[] chromWriterArray = outChromWriters[i]
	    for (int index = 0; index < chroms.size(); index++) {
		chromWriterArray[index].close()
            }
        }
    }

    private int getChromNumberFromString(String chromStr) {
	chroms.indexOf(chromStr) + 1
    }

    /**
     * @param server Oracle server name
     * @param port Oracle listener poer
     * @param sid Oracle SID
     * @param userName Oracle user name
     * @param password Oracle user password
     */
    private Sql getSql(String server, String port, String sid, String userName, String password) {
	if (!sql) {
	    sql = new Sql(DriverManager.getConnection(
		'jdbc:oracle:thin:@' + server + ':' + port + ':' + sid, userName, password))
	}

	sql
    }

    /**
     * Populates the de_subject_snp_dataset table.
     * Due to workflow consideration of i2b2 data loading, the concept_cd column is left empty, and will be filled later
     * The dataset name in the dataset list file needs to be consistent with sourcesystem_cd of patient_dimension.
     * The platform needs to be existing in de_gpl_info
     * The SnpDataset.sampleType can only be 'NORMAL' or 'DISEASE',. It is used to generate GenePattern sample info text file.
     */
    void loadDataSet(String datasetListFileName, String trialName, String platformName,
	             String normalSuffix, String diseaseSuffix, String genderForAll) {

	Map<Long, Long[]> datasetPatientMap = [:]

	for (String line in new File(datasetListFileName).readLines()) {
            String datasetName = line
            String patientStr = datasetName.substring(0, datasetName.length() - 2)
	    String subjectIdStr = patientStr.substring(patientStr.indexOf('_') + 1)

	    String sampleType
            String suffix = datasetName.substring(datasetName.length() - 1)
            if (suffix.equalsIgnoreCase(normalSuffix)) {
                sampleType = SAMPLE_TYPE_NORMAL
            }
            else if (suffix.equalsIgnoreCase(diseaseSuffix)) {
                sampleType = SAMPLE_TYPE_DISEASE
	    }
	    else {
                throw new Exception('The datasetName suffix ' + suffix + ' does not match ' + normalSuffix + ' or ' + diseaseSuffix)
	    }

            Long patientNum = null
            String gender = null
	    sql.eachRow('select * from I2B2DEMODATA.patient_dimension where sourcesystem_cd = ?', [patientStr]) { row ->
                patientNum = row.patient_num
                gender = row.sex_cd
		if (gender == null && genderForAll) {
                    gender = genderForAll;    // Sometimes the gender information is missing in patient_dimension
                }
            }
	    if (patientNum == null) {
                throw new Exception('The patient_num for source id ' + patientStr + ' does not exist in patient_dimension table')
	    }

	    String stmt = '''
				insert into DEAPP.de_subject_snp_dataset
				values(seq_snp_data_id.nextval, ?, null, ?, ?, ?, null, ?, ?, null, ?)''' // For GSE19539 on ovarian cancer, all patients are female
	    sql.execute stmt, [datasetName, platformName, trialName, patientNum, subjectIdStr, sampleType, gender]

            Long datasetId = null
            sql.eachRow('select seq_snp_data_id.currval as datasetId from dual') { row ->
                datasetId = row.datasetId
            }
	    Assert.notNull datasetId, 'failed to get newly created dataset ID for ' + datasetName

	    Long[] datasetPair = datasetPatientMap[patientNum]
            if (datasetPair == null) {
                datasetPair = new Long[2]
		datasetPatientMap[patientNum] = datasetPair
            }
	    if (sampleType == SAMPLE_TYPE_NORMAL) {
                datasetPair[0] = datasetId
            }
            else {
                datasetPair[1] = datasetId
            }
        }

	for (Map.Entry<Long, Long[]> pairEntry in datasetPatientMap) {
	    Long[] pair = pairEntry.value
            if (pair[0] != null && pair[1] != null) {    // The data is paired
		sql.execute '''
				update DEAPP.de_subject_snp_dataset
				set paired_dataset_id=?
				where subject_snp_dataset_id=?''',
		pair[1], pair[0]
		sql.execute '''
				update DEAPP.de_subject_snp_dataset
				set paired_dataset_id=?
				where subject_snp_dataset_id=?''',
		pair[0], pair[1]
            }
        }
    }

    void loadDataByPatient(String datasetListFileName, String trialName, String outPathName, String normalSuffix,
	                   String diseaseSuffix, String normalSampleType, String diseaseSampleType) {

	for (String line in new File(datasetListFileName).readLines()) {
            String datasetName = line
            String patientStr = datasetName.substring(0, datasetName.length() - 1)
	    String patientSourceStr = trialName + patientStr.substring(patientStr.indexOf('_') + 1)

	    String sampleType
            String suffix = datasetName.substring(datasetName.length() - 1)
            if (suffix.equalsIgnoreCase(normalSuffix)) {
                sampleType = normalSampleType // SnpDataset.SAMPLE_TYPE_NORMAL
            }
            else if (suffix.equalsIgnoreCase(diseaseSuffix)) {
                sampleType = diseaseSampleType //SnpDataset.SAMPLE_TYPE_DISEASE
	    }
	    else {
		throw new Exception('The datasetName suffix ' + suffix +
				    ' does not match ' + normalSuffix + ' or ' + diseaseSuffix)
	    }

            Long datasetId = null
            Long patientNum = null
	    String stmt = '''
			select a.patient_num as patient_num, b.subject_snp_dataset_id as dataset_id
			from I2B2DEMODATA.PATIENT_DIMENSION a, DEAPP.de_subject_snp_dataset b
			where a.patient_num = b.patient_num
			and a.sourcesystem_cd = ?
			and b.trial_name = ?
			and b.sample_type = ?'''
            sql.eachRow(stmt, [patientSourceStr, trialName, sampleType]) { row ->
                datasetId = row.dataset_id
                patientNum = row.patient_num
            }
	    if (datasetId == null) {
		throw new Exception('The dataset for ' + datasetName + ' does not exist in database')
	    }

	    File chromAllFile = new File(outPathName, datasetName + '_all.xcn')
	    if (chromAllFile.exists()) {
		sql.execute("insert into DEAPP.de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, 'ALL', ?)",
			    [datasetId, trialName, patientNum, chromAllFile.text])
	    }

	    for (String chrom in chroms) {
		File chromFile = new File(outPathName, datasetName + '_' + chrom + '.xcn')
		if (chromFile.exists()) {
		    sql.execute('insert into DEAPP.de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?)',
				[datasetId, trialName, patientNum, chrom, chromFile.text])
                }
            }
        }
    }

    void loadSNPProbeSortedDef(String probeDefFileName, String platformName) {

	String probeDefFileContents = new File(probeDefFileName).text

	StringBuilder[] chromBufList = new StringBuilder[24]
        int[] chromCountList = new int[24]
        int chromCountTotal = 0
	for (String line in probeDefFileContents.readLines()) {
	    if (line?.trim()) {
                String[] values = line.split('\t')
                String chrom = values[1]
		int index = getChromNumberFromString(chrom)
		StringBuilder chromBuf = chromBufList[index - 1]
                if (chromBuf == null) {
		    chromBuf = new StringBuilder()
		    chromBufList[index - 1] = chromBuf
		    chromCountList[index - 1] = 0 // set the initial count to 0
                }
		chromBuf << line << '\n'
		chromCountList[index - 1]++
                chromCountTotal++
            }
        }

	sql.execute('insert into DEAPP.de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?)',
		    [platformName, chromCountTotal, 'ALL', probeDefFileContents])

	for (int i = 0; i < chroms.size(); i++) {
	    StringBuilder chromBuf = chromBufList[i]
	    if (chromBuf) {
		sql.execute('insert into DEAPP.de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?)',
			    [platformName, chromCountList[i], chroms[i], chromBuf.toString()])
            }
        }
    }

    void connectConceptWithPatient(String datasetFileName, String trialName) {
        Sql sql = getSql()

	List<String> conceptList = []
        String stmt = "select c_basecode from i2b2 where rownum < 10 and c_fullname like '%GSE19539%Biomarker%Affymetrix%'"
        sql.eachRow(stmt) { row ->
	    conceptList << row.c_basecode
        }

	for (String line in new File(datasetFileName).readLines()) {
            // Only use N (Normal data)
            if (line.endsWith('N')) {
		String patientEnd = line.substring(line.lastIndexOf('_') + 1, line.length() - 1)
                String patientSource = trialName + patientEnd.trim()
                Long patientNum = null
		String stmt1 = 'select patient_num from I2B2DEMODATA.patient_dimension where sourcesystem_cd = ?'
                sql.eachRow(stmt1, [patientSource]) { row ->
                    patientNum = row.patient_num
                }
		if (patientNum == null) {
		    throw new Exception('Patient_num for ' + patientSource + ' does not exist')
		}

		for (String concept in conceptList) {
		    String stmt2 = '''
				insert into I2B2DEMODATA.observation_fact(patient_num, concept_cd, provider_id, modifier_cd,
				valueflag_cd, location_cd, import_date, sourcesystem_cd)
				values(?, ?, '@', ?, '@', '@', sysdate, ?)'''
                    sql.execute(stmt2, [patientNum, concept, trialName, patientSource])
                }
            }
        }
    }

    void correctPatientNum(String datasetFileName, String trialName) {
        Sql sql = getSql()

	for (String line in new File(datasetFileName).readLines()) {
            // Only use N (Normal data)
            if (line.endsWith('N')) {
		String patientEnd = line.substring(line.lastIndexOf('_') + 1, line.length() - 1)
                String patientSource = trialName + patientEnd.trim()
                Long patientNum = null
		String stmt1 = 'select patient_num from I2B2DEMODATA.patient_dimension where sourcesystem_cd = ?'
                sql.eachRow(stmt1, [patientSource]) { row ->
                    patientNum = row.patient_num
                }
		if (patientNum == null) {
		    throw new Exception('Patient_num for ' + patientSource + ' does not exist')
		}

		sql.execute 'update DEAPP.de_subject_snp_dataset set patient_num = ? where subject_id = ?',
		    patientNum, patientEnd
            }
        }
    }

    /**
     * @param file property file from the default location
     * @return populated properties
     */
    private Properties loadConfiguration(File file) throws IOException {
	Properties properties = new Properties()
        FileInputStream fis = new FileInputStream(file)
	properties.load fis
        fis.close()
	properties
    }

    /**
     *  Merge the genotyping call given by Affy GTC, with the signal intensity for channel A and B, given by
     *  GenePattern SNPFileCreator module.
     *  This is only needed for SNP Mapping 500K (250K NSP and 250K STY). For SNP Mapping 500K, Affy GTC only output
     *  contrast and strength, instead of signal for channel A and B, as for SNP Generation 6.0.
     *  GenePattern SNPFileCreator has its own share of bugs. It fails to do genotyping, and its chromosomal positions are based
     *  on hg18, not on up-to-date hg19 as in Affy GTC.
     *  This needs large memory, in exchange for a simpler code structure.
     */
    void mergeAffyGTCGenotypeWithGPSignal(String affyFileName, String gpFileName, String resultFileName) {

	String affyHeader = null
	List<String> affyStringList = []
	for (String line in new File(affyFileName).readLines()) {
            if (line.startsWith('#')) {
                // Skip the annotation lines
            }
            else if (line.startsWith('Probe Set ID')) {
                affyHeader = line.trim()
            }
            else if (line.startsWith('AFFX-')) {
                // Skip the AFFX probes
            }
            else if (line.startsWith('SNP_A-')) {
		affyStringList << line.trim()
            }
        }

	String gpHeader = null
	List<String> gpStringList = []
	for (String line in new File(gpFileName).readLines()) {
            if (line.startsWith('SNP\tChromosome\tPhysicalPosition')) {
                gpHeader = line.trim()
            }
            else if (line.startsWith('AFFX-')) {
                // Skip the AFFX probes
            }
            else if (line.startsWith('SNP_A-')) {
		gpStringList << line.trim()
            }
        }

        // The number of SNP probes should be equal for Affy and GP files
	if (affyStringList.size() != gpStringList.size()) {
            throw new Exception('The Affy file has ' + affyStringList.size() + ' SNP probes, while the GP file has ' +
				gpStringList.size() + ' SNP probes.')
	}

	List<String> datasetList = []
        String[] columnArray = gpHeader.split('\t')
        for (int i = 3; i < columnArray.length; i = i + 3) {
            String columnName = columnArray[i]
	    datasetList << columnName.substring(0, columnName.indexOf('_') + 2)
        }
        String[] affyColumnArray = affyHeader.split('\t')
        for (int j = 0; j < datasetList.size(); j++) {
            String columnName = affyColumnArray[j + 1]
	    if (!columnName.startsWith(datasetList[j])) {
                // Make sure the headers of affy and GP files are consistent
                throw new Exception('The ' + j + '-th dataset name in Affy file is ' + columnName +
				    ', but the corresponding GP file dataset name is ' + datasetList[j])
            }
        }

        BufferedWriter writer = null
        try {
            writer = new BufferedWriter(new FileWriter(resultFileName))
            // Write out the header, exactly the same as the GP file
            writer.write(gpHeader + '\n')

            for (int k = 0; k < gpStringList.size(); k++) {
		String[] gpValueList = gpStringList[k].split('\t')
		String[] affyValueList = affyStringList[k].split('\t')
                String gpSNPName = gpValueList[0]
                String affySNPName = affyValueList[0]
		if (!gpSNPName.equalsIgnoreCase(affySNPName)) {    // Make sure the SNP Names match
		    throw new Exception('The ' + k + '-th SNP in GP file is ' + gpSNPName + ', but in Affy file is ' + affySNPName)
                }
                String affyChrom = affyValueList[1 + datasetList.size() + 1]
		if (affyChrom.contains('---')) {
		    // The SNP probe no longer has a valid chromosomal position. Skip the whole line
		    continue
		}
                String affyChromPos = affyValueList[1 + datasetList.size() + 2]

		StringBuilder buf = new StringBuilder()
		buf << gpSNPName << '\t' << affyChrom << '\t' << affyChromPos

                for (int m = 0; m < datasetList.size(); m++) {
                    String signalA = gpValueList[3 + 3 * m]
                    String signalB = gpValueList[3 + 3 * m + 1]
                    String genotype = affyValueList[1 + m]
		    if (genotype.equalsIgnoreCase('NoCall')) {
			genotype = 'NC'
		    }
		    buf << '\t' << signalA << '\t' << signalB << '\t' << genotype
                }

                writer.write(buf.toString() + '\n')
            }
        }
        finally {
	    writer?.flush()
	    writer?.close()
        }
    }

    /** Merges the NSP SNP file and STY SNP File. */
    void mergeNspAndStyFiles(String nspFileName, String styFileName, String resultFileName) {
        String nspHeader = null
        String styHeader = null

        BufferedWriter writer = null
        try {
            writer = new BufferedWriter(new FileWriter(resultFileName))

	    Set<String> nspProbeSet = []
	    Set<String> styProbeSet = []

	    for (String line in new File(nspFileName).readLines()) {
                if (line.startsWith('SNP\tChromosome\tPhysicalPosition')) {
                    nspHeader = line.trim()
                }
                else {
		    String probeName = line.substring(0, line.indexOf('\t'))
                    if (nspProbeSet.contains(probeName)) {
			logger.debug 'The NSP probe {} is duplicated in the file', probeName
                    }
		    nspProbeSet << probeName
                }

		writer.write(line + '\n') // Write out everything in the NSP file, including the header
            }

	    for (String line in new File(styFileName).readLines()) {
                if (line.startsWith('SNP\tChromosome\tPhysicalPosition')) {
                    styHeader = line.trim()
                }
                else {
		    String probeName = line.substring(0, line.indexOf('\t'))
                    if (styProbeSet.contains(probeName)) {
			logger.debug 'The STY probe {} is duplicated in the file', probeName
                    }
		    styProbeSet << probeName
                    if (nspProbeSet.contains(probeName)) {
			logger.debug 'The STY probe {} is duplicated in the NSP file', probeName
                    }
		    writer.write(line + '\n') // Only write out SNP data lines, excluding the header
                }
            }

            // Make sure the two files have the same header
	    if (nspHeader.trim() != styFileName.trim()) {
		logger.error 'The headers of two files are not the same:\n{}\n{}\n\n',
		    nspHeader, styHeader
            }
        }
        finally {
	    writer?.flush()
	    writer?.close()
        }
    }

    void generateGPSampleFile(String snpFileName, String sampleFileName, String diseaseSuffix,
	                      String normalSuffix, String gender) {
        BufferedReader snpReader = new BufferedReader(new FileReader(snpFileName))
        String snpHeader = snpReader.readLine()
        String[] snpColumnList = snpHeader.split('\t')

        File sampleFile = new File(sampleFileName)
	sampleFile << 'Array\tSample\tType\tPloidy(numeric)\tGender\tPaired\n'
        for (int i = 3; i < snpColumnList.length; i = i + 6) {
	    String normalName = null
	    String diseaseName = null
            for (int k = 0; k < 2; k++) {
                String columnName = snpColumnList[i + 3 * k]
		String datasetName = columnName.substring(0, columnName.indexOf('_Allele_A'))
		if (datasetName.endsWith(normalSuffix)) {
                    normalName = datasetName
		}
		else if (datasetName.endsWith(diseaseSuffix)) {
                    diseaseName = datasetName
		}
	    }
	    sampleFile << normalName + '\t' + normalName + '\tcontrol\t2\t' + gender + '\tYes\n'
	    sampleFile << diseaseName + '\t' + diseaseName + '\tdisease\t2\t' + gender + '\t' + normalName + '\n'
        }
    }

    /**
     * Generates the data file used by SQL Loader to populate de_snp_data_by_probe table
     * The SQL procedure that is used to populate other fields (change the trial_name):
     *
     DECLARE
     snp_id_rec NUMBER
     snp_name_rec VARCHAR2(255)
     probe_id_rec NUMBER
     probe_cnt NUMBER
     BEGIN
     FOR data_rec in (select * from de_snp_data_by_probe where trial_name = 'GSE19539' order by snp_data_by_probe_id)
     LOOP
     select count(1) into probe_cnt from de_snp_probe where probe_name = data_rec.probe_name

     IF probe_cnt = 1 THEN
     select snp_probe_id, snp_id, snp_name into probe_id_rec, snp_id_rec, snp_name_rec from de_snp_probe where probe_name = data_rec.probe_name

     update de_snp_data_by_probe set probe_id = probe_id_rec, snp_id = snp_id_rec, snp_name = snp_name_rec where snp_data_by_probe_id = data_rec.snp_data_by_probe_id
     commit
     END IF
     END LOOP
     END
     */
    void loadDataByProbe(String xcnByPatientDirName, String probeDefFileName, String trialName,
                         String dataByProbeLoadingFile) {
	List<String> datasetNameList = []

	int datasetLocationIndex = 0
	sql.eachRow('select * from DEAPP.de_subject_snp_dataset where trial_name = ? order by subject_snp_dataset_id', [trialName]) { row ->
            String datasetName = row.dataset_name
	    datasetNameList << datasetName
	    datasetLocationIndex++
        }

	List<BufferedReader> xcnReaderList = []
	for (String datasetName in datasetNameList) {
	    xcnReaderList << new BufferedReader(new FileReader(
		new File(xcnByPatientDirName, datasetName + '_all.xcn')))
        }

        BufferedWriter loadingFileWriter = new BufferedWriter(new FileWriter(dataByProbeLoadingFile))
        BufferedReader probeDefReader = new BufferedReader(new FileReader(probeDefFileName))
	String lineProbeDef
	while ((lineProbeDef = probeDefReader.readLine()) != null && lineProbeDef.trim()) {
	    String probeName = lineProbeDef.split('\t')[0]

	    StringBuilder xcnDataBuf = new StringBuilder()
	    for (BufferedReader xcnReader in xcnReaderList) {
                // The snp data is compacted in the format of [##.##][AB] for copy number and genotype, in the same order as .xcn file
		xcnDataBuf << xcnReader.readLine().replace('\t', '')
            }
	    loadingFileWriter.writeLine probeName + '\t' + trialName + '\t' + xcnDataBuf
        }

        loadingFileWriter.close()
        probeDefReader.close()
	for (BufferedReader xcnReader in xcnReaderList) {
            xcnReader.close()
        }
    }

    void addRsIdSortedDef(String annotLoadingFileName) {
	Map<String, String> probeRsIdMap = [:]
        // The annotation loading file is snp id, probe, chrom information extracted from Affy annotation file
	for (String line in new File(annotLoadingFileName).readLines()) {
	    if (line?.trim()) {
                String[] values = line.split('\t')
                String rsId = values[0]
                String probeName = values[3]
		probeRsIdMap[probeName] = rsId
            }
        }

	sql.eachRow('select * from DEAPP.de_snp_probe_sorted_def order by snp_probe_sorted_def_id') { row ->
            Long defId = row.snp_probe_sorted_def_id

	    StringBuilder rsIdDefBuf = new StringBuilder()
            Clob clob = row.probe_def
	    String probeDefStr = clob.asciiStream.text
            String[] probeDefLines = probeDefStr.split('\n')
	    for (String lineStr in probeDefLines) {
                String[] probeValues = lineStr.split('\t')
                String probeName = probeValues[0]
                String chrom = probeValues[1]
                String chromPos = probeValues[2]
		String rsId = probeRsIdMap[probeName]
                if (rsId != null) {
		    rsIdDefBuf << rsId << '\t' << chrom << '\t' << chromPos << '\t\n'
                }
                else {
		    rsIdDefBuf << lineStr << '\n'
                }
            }

	    sql.execute('update DEAPP.de_snp_probe_sorted_def set snp_id_def = ? where snp_probe_sorted_def_id = ?',
			[rsIdDefBuf.toString(), defId])
        }
    }

    static void main(String[] args) {
        SnpDataLoading sdl = new SnpDataLoading()

        // extract parameters
	File path = new File(SnpDataLoading.protectionDomain.codeSource.location.path)
	Properties props = sdl.loadConfiguration(new File(path.parent, 'SnpViewer.properties'))

        // create db connection object
	sdl.getSql props.oracle_server, props.oracle_port, props.oracle_sid,
	    props.oracle_user, props.oracle_password

        /*
         def datasetListFileName = outPathName + File.separator + datasetListName
	 sdl.loadDataSet(datasetListFileName, props.get("trialName"), props.get("platformName"), props.get("assayName"),
	 props.get("normalSuffix"), props.get("diseaseSuffix"), props.get("normalSampleType"), props.get("diseaseSampleType"))
         */

        /*
	 sdl.loadDataByPatient(datasetListFileName, props.get("trialName"), props.get("outPathName"), props.get("normalSuffix"),
	 props.get("diseaseSuffix"), props.get("normalSampleType"), props.get("diseaseSampleType"))
         */

        /*
	 sdl.loadSNPProbeSortedDef(props.get("outPathName") + File.separator + probeDefFileName, props.get("platformName"))
         */

        /*
	 String affyFileName = props.get("affy_genotype_file")
	 String gpSignalFileName = props.get("gp_signal_file")
	 String resultFileName = props.get("genotype_signal_merged_file")
         sdl.mergeAffyGTCGenotypeWithGPSignal(affyFileName, gpSignalFileName, resultFileName)
         */

        /*
	 String nspFileName = props.get("affy_500k_nsp_snp_file")
	 String styFileName = props.get("affy_500k_sty_snp_file")
	 String resultFileName = props.get("affy_500k_merged_snp_file")
         sdl.mergeNspAndStyFiles(nspFileName, styFileName, resultFileName)
         */

        /*
	 String snpFileName = props.get("affy_500k_merged_snp_file")
	 String sampleFileName = props.get("gp_sample_file")
	 String diseaseSuffix = props.get("disease_suffix")
	 String normalSuffix = props.get("normal_suffix")
	 String gender = props.get("patient_gender");	// TODO: how to get each patient's gender, and output to sample info file.
         sdl.generateGPSampleFile(snpFileName, sampleFileName, diseaseSuffix, normalSuffix, gender)
        */

        /*
	 String xcnFileName = props.get("xcn_file_for_all_datasets")
	 String resultFolder = props.get("xcn_file_split_result_folder")
         sdl.splitBatchXCN(xcnFileName, resultFolder)
         */

        /*
	 String datasetListFileName = props.get("dataset_list_file")
	 String trialName = props.get("trial_name")
	 String platformName = props.get("platform_name")
	 String normalSuffix = props.get("normal_suffix")
	 String diseaseSuffix = props.get("disease_suffix")
	 String genderForAll = props.get("patient_gender")
         sdl.loadDataSet(datasetListFileName, trialName, platformName,
         normalSuffix, diseaseSuffix, genderForAll)
         */

	String xcnByPatientDirName = props.get("xcn_by_patient_dir")
	String probeDefFileName = props.get("probe_def_file")
	String trialName = props.get("trial_name")
	String dataByProbeLoadingFile = props.get("data_by_probe_loading_file")
        sdl.loadDataByProbe(xcnByPatientDirName, probeDefFileName, trialName, dataByProbeLoadingFile)

        /*
	 String annotLoadingFileName = props.get("affy_annotation_data_file")
         sdl.addRsIdSortedDef(annotLoadingFileName)
         */
    }
}

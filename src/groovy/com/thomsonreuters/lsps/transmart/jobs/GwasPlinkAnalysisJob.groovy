package com.thomsonreuters.lsps.transmart.jobs

import grails.util.Holders
import groovy.sql.Sql
import jobs.AbstractAnalysisJob
import jobs.steps.Step
import org.anarres.lzo.LzoAlgorithm
import org.anarres.lzo.LzoInputStream
import org.anarres.lzo.LzoLibrary
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.support.lob.DefaultLobHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import org.transmartproject.core.exceptions.EmptySetException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.exceptions.UnexpectedResultException

import javax.sql.DataSource
import java.nio.file.FileSystems

/**
 * Date: 22-Apr-16
 * Time: 13:07
 */
@Component
@Scope('job')
class GwasPlinkAnalysisJob extends AbstractAnalysisJob implements InitializingBean {
    TransactionTemplate transactionTemplate
    JdbcTemplate jdbcTemplate

    @Autowired
    PlatformTransactionManager transactionManager

    @Autowired
    DataSource dataSource

    final static previewFileExt = ['.adjusted', '.assoc', '.qassoc', '.fisher', '.linear', '.logistic'] as SortedSet
    final static Map<String, Map> analysis = [
            assoc            : ['--assoc'],
            assoc_fisher     : ['--assoc', 'fisher'],
            assoc_fisher_midp: ['--assoc', 'fisher-midp'],
            linear           : ['--linear'],
            logistic         : ['--logistic'],
    ]


    String getAnalysisName() {
        def name = getParams().plinkAnalysisName as String
        name.replaceAll(/[\x00-\x19\/\xff]+/, '')
    }

    String getAnalysisType() {
        def name = getParams().plinkAnalysisType as String
        if (!analysis[name]) {
            throw new NoSuchResourceException("Unknown analysis type: '$name'")
        }
        return name
    }

    String getPreviewFileName() {
        def fs = FileSystems.getDefault()
        def resultsDirName = fs.getPath(getTemporaryDirectory().toString(), 'plink-results') as String
        def resultsIndex = previewFileExt.size()
        def resultsFileName
        new File(resultsDirName).eachFile { file ->
            if (!file.isFile() || !file.canRead()) {
                return
            }
            def name = (file as String).replace(resultsDirName + File.separator, '')
            previewFileExt.eachWithIndex { ext, i ->
                if (i >= resultsIndex) {
                    return
                }
                if (name.endsWith(ext)) {
                    resultsFileName = name
                    resultsIndex = i
                }
            }
        }

        return resultsFileName ?: 'error.log'
    }

    @Override
    void afterPropertiesSet() throws Exception {
        transactionTemplate = new TransactionTemplate(transactionManager)
        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    class PrepareInputFilesStep implements Step {
        public final static Integer PHENO_CONTROL = 1
        public final static Integer PHENO_CASE = 2
        public final static Integer PHENO_MISSING = -9

        private final static missingPheno = ['-9', 'null', ''] as Set

        @Override
        String getStatusName() {
            return "Preparing input files..."
        }

        private InputStream decomressStream(InputStream stream) {
            def decompressor = LzoLibrary.instance.newDecompressor(LzoAlgorithm.LZO1X, null)
            new LzoInputStream(stream, decompressor)
        }

        private Map<String, String> getPatientAndValueByPath(sql, paths, allowCategorical) {
            def hasCategorical = false
            def rawData = [:]
            sql.rows("""\
                select patient_num, tval_char, nval_num
                  from      i2b2demodata.observation_fact of
                       join i2b2demodata.concept_dimension cd on cd.concept_cd=of.concept_cd
                 where concept_path in ( ${(['?'] * paths.size()).join(',')} )
            """.stripIndent(), paths).each {
                def patientId = it.patient_num as String
                def isNumeric = it.tval_char == 'E'
                if (!hasCategorical && !isNumeric) {
                    hasCategorical = true
                }
                rawData[patientId] = (isNumeric ? it.nval_num : it.tval_char) as String
            }

            if (!hasCategorical) {
                return rawData
            }
            if (!allowCategorical) {
                return [:]
            }

            def control = null
            def result = [:]
            rawData.each { pid, val ->
                if (val.toLowerCase() in missingPheno) {
                    result[pid] = PHENO_MISSING
                } else {
                    if (control == null) {
                        control = val
                    }
                    result[pid] = val == control ? PHENO_CONTROL : PHENO_CASE
                }
            }

            return result
        }

        @Override
        void execute() {
            def sql = new Sql(dataSource)
            def resultInstanceIds = []
            [getParams().result_instance_id1, getParams().result_instance_id2].findAll { !!it }.each {
                resultInstanceIds.add(it)
            }
            if (!resultInstanceIds) {
                throw new EmptySetException("No subsets selected")
            }

            def phenotypes = getParams().phenotypes
            def covariates = getParams().covariates
            Map<String, Map> patients = null
            Map<String, String> valueForPhenotypeByPatient = null, valueForCovariatesByPatient = null
            try {
                patients = sql.rows("""
                    select pd.sourcesystem_cd, max(ps.result_instance_id) as result_instance_id, ps.patient_num
                    from i2b2demodata.qt_patient_set_collection ps, i2b2demodata.patient_dimension pd
                    where ps.patient_num = pd.patient_num and ps.result_instance_id IN (${
                    (['?'] * resultInstanceIds.size()).join(',')
                })
                    group by pd.sourcesystem_cd, ps.patient_num
                """, resultInstanceIds).collectEntries {
                    def sourceSystemCd = it.sourcesystem_cd as String
                    def parts = sourceSystemCd.split(':', 2)
                    def patientNum = it.patient_num as String
                    [parts[1], [iid: parts[1], studyId: parts[0], phenotype: resultInstanceIds.indexOf(it.result_instance_id as String) + 1, patientNum: patientNum]]
                }

                if (!patients) {
                    throw new EmptySetException("The patient set is empty.")
                }

                if (phenotypes.size() > 0)
                    valueForPhenotypeByPatient = getPatientAndValueByPath(sql, phenotypes, false)
                if (covariates.size() > 0)
                    valueForCovariatesByPatient = getPatientAndValueByPath(sql, covariates, true)

            } finally {
                sql.close()
            }

            def studyId = patients.values().first().studyId

            def tempDir = getTemporaryDirectory()
            def analysisName = getAnalysisName()
            def familyIds = [:]
            transactionTemplate.execute({
                jdbcTemplate.query("select bed, bim, fam from gwas_plink.plink_data where study_id = ?", [studyId] as Object[], { rs ->
                    if (!rs.next()) {
                        throw new EmptySetException("The GWAS Plink data not found for study '$studyId'.")
                    }
                    def lobHandler = new DefaultLobHandler(wrapAsLob: true)
                    new File(tempDir, "${analysisName}.bed") << decomressStream(lobHandler.getBlobAsBinaryStream(rs, 1))
                    new File(tempDir, "${analysisName}.bim") << decomressStream(lobHandler.getBlobAsBinaryStream(rs, 2))
                    def fam = decomressStream(lobHandler.getBlobAsBinaryStream(rs, 3))
                    new File(getTemporaryDirectory(), "${getAnalysisName()}.fam").withWriter { out ->
                        fam.eachLine { line ->
                            def tokens = line.split(/[ \t]+/)
                            String iid = tokens[1]
                            familyIds[iid] = tokens[0]

                            for (int i = 0; i < tokens.size(); i++) {
                                String token = tokens[i]
                                if (i == 5 && resultInstanceIds.size() > 1) {
                                    token = patients[iid]?.phenotype ?: '1'
                                }
                                out.write(token)
                                out.write('\t')
                            }
                            out.write('\n')
                        }
                    }
                } as ResultSetExtractor)
            } as TransactionCallbackWithoutResult)

            new File(getTemporaryDirectory(), "${getAnalysisName()}_id.txt").withWriter {
                for (Map patient in patients.values()) {
                    def iid = patient.iid
                    if (familyIds.containsKey(iid)) {
                        it.write("${familyIds[iid]}\t$iid\n")
                    }
                }
            }

            if (phenotypes.size() > 0) {
                new File(getTemporaryDirectory(), "${getAnalysisName()}_pheno.txt").withWriter {
                    for (Map patient in patients.values()) {
                        def iid = patient.iid
                        def patientNum = patient.patientNum
                        if (familyIds.containsKey(iid) && valueForPhenotypeByPatient.containsKey(patientNum)) {
                            it.write("${familyIds[iid]}\t$iid\t${valueForPhenotypeByPatient[patientNum]}\n")
                        }
                    }
                }
            }

            if (covariates.size() > 0) {
                new File(getTemporaryDirectory(), "${getAnalysisName()}_covar.txt").withWriter {
                    for (Map patient in patients.values()) {
                        def iid = patient.iid
                        def patientNum = patient.patientNum
                        if (familyIds.containsKey(iid) && valueForCovariatesByPatient.containsKey(patientNum)) {
                            it.write("${familyIds[iid]}\t$iid\t${valueForCovariatesByPatient[patientNum]}\n")
                        }
                    }
                }
            }
        }
    }

    class RunPlinkStep implements Step {
        @Override
        String getStatusName() {
            return "Running analyze with plink..."
        }

        List<String> getOutputOptions() {
            def analysisType = getAnalysisType()
            def confOpts = getAnalysis().get(analysisType)
            if (!confOpts) {
                throw new NoSuchResourceException("Unknown analysis type: '$analysisType'")
            }
            def options = [*confOpts]
            def pValueThreshold = getParams().pValueThreshold
            def phenotypes = getParams().phenotypes
            def covariates = getParams().covariates
            String additionalOption = getParams().additionalOption
            if (analysisType == "linear" || analysisType == "logistic") {
                if (additionalOption in ["genotypic", "dominant", "recessive"]) {
                    options << additionalOption
                }
                options << "hide-covar"
                /* if (phenotypes.size() > 0) {
                    options << '--all-pheno'
                } */
            }
            if (pValueThreshold && (pValueThreshold as String).isNumber()) {
                options += ['--pfilter', pValueThreshold]
            }
            if (phenotypes.size() > 0 && analysisType != 'logistic') {
                def phfname = "${getAnalysisName()}_pheno.txt"
                if (getParams().makePheno) {
                    options += ['--make-pheno', phfname, '*']
                } else {
                    options += ['--pheno', phfname, '--prune']
                }
            }
            if (covariates.size() > 0) {
                options += ['--covar', "${getAnalysisName()}_covar.txt"]
            }
            options
        }

        @Override
        void execute() {
            def fs = FileSystems.getDefault()
            def plinkPath = Holders.config.grails.plugin.transmartGwasPlink.plinkPath
            def resultsDir = new File(getTemporaryDirectory(), "plink-results")
            resultsDir.mkdir()
            def error = new File(getTemporaryDirectory(), "error.log")
            def analysisName = getAnalysisName()
            def opts = getOutputOptions()
            Process process = new ProcessBuilder([
                    plinkPath,
                    "--bfile", analysisName,
                    "--keep", "${analysisName}_id.txt",
                    "--out", fs.getPath('plink-results', analysisName) as String,
                    "--adjust",
                    *opts
            ] as String[])
                    .directory(getTemporaryDirectory())
                    .redirectError(error)
                    .start()

            int exitCode = process.waitFor()
            if (exitCode == 127) {
                throw new NoSuchResourceException("`plink` exited with exit code $exitCode. Most probably `plink` was not found by configured path")
            } else if (exitCode != 0) {
                throw new UnexpectedResultException("`plink` exited with error: ${error.exists() ? error.text : 'unknown'}")
            }
        }
    }

    @Override
    protected List<Step> prepareSteps() {
        return [
                new PrepareInputFilesStep(),
                new RunPlinkStep()
        ]
    }

    @Override
    protected List<String> getRStatements() {
        return []
    }

    @Override
    protected getForwardPath() {
        "/gwasPlink/resultOutput?jobName=${name}&analysisName=${getAnalysisName()}&previewFileName=${getPreviewFileName() ?: ''}&previewRowsCount=${getParams().previewRowsCount ?: ''}"
    }
}

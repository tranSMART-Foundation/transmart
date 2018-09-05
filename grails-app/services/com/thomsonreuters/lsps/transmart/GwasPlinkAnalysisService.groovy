package com.thomsonreuters.lsps.transmart

import com.google.common.collect.Maps
import com.recomdata.transmart.data.association.RModulesController
import grails.converters.JSON
import grails.transaction.Transactional
import grails.util.Holders
import jobs.UserParameters
import jobs.misc.AnalysisQuartzJobAdapter
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static jobs.misc.AnalysisQuartzJobAdapter.*

@Transactional
class GwasPlinkAnalysisService {
    def grailsApplication
    def currentUserBean
    def quartzScheduler

    private final String CSV_DELIMITER = ','

    private static class JobWorkspace {
        final File jobDir

        JobWorkspace(String jobName) {
            this.jobDir = new File(Holders.config.RModules.tempFolderDirectory as String, jobName)
        }

        File getWorkingDir() {
            new File(jobDir, "workingDirectory")
        }
    }

    private static class PlinkJobWorkspace extends JobWorkspace {
        PlinkJobWorkspace(String jobName) {
            super(jobName)
        }

        File getPlinkResultsDir() {
            new File(workingDir, "plink-results")
        }
    }

    String prepareZippedResult(String jobName, String analysisName) {
        final jobWorkspace = new PlinkJobWorkspace(jobName)
        String zipFileName = "${analysisName}.zip"
        byte[] buffer = new byte[512 * 1024]
        File zipFile = new File(jobWorkspace.jobDir, zipFileName)
        if (zipFile.exists()) {
            zipFile.delete()
        }
        def workDir = jobWorkspace.getWorkingDir()
        def resDirName = (jobWorkspace.getPlinkResultsDir() as String).replace((workDir as String) + workDir.separator, '')
        def zip = new ZipOutputStream(new FileOutputStream(zipFile))
        zip.setLevel(ZipOutputStream.DEFLATED);
        workDir.eachFileRecurse { file ->
            if (!file.isFile() || !file.canRead()) {
                return
            }
            def name = (file as String).replace((workDir as String) + workDir.separator, '')
            if (!(name.startsWith(analysisName) || name.startsWith(resDirName)) || name.endsWith('.bim') || name.endsWith('.bed')) {
                return
            }
            zip.putNextEntry(new ZipEntry(name))
            def bytesRead
            def fiStream = new FileInputStream(file)
            while ((bytesRead = fiStream.read(buffer)) != -1) {
                zip.write(buffer, 0, bytesRead)
            }
            zip.flush()
            zip.closeEntry()
        }
        zip.finish()
        zip.close()
        return zipFileName
    }

    def getPreviewData(String jobName, String previewFileName, int previewRowsCount) {
        if (!previewFileName)
            return null

        def jobWorkspace = new PlinkJobWorkspace(jobName)
        if (previewFileName == 'error.log') {
            def logFile = new File(jobWorkspace.workingDir, previewFileName)
            if (!logFile.canRead())
                return null
            return new String(logFile.readBytes())
        }

        def result = []
        def csvFile = new File(jobWorkspace.plinkResultsDir, previewFileName + '.csv')
        new File(jobWorkspace.plinkResultsDir, previewFileName).withReader { reader ->
            String line
            Integer pValIdx
            def topLines = new TreeMap<Double, List>()
            def nLines = 0
            while ((line = reader.readLine()) != null) {
                def vals = line.trim().split('\\s+')
                csvFile << vals.collect { convertToCSV(it) }.join(CSV_DELIMITER) + '\n'
                if (reader.getLineNumber() == 1) {
                    pValIdx = vals.findIndexOf { it == 'P' }
                    result.add(vals)
                    continue
                }
                if (pValIdx < 0) {
                    result.add(vals)
                    if (reader.getLineNumber() <= previewRowsCount) {
                        continue
                    } else {
                        break
                    }
                }
                try {
                    def pVal = vals[pValIdx] as Double
                    if (nLines >= previewRowsCount) {
                        def maxPVal = topLines.lastKey()
                        if (pVal >= maxPVal) {
                            continue
                        }
                        topLines[maxPVal].pop()
                        nLines--
                        if (topLines[maxPVal].size() == 0) {
                            topLines.remove(maxPVal)
                        }
                    }
                    if (!topLines.containsKey(pVal)) {
                        topLines[pVal] = []
                    }
                    topLines[pVal] << vals
                    nLines++
                } catch (Exception e) {
                }
            }
            topLines.each { _, v ->
                result += v
            }
        }

        result
    }

    def convertToCSV(val) {
        if (val.equals(null)) {
            return ''
        }
        return val =~ /[,"]/ ? '"' + val.replaceAll(/"/, '""') + '"' : val
    }

    def createAnalysisJob(Map params, String analysisGroup, Class jobClazz, boolean useAnalysisConstraints = true) {
        params.phenotypes = JSON.parse(params.phenotypes)
        params.covariates = JSON.parse(params.covariates)
        UserParameters userParams = new UserParameters(map: Maps.newHashMap(params))

        params[PARAM_GRAILS_APPLICATION] = grailsApplication
        params[PARAM_JOB_CLASS] = jobClazz
        if (useAnalysisConstraints) {
            // TODO: move `createAnalysisConstraints` to service
            params.put(PARAM_ANALYSIS_CONSTRAINTS, RModulesController.createAnalysisConstraints(params))
        }

        params.put(PARAM_USER_PARAMETERS, userParams)
        params.put(PARAM_USER_IN_CONTEXT, currentUserBean.targetSource.target)
        params.put('subsetSelectedFilesMap', [subset1: [], subset2: []])
        params.put("jobName", params.jobName)

        JobDetail jobDetail = new JobDetail(params.jobName, params.jobType, AnalysisQuartzJobAdapter)
        jobDetail.jobDataMap = new JobDataMap(params)
        SimpleTrigger trigger = new SimpleTrigger("triggerNow ${Calendar.instance.time.time}", analysisGroup)
        quartzScheduler.scheduleJob(jobDetail, trigger)
    }
}

package com.recomdata.transmart.data.export

import com.recomdata.transmart.domain.i2b2.AsyncJob
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value

@Slf4j('logger')
class SweepingService {

    @Value('${com.recomdata.export.jobs.sweep.fileAge:0}')
    private int fileAge

    @Value('${com.recomdata.plugins.tempFolderDirectory:}')
    private String tempFolderDirectory

    private DeleteDataFilesProcessor processor = new DeleteDataFilesProcessor()

    @Transactional
    void sweep() {
        logger.info 'Triggering file sweep'
	List<AsyncJob> jobList = AsyncJob.findAllByJobTypeAndJobStatusAndLastRunOnLessThan(
	    'DataExport', 'Completed', new Date() - fileAge)

	for (AsyncJob job in jobList) {
	    if (processor.deleteDataFile(job.viewerURL, job.jobName, tempFolderDirectory)) {
                job.delete()
            }
        }
    }
}

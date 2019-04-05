package com.recomdata.genepattern

import groovy.transform.CompileStatic
import org.json.JSONObject

@CompileStatic
class WorkflowStatus {

    List<JobStatus> jobStatusList = []
    String currentStatus = 'Starting' // Starting, Running, Cancelled, Completed
    JSONObject result
    int currentStatusIndex = 1

    // repeat count to help manage dup javascript firings
    int rpCount = 0

    /**
     * update object if it's in set, add if not exists
     */
    void addJobStatus(JobStatus status) {
	int index = jobStatusList.indexOf(status)
	if (index > -1) {
	    JobStatus s = jobStatusList[index]
            s.status = status.status
            s.message = status.message
            s.gpJobId = status.gpJobId
            s.totalRecord = status.totalRecord
        }
        else {
	    jobStatusList << status
        }
    }

    void addNewJob(String sname) {
	jobStatusList << new JobStatus(name: sname, status: 'Q')
    }

    void setCurrentJobStatus(JobStatus status) {
        // set previous job to be completed..
	int index = jobStatusList.indexOf(status)
	if (index > -1) {
	    for (int i = 0; i < index; i++) {
                jobStatusList[i].setComplete()
            }
        }
        addJobStatus(status)
        // find running index
        int si = 0
        for (s in jobStatusList) {
            si++
            if (s.isRunning()) {
                currentStatusIndex = si
                break
            }
        }
        currentStatus = 'Running'
    }

    void setCancelled() {
	currentStatus = 'Cancelled'
    }

    boolean isCancelled() {
	currentStatus == 'Cancelled'
    }

    boolean isCompleted() {
	currentStatus == 'Completed'
    }

    void setCompleted() {
        currentStatus = 'Completed'
    }
}

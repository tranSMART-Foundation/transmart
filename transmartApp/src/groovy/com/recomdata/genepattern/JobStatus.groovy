package com.recomdata.genepattern

import groovy.transform.CompileStatic

@CompileStatic
class JobStatus {

    String name = ''
    String status = '' // C -completed, R- Running, I - initializing, Q -queued, T - terminated
    String message = ''
    def gpJobId
    int totalRecord = 0

    boolean isRunning() {
	status == 'R'
    }

    void setComplete() {
        status = 'C'
    }

    boolean isCompleted() {
	status == 'C'
    }

    int hashCode() {
        final int prime = 31
        int result = 1
	result = prime * result + (name == null ? 0 : name.hashCode())
	result
    }

    boolean equals(obj) {
	if (is(obj)) {
	    return true
	}
	if (obj == null) {
            return false
	}
	if (getClass() != obj.getClass()) {
            return false
	}

	name == ((JobStatus) obj).name
    }
}

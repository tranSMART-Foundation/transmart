/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 ******************************************************************/

package com.recomdata.transmart.domain.i2b2

import groovy.time.TimeCategory
import groovy.time.TimeDuration

class AsyncJob {

    static final Set<String> TERMINATION_STATES = ['Completed', 'Cancelled', 'Error']

    String altViewerURL
    String jobInputsJson
    String jobName
    String jobStatus
    Date jobStatusTime
    String jobType
    Date lastRunOn
    String results
    String viewerURL

    static mapping = {
        table 'I2B2DEMODATA.ASYNC_JOB'
	id generator: 'sequence', params: [sequence: 'searchapp.hibernate_sequence']
        version false

        viewerURL column: 'VIEWER_URL'
        altViewerURL column: 'ALT_VIEWER_URL'
        results column: 'JOB_RESULTS'
    }

    static constraints = {
	altViewerURL nullable: true
	jobInputsJson nullable: true
	jobName nullable: true
	jobStatus nullable: true
	jobStatusTime nullable: true
	jobType nullable: true
	lastRunOn nullable: true
	results nullable: true
	viewerURL nullable: true
    }

    TimeDuration getRunTime() {
	Date lastTime = TERMINATION_STATES.contains(jobStatus) ? jobStatusTime : new Date()
        lastRunOn && lastTime ? TimeCategory.minus(lastTime, lastRunOn) : null
    }

    void setJobStatus(String status) {
	if (jobStatus == status) {
            return
        }

	jobStatus = status
	jobStatusTime = new Date()
    }
}

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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/
package org.transmart.biomart

class ClinicalTrial extends Experiment {
    String blindingProcedure
    String dosingRegimen
    Long durationOfStudyWeeks
    String exclusionCriteria
    String genderRestrictionMfb
    String groupAssignment
    String inclusionCriteria
    Long maxAge
    Long minAge
    Long numberOfPatients
    Long numberOfSites
    String primaryEndPoints
    String routeOfAdministration
    String secondaryEndPoints
    String secondaryIds
    String studyOwner
    String studyPhase
    String studyType
    String subjects
    String trialNumber
    String typeOfControl

    static transients = ['values']

    static mapping = {
	table 'BIOMART.BIO_CLINICAL_TRIAL'
	id column: 'BIO_EXPERIMENT_ID'
        version false
        cache usage: 'read-only'

	exclusionCriteria type: 'text'
	inclusionCriteria type: 'text'
        studyType column: 'STUDYTYPE'
    }

    List getValues() {
	[title, trialNumber, primaryInvestigator, description, studyPhase, studyType, design, blindingProcedure,
	 durationOfStudyWeeks, completionDate, inclusionCriteria, exclusionCriteria, dosingRegimen, typeOfControl,
	 genderRestrictionMfb, groupAssignment, primaryEndPoints, secondaryEndPoints, routeOfAdministration,
	 secondaryIds, subjects, maxAge, minAge, numberOfPatients, numberOfSites, compoundNames, diseaseNames]
    }
}

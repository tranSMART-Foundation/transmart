<g:if test="${clinicalTrial == null}">
    <table class="detail">
	<tr><td>Trial not found.</td></tr>
</g:if>
<g:else>
    <table class="detail" style="width: 515px;">
        <tbody>
            <tr class="prop">
		<td valign="top" class="name">Trial number:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'trialNumber')}
                    <g:if test="${clinicalTrial?.files}">
			<g:set var="fcount" value="${0}"/>
			<g:each in="${clinicalTrial.files}" var="file">
                            <g:if test="${file.content.type == 'Experiment Web Link'}">
				<g:set var="fcount" value="${fcount++}"/>
				<g:if test="${fcount > 1}">,</g:if>
				<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
                            </g:if>
                            <g:elseif test="${file.content.type == 'Dataset Explorer Node Link' && search == 1}">
				<g:link controller="datasetExplorer" params="[path: file.content.location]">
				    Dataset Explorer
				    <asset:image src="internal-link.gif"/>
				</g:link>
                            </g:elseif>
			</g:each>
                    </g:if>
                    <g:if test="${searchId != null}">
			| <g:link controller="search" action="newSearch" id="${searchId}">
			      Search analyzed Data
			      <asset:image src="internal-link.gif"/>
			  </g:link>
                    </g:if>
		</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Title:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'title')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Owner:</td>
		<g:if test="${clinicalTrial.type == 'Clinical Trial'}">
                    <td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'studyOwner')}</td>
		</g:if>
		<g:else>
                    <td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'primaryInvestigator')}</td>
		</g:else>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Description:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'description')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Study phase:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'studyPhase')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Study type:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'studyType')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Study design:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'design')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Blinding procedure:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'blindingProcedure')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Duration of study (weeks):</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'durationOfStudyWeeks')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Completion date:</td>
		<g:if test="${fieldValue(bean: clinicalTrial, field: 'completionDate')?.length() > 10}">
                    <td valign="top"
			class="value">${fieldValue(bean: clinicalTrial, field: 'completionDate').substring(0, 11)}</td>
		</g:if>
		<g:else>
                    <td valign="top" class="value">&nbsp;</td>
		</g:else>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Compound:</td>
		<td valign="top" class="value">
                    <g:each in="${clinicalTrial.compounds}" var="compound">
			<g:createFilterDetailsLink id="${compound?.id}" label="${compound?.genericName}" type="compound"/>
			<br>
                    </g:each>
		</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Inclusion criteria:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'inclusionCriteria')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Exclusion criteria:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'exclusionCriteria')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Dosing regimen:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'dosingRegimen')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Type of control:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'typeOfControl')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Gender restriction mfb:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'genderRestrictionMfb')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Group assignment:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'groupAssignment')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Primary endpoints:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'primaryEndPoints')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Secondary endpoints:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'secondaryEndPoints')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Route of administration:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'routeOfAdministration')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Secondary ids:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'secondaryIds')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Subjects:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'subjects')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Max age:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'maxAge')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Min age:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'minAge')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Number of patients:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'numberOfPatients')}</td>
            </tr>

            <tr class="prop">
		<td valign="top" class="name">Number of sites:</td>
		<td valign="top" class="value">${fieldValue(bean: clinicalTrial, field: 'numberOfSites')}</td>
            </tr>
        </tbody>
    </table>
</g:else>

<!DOCTYPE html>
<html>
    <head>
	<asset:stylesheet href="ext/resources/css/ext-all.css"/>
	<asset:stylesheet href="ext/resources/css/xtheme-gray.css"/>
	<asset:stylesheet href="datasetExplorer.css"/>
    </head>

    <body>
	<g:if test="${clinicalTrial == null}">
	    <table class="detail">
		<tr>
		    <td>Study not found.</td>
		</tr>
	</g:if>
	<g:else>
	    <table class="detail" style="width: 515px;">
		<tbody>
		    <tr class="prop">
			<td valign="top" class="name">Trial number:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'trialNumber')}
			    <g:if test="${clinicalTrial?.files}">
				<g:set var='fcount' value="${0}"/>
				<g:each in="${clinicalTrial.files}" var="file">
				    <g:if test="${file.content.type == 'Experiment Web Link'}">
					<g:set var='fcount' value="${fcount++}"/>
					<g:if test="${fcount > 1}">,</g:if>
					<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
				    </g:if>
				    <g:elseif test="${file.content.type == 'Dataset Explorer Node Link' && search == 1}">
					<g:link controller='datasetExplorer' params="[path: file.content.location]">
					    Dataset Explorer
					    <img src="${resource(dir: 'images', file: 'internal-link.gif')}"/>
					</g:link>
				    </g:elseif>
				</g:each>
			    </g:if>
			    <g:if test="${searchId}">
				| <g:link controller='search' action='newSearch' id="${searchId}">
				    Search analyzed Data
				    <img src="${resource(dir: 'images', file: 'internal-link.gif')}"/>
				</g:link>
			    </g:if>
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Name:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'title')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Date:</td>
			<g:if test="${fieldValue(bean: clinicalTrial, field: 'completionDate')?.length() > 10}">
			    <td valign="top" class="value">
				${fieldValue(bean: clinicalTrial, field: 'completionDate').substring(0, 11)}
			    </td>
			</g:if>
			<g:else>
			    <td valign="top" class="value">&nbsp;</td>
			</g:else>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Owner:</td>
			<g:if test="${clinicalTrial.type == 'Clinical Trial'}">
			    <td valign="top" class="value">
				${fieldValue(bean: clinicalTrial, field: 'studyOwner')}
			    </td>
			</g:if>
			<g:else>
			    <td valign="top" class="value">
				${fieldValue(bean: clinicalTrial, field: 'primaryInvestigator')}
			    </td>
			</g:else>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Institution:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'institution')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Country:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'country')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Related Publication(s):</td>
			<td valign="top" class="value">
			    <g:if test="${clinicalTrial?.files}">
				<g:set var='fcount' value="${0}"/>
				<g:each in="${clinicalTrial.files}" var="file">
				    <g:if test="${file.content.type == 'Publication Web Link'}">
					<g:set var='fcount' value="${fcount++}"/>
					<g:if test="${fcount > 1}">,</g:if>
					<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
				    </g:if>
				</g:each>
			    </g:if>
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'description')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Access Type:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'accessType')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Phase:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'studyPhase')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Objective:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'design')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">BioMarker Type:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'bioMarkerType')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Compound:</td>
			<td valign="top" class="value">
			    <g:each in="${clinicalTrial.compounds}" var='compound'>
				<g:createFilterDetailsLink id="${compound?.id}" label="${compound?.genericName}" type='compound'/>
				<br/>
			    </g:each>
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Design Factors:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'overallDesign')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Number of Followed Subjects:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'numberOfPatients')}
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Organism(s):</td>
			<td valign="top" style="text-align: left;" class="value">
			    <g:each var='og' in="${clinicalTrial.organisms}">
				${og?.label?.encodeAsHTML()}<br/>
			    </g:each>
			</td>
		    </tr>
		    <tr class="prop">
			<td valign="top" class="name">Target/Pathways:</td>
			<td valign="top" class="value">
			    ${fieldValue(bean: clinicalTrial, field: 'target')}
			</td>
		    </tr>
		</tbody>
	    </table>
	</g:else>
    </body>
</html>

<table width="100%" style="padding-top: 30px; padding-bottom: 30px; table-layout: fixed;">
    <tbody>
	<tr>
            <td colspan="2" align="center">
		<div class="analysistitle" id="analysis_title">
                    Summary Statistics
		    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.analyzeSummary ?: '/transmartmanual/summary_statistics.html'}">
			<asset:image src="help/helpicon_white.jpg" alt="Help"/>
		    </a>
		</div>
            </td>
	</tr>
	<g:if test="${subsets[1].exists}">
	    <tr>
		<td colspan="2" align="center">
		    <div style='font-size:10pt;color:blue;'>Query Number: ${subsets[1].instance}</div>
		</td>
	    </tr>
	</g:if>
	<tr>
            <td width="50%" align="center" valign="top" style="padding: 10px">
		<br/><br/>
		${subsets[1].query}
		<br/><br/>
            </td>
            <td width="50%" align="center" valign="top" style="padding: 10px">
		<br/><br/>
		${subsets[2].query}
		<br/><br/>
            </td>
	</tr>
	<tr>
            <td colspan="2" align="center">
		<g:render template='/chart/patientCount' model="[subsets: subsets]"/>
            </td>
	</tr>
	<tr>
            <td colspan="2" align="center">
		<g:render template='/chart/valueComparison' model="[subsets: subsets, prefix: 'age']"/>
            </td>
	</tr>
	<tr>
            <g:render template='/chart/subsetCharts' model="[subsets: subsets, prefix: 'sex']"/>
	</tr>
	<tr>
            <g:render template='/chart/subsetCharts' model="[subsets: subsets, prefix: 'race']"/>
	</tr>
    </tbody>
</table>


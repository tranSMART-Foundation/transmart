<h3>Step ${wfstatus.currentStatusIndex} of ${wfstatus.jobStatusList.size()}</h3>
<table class="jobstatus">
    <g:each in="${wfstatus.jobStatusList}">
        <tr>
	    <td>
		${it.name}
		<g:if test="${it.isCompleted()}">&nbsp;&nbsp;
		    <asset:image src="green_check.png"/>
		</g:if>
		<g:elseif test="${it.isRunning()}">
                    <asset:image src="loading-balls.gif"/>
		</g:elseif>
	    </td>
	</tr>
    </g:each>
</table>

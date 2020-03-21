<h2>Spring &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.adminConfig ?: '/transmartmanual/admin.html#package-and-configuration'}" >
        <asset:image src="help/helpicon_white.jpg"
		     alt="Help" border="0" width="18pt" style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

<table id="configSpring"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Spring (${configParams.springParams.size()})"
	divPrefix="config_spring" status="closed" colSpan="${3}"/>

    <tbody id="config_spring_detail" style="display: block;">
        <tr>
	    <th>Parameter</th>
	    <th>Value</th>
	    <th width="100%">Description</th>
	</tr>

        <g:each in="${configParams.springParams}" var="paramData">
            <tr>
	        <td>${paramData.key}</td>
		<g:if test="${paramData.value.value != null}">
		    <td>${paramData.value.value}</td>
		</g:if>
		<g:elseif test="${paramData.value.default != null}">
		    <td>Default: ${paramData.value.default}</td>
		</g:elseif>
		<g:else>
		    <td>&nbsp;</td>
                </g:else>
		<td>${paramData.value.desc}</td>
	    </tr>
	</g:each>

    </tbody>
</table>

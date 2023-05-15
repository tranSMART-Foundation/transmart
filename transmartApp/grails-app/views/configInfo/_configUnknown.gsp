<h2>Unknown &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.adminConfig ?: '/transmartmanual/admin.html#package-and-configuration'}" >
        <asset:image src="help/helpicon_white.jpg"
		     alt="Help" border="0" width="18pt" style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

<p>
<b>Parameters not defined in the configuration status page. This should include all parameters actually used by tranSMART.</b>
<p>

<p>
<b>Any parameters appearing here may be mistyped names or newly developed options not yet described here.</b>
<p>

<table id="configUnknown"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Unknown (${configParams.unknownParams.size()})"
	divPrefix="config_unknown" status="open" colSpan="${3}"/>

    <tbody id="config_unknown_detail" style="display: block;">
        <tr>
	    <th>Parameter</th>
	    <th>Value</th>
	    <th>Description</th>
	</tr>

<%-- known parameters with defaults and descriptions b --%>
%{-- known parameters with defaults and descriptions c --}%

        <g:each in="${configParams.unknownParams}" var="paramData">
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

<h2>Ldap &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.adminConfig ?: '/transmartmanual/admin.html#package-and-configuration'}" >
        <asset:image src="help/helpicon_white.jpg"
	             alt="Help" border="0" width="18pt" style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

<table id="configLdap"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Ldap (${configParams.ldapParams.size()})"
	divPrefix="config_ldap" status="open" colSpan="${3}"/>

    <tbody id="config_ldap_detail" style="display: block;">
        <tr>
	    <th>Parameter</th>
	    <th>Value</th>
	    <th width="100%">Description</th>
	</tr>

        <g:each in="${configParams.ldapParams}" var="paramData">
            <tr>
	        <td>${paramData.key}</td>
		<g:if test="${paramData.value.value != null}">
		    <td nowrap>${paramData.value.value}</td>
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

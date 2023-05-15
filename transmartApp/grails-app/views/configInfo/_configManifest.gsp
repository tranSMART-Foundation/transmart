<h2>Manifest &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.org.transmartproject.helpUrls.adminConfig ?: '/transmartmanual/admin.html#package-and-configuration'}" >
        <asset:image src="help/helpicon_white.jpg" alt="Help" border="0" width="18pt" style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

%{--
	very long list of files in the Manifest, closed by default

	define status="closed" in tableHeaderToggle
	define style="display: none;" in tbody
--}%

<p>
<b>Very long list of all files included in the manifest of the unpacked tranSMART server.</b>
<p>

<p>
<b>This table is closed by default. Expand to see the full contents.</b>
<p>

<table id="configManifest"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Manifest (${configParams.manifestParams.size()})"
	divPrefix="config_manifest" status="closed" colSpan="${3}"/>

    <tbody id="config_manifest_detail" style="display: none;">
        <tr>
	    <th>Parameter</th>
	    <th>Value</th>
	    <th>Description</th>
	</tr>

<%-- known parameters with defaults and descriptions b --%>
%{-- known parameters with defaults and descriptions c --}%

        <g:each in="${configParams.manifestParams}" var="paramData">
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

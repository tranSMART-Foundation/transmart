<table class="categoryListCheckbox">
    <tbody>
	<tr>
            <td style="font-weight:bold;">
		By ${termDisplayName}
            </td>
	</tr>
    </tbody>

    <tbody>
	<g:each var="term" in="${termList}" status="iterator">
            <g:if test="${(iterator == grailsApplication.config.com.recomdata.solr.maxLinksDisplayed)}">
    </tbody>
    <tbody id="tbodyMoreLink${termName}">
        <tr>
            <td>
                <a href="#"
                   onClick="toggleMoreResults(document.getElementById('tbodyMoreLink${termName}'), document.getElementById('tbodyLessLink${termName}'), document.getElementById('tbodyHiddenResults${termName}'))">More [+]</a>
            </td>
        </tr>
    </tbody>
    <tbody id="tbodyHiddenResults${termName}" style="display:none;">
    </g:if>
    <tr>
        <td>
            <input type="checkBox" name="${termName}_${term.key}"
                   <g:if test="${JSONData[termName]?.contains(term.key)}">checked</g:if>
                   onClick="updateFilterList('${term.key.replace("'","\\'")}', this.checked, '${termName}');"/>
            <a href="#" class="categoryLinks"
               onClick="toggleMainCategorySelection('${term.key.replace("'","\\'")}', '${termName}')">${term.key} (${term.value})</a>
        </td>
    </tr>
    </g:each>
    </tbody>

    <tbody id="tbodyLessLink${termName}" style="display:none;">
	<tr>
            <td>
		<a href="#"
		   onClick="toggleMoreResults(document.getElementById('tbodyMoreLink${termName}'), document.getElementById('tbodyLessLink${termName}'), document.getElementById('tbodyHiddenResults${termName}'))">Less [-]</a>
            </td>
	</tr>
    </tbody>
</table>

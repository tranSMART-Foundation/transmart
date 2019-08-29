<table width="100%" style="margin-bottom: 30px">
    <tbody>
	<g:each in="${concepts}" var='concept'>
	    <tr>
		<td align="center">
		    <g:if test="${concept.value}">
			<hr style="margin-bottom: 30px"/>
			<div class="analysistitle" title="${concept.value.commons.conceptPath}">Analysis of ${concept.value.commons.conceptName}</div>
			<div class="analysissubtitle">${concept.value?.commons?.conceptTrimmed ?: ""}</div>
			<div style="margin-top: -15px; padding-bottom: 10px; font-size: 12px;">
			    ${concept.value?.commons?.testmessage}<br/>
			    <g:if test="${concept.value?.commons.pvalue != null}">
				<g:if test="${concept.value?.commons.tstat != null && concept.value?.commons.tstat != Double.NaN}">
				  <table class="stats_table">
				    <tr><td><span class="stats_title">p-value</span></td>
				      <td>${concept.value?.commons.pvalue}</td>
				    </tr>
				    <tr><td><span class="stats_title">t-statistic</span></td>
				      <td>${concept.value?.commons.tstat}</td>
				    </tr>
				  </table>
				</g:if>
				<g:elseif test="${concept.value?.commons.chisquare != null && concept.value?.commons.chisquare != Double.NaN}">
				  <table class="stats_table">
				    <tr><td><span class="stats_title">p-Value</span></td><td>${concept.value?.commons.pvalue}</td></tr>
                                    <tr><td><span class="stats_title">χ²</span></td><td>${concept.value?.commons.chisquare}</td></tr>
				  </table>
				</g:elseif>
				<g:else>
				    Variable arithmetically undefined <i>(NaN)</i>
				</g:else>
			    </g:if>
			</div>
			<g:render template="/chart/${concept.value.commons.type}Comparison" model="[subsets: concept.value]"/>
		    </g:if>
		</td>
	    </tr>
	</g:each>
    </tbody>
</table>

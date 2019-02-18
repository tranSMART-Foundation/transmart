<h1>Compilation context</h1>

<g:if test="${!warDeployed}">
    <p>
	<i>You are executing tranSMART from its source. This information will be available as soon as you deploy a compiled WAR archive.</i>
    </p>
</g:if>
<g:else>
    <table>
        <thead>
            <tr style="height: 30px;">
		<th style="vertical-align: middle; width: 250px;">Property</th>
		<th style="vertical-align: middle; width: 400px;">Value</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${buildInfoProperties}" status='i' var='prop'>
		<g:if test="${g.meta(name: prop)}">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 30px;">
			<td><g:message code="${prop}"/></td>
			<td><g:meta name="${prop}"/></td>
                    </tr>
		</g:if>
            </g:each>
        </tbody>
    </table>
</g:else>

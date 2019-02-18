<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>SecureObjectPath List</title>
    </head>

    <body>
	<div class="body">
	    <h1>SecureObjectPath List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='id' title='Id'/>
			    <g:sortableColumn property='conceptPath' title='Concept Path'/>
			    <th>Secure Object</th>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${sops}" status='i' var='sop'>
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action='show' id="${sop.id}">${fieldValue(bean: sop, field: 'id')}</g:link></td>
				<td>${fieldValue(bean: sop, field: 'conceptPath')}</td>
				<td>${fieldValue(bean: sop, field: 'secureObject')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${sopCount}"/>
	    </div>
	</div>
    </body>
</html>

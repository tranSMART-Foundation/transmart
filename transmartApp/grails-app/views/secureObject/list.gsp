<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>SecureObject List</title>
    </head>

    <body>
	<div class="body">
	    <h1>SecureObject List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='id' title='Id'/>
			    <g:sortableColumn property='bioDataId' title='Bio Data Id'/>
			    <g:sortableColumn property='dataType' title='Data Type'/>
			    <g:sortableColumn property='bioDataUniqueId' title='Bio Data Unique Id'/>
			    <g:sortableColumn property='displayName' title='Display Name'/>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${secureObjects}" status="i" var="so">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${so.id}">${fieldValue(bean: so, field: 'id')}</g:link></td>
				<td>${fieldValue(bean: so, field: 'bioDataId')}</td>
				<td>${fieldValue(bean: so, field: 'dataType')}</td>
				<td>${fieldValue(bean: so, field: 'bioDataUniqueId')}</td>
				<td>${fieldValue(bean: so, field: 'displayName')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${secureObjectCount}"/>
	    </div>
	</div>
    </body>
</html>

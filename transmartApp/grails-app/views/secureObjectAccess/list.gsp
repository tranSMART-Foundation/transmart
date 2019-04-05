<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="main"/>
	<title>SecureObjectAccess List</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="create" action="create">New SecureObjectAccess</g:link></span>
	</div>

	<div class="body">
	    <h1>SecureObjectAccess List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <th>Id</th>
			    <th>Principal</th>
			    <th>Access Level</th>
			    <th>Secure Object</th>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${soas}" status="i" var="soa">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${soa.id}">${fieldValue(bean: soa, field: 'id')}</g:link></td>
				<td>${fieldValue(bean: soa, field: 'id')}</td>
				<td>${fieldValue(bean: soa, field: 'principal')}</td>
				<td>${fieldValue(bean: soa, field: 'accessLevel')}</td>
				<td>${fieldValue(bean: soa, field: 'secureObject')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${soaCount}"/>
	    </div>
	</div>
    </body>
</html>

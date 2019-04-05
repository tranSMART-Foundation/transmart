<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Access Control List</title>
    </head>

    <body>
	<div id="header-div" class="header-div">
	    <g:render template='/layouts/commonheader' model="[app: 'authUserSecureAccess']"/>
	</div>
	<div class="body">
	    <h1>AuthUserSecureAccess List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='id' title='Id'/>
			    <g:sortableColumn property='username' title='User Name'/>
			    <g:sortableColumn property='accessLevelName' title='Access Level'/>
			    <g:sortableColumn property='displayName' title='Secure Object'/>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${instances}" status="i" var="ausa">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${ausa.id}">${fieldValue(bean: ausa, field: 'id')}</g:link></td>
				<td>${fieldValue(bean: ausa, field: 'authUser')}</td>
				<td>${fieldValue(bean: ausa, field: 'accessLevel.accessLevelName')}</td>
				<td>${fieldValue(bean: ausa, field: 'secureObject.displayName')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${totalCount}"/>
	    </div>
	</div>
    </body>
</html>

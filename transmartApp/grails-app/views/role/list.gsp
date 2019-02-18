<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Role List</title>
    </head>

    <body>
	<div class="body">
	    <h1>Role List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='id' title='ID'/>
			    <g:sortableColumn property='authority' title='Role Name'/>
			    <g:sortableColumn property='description' title='Description'/>
			    <th>&nbsp;</th>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${roles}" status="i" var="role">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>${role.id}</td>
				<td>${role.authority?.encodeAsHTML()}</td>
				<td>${role.description?.encodeAsHTML()}</td>
				<td class="actionButtons">
				    <span class="actionButton">
					<g:link action="show" id="${role.id}">Show</g:link>
				    </span>
				</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${roleCount}"/>
	    </div>
	</div>
    </body>
</html>

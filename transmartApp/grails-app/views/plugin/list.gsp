<%@ page import="com.recomdata.transmart.plugin.Plugin" %>
<html>
    <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Plugin List</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="create" action="create">New Plugin</g:link></span>
	</div>

	<div class="body">
	    <h1>Plugin List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='id' title='Id'/>
			    <g:sortableColumn property='name' title='Name'/>
			    <g:sortableColumn property='pluginName' title='Plugin Name'/>
			    <g:sortableColumn property='hasModules' title='Has Modules'/>
			    <g:sortableColumn property='hasForm' title='Has Form'/>
			    <g:sortableColumn property='active' title='Active'/>
			    <g:sortableColumn property='defaultLink' title='Default Link'/>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${plugins}" status="i" var="plugin">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${plugin.id}">${fieldValue(bean: plugin, field: 'id')}</g:link></td>
				<td>${fieldValue(bean: plugin, field: 'name')}</td>
				<td>${fieldValue(bean: plugin, field: 'pluginName')}</td>
				<td><g:checkBox name="hasModules" value="${plugin?.hasModules}" disabled="true"/></td>
				<td><g:checkBox name="hasForm" value="${plugin?.hasForm}" disabled="true"/></td>
				<td><g:checkBox name="active" value="${plugin?.active}" disabled="true"/></td>
				<td>${fieldValue(bean: plugin, field: 'defaultLink')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${pluginCount}"/>
	    </div>
	</div>
    </body>
</html>

<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="main"/>
	<title>PluginModule List</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="create" action="create">New PluginModule</g:link></span>
	</div>

	<div class="body">
	    <h1>PluginModule List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr>
			    <g:sortableColumn property='name' title='Name'/>
			    <g:sortableColumn property='active' title='Active'/>
			    <g:sortableColumn property='category' title='Category'/>
			    <g:sortableColumn property='hasForm' title='Has Form'/>
			    <g:sortableColumn property='formLink' title='Form Link'/>
			    <g:sortableColumn property='formPage' title='Form Page'/>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${pms}" status='i' var='pm'>
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${pm.id}" style="font-size:12px">${fieldValue(bean: pm, field: 'name')}</g:link></td>
				<td align="center"><g:checkBox name="" value="${pm?.active}" disabled="true"/></td>
				<td>${fieldValue(bean: pm, field: 'category')}</td>
				<td align="center"><g:checkBox name="" value="${pm?.hasForm}" disabled="true"/></td>
				<td>${fieldValue(bean: pm, field: 'formLink')}</td>
				<td>${fieldValue(bean: pm, field: 'formPage')}</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${pmCount}"/>
	    </div>
	</div>
    </body>
</html>

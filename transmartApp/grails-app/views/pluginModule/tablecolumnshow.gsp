<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Show PluginModule</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class='list' action='list'>PluginModule List</g:link></span>
	    <span class="menuButton"><g:link class='create' action='create'>New PluginModule</g:link></span>
	</div>

	<div class="body">
	    <h1>Show PluginModule</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>

			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Name:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'name')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Active:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'active')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Has Form:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'hasForm')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Form Link:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'formLink')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Form Page:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'formPage')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Params:</td>
			    <td valign="top" class="value">${fieldValue(bean: pm, field: 'params')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Plugin:</td>
			    <td valign="top" class="value">
				<g:link controller='plugin' action='show' id="${pm?.pluginId}">${pm?.plugin?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${pm?.id}"/>
		    <span class="button"><g:actionSubmit class='edit' value='Edit'/></span>
		    <span class="button"><g:actionSubmit class='delete' onclick="return confirm('Are you sure?');" value='Delete'/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

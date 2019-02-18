<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Edit Plugin</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="list" action="list">Plugin List</g:link></span>
	    <span class="menuButton"><g:link class="create" action="create">New Plugin</g:link></span>
	</div>

	<div class="body">
	    <h1>Edit Plugin</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${plugin}">
		<div class="errors">
		    <g:renderErrors bean="${plugin}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${plugin?.id}"/>
		<input type="hidden" name="version" value="${plugin?.version}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="name">Name:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'name', 'errors')}">
				    <input type="text" id="name" name="name"
					   value="${fieldValue(bean: plugin, field: 'name')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="pluginName">Plugin Name:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'pluginName', 'errors')}">
				    <input type="text" id="pluginName" name="pluginName"
					   value="${fieldValue(bean: plugin, field: 'pluginName')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="hasModules">Has Modules:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'hasModules', 'errors')}">
				    <g:checkBox name="hasModules" value="${plugin?.hasModules}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="hasForm">Has Form:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'hasForm', 'errors')}">
				    <g:checkBox name="hasForm" value="${plugin?.hasForm}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="active">Active:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'active', 'errors')}">
				    <g:checkBox name="active" value="${plugin?.active}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="defaultLink">Default Link:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'defaultLink', 'errors')}">
				    <input type="text" id="defaultLink" name="defaultLink"
					   value="${fieldValue(bean: plugin, field: 'defaultLink')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="formLink">Form Link:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'formLink', 'errors')}">
				    <input type="text" id="formLink" name="formLink"
					   value="${fieldValue(bean: plugin, field: 'formLink')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="formPage">Form Page:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'formPage', 'errors')}">
				    <input type="text" id="formPage" name="formPage"
					   value="${fieldValue(bean: plugin, field: 'formPage')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="modules">Modules:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: plugin, field: 'modules', 'errors')}">
				    <ul>
					<g:each var="m" in="${plugin?.modules ?}">
					    <li><g:link controller="pluginModule" action="show" id="${m.id}">${m?.name}</g:link></li>
					</g:each>
				    </ul>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><g:actionSubmit class="save" value="Update"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		    <span class="menuButton"><g:link class="create" controller="pluginModule"
						     params="['plugin.id': plugin?.id]"
						     action="create" style="font-size:12px">Add PluginModule</g:link></span>
		</div>
	    </g:form>
	</div>
    </body>
</html>

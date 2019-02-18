<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Create PluginModule</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="list" action="list">PluginModule List</g:link></span>
	</div>

	<div class="body">
	    <h1>Create PluginModule</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${pm}">
		<div class="errors">
		    <g:renderErrors bean="${pm}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form action='save'>
		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="name">Name:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: pm, field: 'name', 'errors')}">
				    <input type="text" id="name" name="name" value="${fieldValue(bean: pm, field: 'name')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="moduleName">Name:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: pm, field: 'moduleName', 'errors')}">
				    <input type="text" id="moduleName" name="moduleName" value="${fieldValue(bean: pm, field: 'moduleName')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="active">Active:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: pm, field: 'active', 'errors')}">
				    <g:checkBox name="active" value="${pm?.active}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="hasForm">Has Form:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: pm, field: 'hasForm', 'errors')}">
				    <g:checkBox name="hasForm" value="${pm?.hasForm}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="category">Category:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: pm, field: 'category', 'errors')}">
				    <g:select name='category' from="${categories}" value="${fieldValue(bean: pm, field: 'category')}"
					      optionKey='key' optionValue='value'/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="formLink">Form Link:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: pm, field: 'formLink', 'errors')}">
				    <input type="text" id="formLink" name="formLink" value="${fieldValue(bean: pm, field: 'formLink')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="formPage">Form Page:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: pm, field: 'formPage', 'errors')}">
				    <input type="text" id="formPage" name="formPage" value="${fieldValue(bean: pm, field: 'formPage')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="paramsStr">Params:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: pm, field: 'params', 'errors')}">
				    <textarea id="paramsStr" name="paramsStr">${fieldValue(bean: pm, field: 'params')}</textarea>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="plugin.id">Plugin:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: pm, field: 'plugin', 'errors')}">
				    <g:select optionKey='id' optionValue='name' from="${plugins}" name='plugin.id' value="${pm?.pluginId}"/>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><input class="save" type="submit" value="Create"/></span>
		</div>
	    </g:form>
	</div>
    </body>
</html>

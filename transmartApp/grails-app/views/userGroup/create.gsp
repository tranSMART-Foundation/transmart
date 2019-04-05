<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Create User Group</title>
    </head>

    <body>

	<div class="body">
	    <h1>Create User Group</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${ug}">
		<div class="errors">
		    <g:renderErrors bean="${ug}" as='list'/>
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
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'name', 'errors')}">
				    <input type="text" id="name" name="name" value="${fieldValue(bean: ug, field: 'name')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="description">Description:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: ug, field: 'description', 'errors')}">
				    <textarea rows="5" cols="40" name="description" id="description">${fieldValue(bean: ug, field: 'description')}</textarea>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="enabled">Enabled:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'enabled', 'errors')}">
				    <g:checkBox name='enabled' value="${ug?.enabled}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="uniqueId">Unique Id:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'uniqueId', 'errors')}">
				    <input type="text" id="uniqueId" name="uniqueId" value="${fieldValue(bean: ug, field: 'uniqueId')}"/>
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

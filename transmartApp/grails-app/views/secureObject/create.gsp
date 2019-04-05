<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Create SecureObject</title>
    </head>

    <body>
	<div class="body">
	    <h1>Create SecureObject</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${so}">
		<div class="errors">
		    <g:renderErrors bean="${so}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form action='save'>
		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="bioDataId">Bio Data Id:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'bioDataId', 'errors')}">
				    <input type="text" id="bioDataId" name="bioDataId"
					   value="${fieldValue(bean: so, field: 'bioDataId')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="dataType">Data Type:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'dataType', 'errors')}">
				    <textarea rows="5" cols="40" name="dataType" id="dataType">${fieldValue(bean: so, field: 'dataType')}</textarea>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="bioDataUniqueId">Bio Data Unique Id:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'bioDataUniqueId', 'errors')}">
				    <input type="text" id="bioDataUniqueId" name="bioDataUniqueId"
					   value="${fieldValue(bean: so, field: 'bioDataUniqueId')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="displayName">Display Name:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'displayName', 'errors')}">
				    <input type="text" id="displayName" name="displayName"
					   value="${fieldValue(bean: so, field: 'displayName')}"/>
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

<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Edit AuthUserSecureAccess</title>
    </head>

    <body>
	<div class="body">
	    <h1>Edit AuthUserSecureAccess</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${ausa}">
		<div class="errors">
		    <g:renderErrors bean="${ausa}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${ausa?.id}"/>
		<input type="hidden" name="version" value="${ausa?.version}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="authUser.id">Auth User:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'authUser', 'errors')}">
				    <g:select optionKey='id' from="${authUsers}" name='authUser.id'
					      value="${ausa?.authUserId}" noSelection="['null': '']"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="accessLevel.id">Access Level:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'accessLevel', 'errors')}">
				    <g:select optionKey='id' optionValue='accessLevelName' from="${accessLevels}"
					      name='accessLevel.id' value="${ausa?.accessLevelId}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="secureObject.id">Secure Object:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'secureObject', 'errors')}">
				    <g:select optionKey='id' optionValue='displayName' from="${secureObjects}"
					      name='secureObject.id' value="${ausa?.secureObjectId}"/>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><g:actionSubmit class="save" value="Update"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</div>
	    </g:form>
	</div>
    </body>
</html>

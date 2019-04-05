<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Show AuthUserSecureAccess</title>
    </head>

    <body>
	<div class="body">
	    <h1>Show Access Control</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>

			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: ausa, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Auth User:</td>
			    <td valign="top" class="value">
				<g:link controller='authUser' action='show'
					id="${ausa?.authUserId}">${ausa?.authUser?.username?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Access Level:</td>
			    <td valign="top" class="value">
				<g:link controller='secureAccessLevel' action='show'
					id="${ausa?.accessLevelId}">${ausa?.accessLevel?.accessLevelName?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Secure Object:</td>
			    <td valign="top" class="value">
				<g:link controller='secureObject' action='show'
					id="${ausa?.secureObjectId}">${ausa?.secureObject?.displayName?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${ausa?.id}"/>
		    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

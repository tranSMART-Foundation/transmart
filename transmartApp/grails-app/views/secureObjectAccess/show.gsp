<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Show SecureObjectAccess</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="list" action="list">SecureObjectAccess List</g:link></span>
	    <span class="menuButton"><g:link class="create" action="create">New SecureObjectAccess</g:link></span>
	</div>

	<div class="body">
	    <h1>Show SecureObjectAccess</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>

			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: soa, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Principal:</td>
			    <td valign="top" class="value">
				<g:link controller='principal' action='show' id="${soa?.principalId}">${soa?.principal?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Access Level:</td>
			    <td valign="top" class="value">
				<g:link controller='secureAccessLevel' action='show' id="${soa?.accessLevelId}">${soa?.accessLevel?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Secure Object:</td>
			    <td valign="top" class="value">
				<g:link controller='secureObject' action='show' id="${soa?.secureObjectId}">${soa?.secureObject?.encodeAsHTML()}</g:link>
			    </td>
			</tr>

		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${soa?.id}"/>
		    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

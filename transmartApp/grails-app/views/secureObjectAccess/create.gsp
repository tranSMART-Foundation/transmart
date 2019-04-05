<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Create SecureObjectAccess</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="list" action="list">SecureObjectAccess List</g:link></span>
	</div>

	<div class="body">
	    <h1>Create SecureObjectAccess</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${soa}">
		<div class="errors">
		    <g:renderErrors bean="${soa}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form action='save'>
		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="principal.id">Principal:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: soa, field: 'principal', 'errors')}">
				    <g:select optionKey='id' from="${principals}" name='principal.id'
					      value="${soa?.principalId}" noSelection="['null': '']"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="accessLevel.id">Access Level:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: soa, field: 'accessLevel', 'errors')}">
				    <g:select optionKey='id' name='accessLevel.id' from="${secureAccessLevels}" value="${soa?.accessLevelId}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="secureObject.id">Secure Object:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: soa, field: 'secureObject', 'errors')}">
				    <g:select optionKey='id' name='secureObject.id' from="${secureObjects}" value="${soa?.secureObjectId}"/>
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

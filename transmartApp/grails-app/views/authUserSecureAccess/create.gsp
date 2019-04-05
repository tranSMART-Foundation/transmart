<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Grant New Access Control</title>
    </head>

    <body>
	<div class="body">
	    <h1>Grant New Access Control</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${ausa}">
		<div class="errors">
		    <g:renderErrors bean="${ausa}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form action='save'>
		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="authUser.id">Auth User:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'authUser', 'errors')}">
				    <g:select optionKey='id' from="${authUsers}" name='authUser.id'
					      value="${ausa?.authUserId}" noSelection="['null': '']"
					      onchange="${remoteFunction(action: 'listAccessLevel', update: 'accessLevelList', params: '\'id=\'+this.value')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="accessLevel.id">Access Level:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'accessLevel', 'errors')}">
				    <g:select id="accessLevelList" optionKey='id' optionValue='accessLevelName'  name='accessLevel.id'
					      from="${secureAccessLevels}" value="${ausa?.accessLevelId}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="secureObject.id">Secure Object:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ausa, field: 'secureObject', 'errors')}">
				    <g:select optionKey='id' optionValue='displayName' name='secureObject.id'
					      from="${secureObjects}" value="${ausa?.secureObjectId}"/>
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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Edit AuthUser</title>
    </head>

    <body>
	<div class="body">
	    <h1>Edit AuthUser</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${person}">
		<div class="errors">
		    <g:renderErrors bean="${person}" as="list"/>
		</div>
	    </g:hasErrors>

	    <div class="prop">
		<span class="name">WWID:</span>
		<span class="value">${person.id}</span>
	    </div>

	    <g:form controller='authUser' action='update'>
		<input type="hidden" name="id" value="${person.id}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name"><label for="userRealName">Full Name:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'userRealName', 'errors')}">
				    <g:textField name='userRealName' value="${person.userRealName?.encodeAsHTML()}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label for="email">Email:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'email', 'errors')}">
				    <g:textField name='email' value="${person?.email?.encodeAsHTML()}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label>User Level:</label></td>
				<td valign="top" class="value">
				    <g:select name='userLevel' value="${userLevel}" optionValue='description' from="${userLevels}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label>Auth0 Provider:</label></td>
				<td valign="top" class="value">
				    <g:select name='auth0Provider' value="${auth0Provider}" from="${auth0Providers}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label for="uniqueId">Provider ID:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'uniqueId', 'errors')}">
				    <g:textField name='uniqueId' value="${uniqueId}"/>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><g:submitButton name='update' value='Update' class='save' /></span>
		</div>
	    </g:form>

	</div>
    </body>
</html>

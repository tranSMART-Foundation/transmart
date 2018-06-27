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
						<input type="text" id="userRealName" name="userRealName" value="${person.userRealName?.encodeAsHTML()}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label for="enabled">Enabled:</label></td>
					<td valign="top" class="value ${hasErrors(bean: person, field: 'enabled', 'errors')}">
						<g:checkBox name="enabled" value="${person.enabled}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label for="email">Email:</label></td>
					<td valign="top" class="value ${hasErrors(bean: person, field: 'email', 'errors')}">
						<input type="text" id="email" name="email" value="${person?.email?.encodeAsHTML()}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label for="affiliation">Affiliation:</label></td>
					<td valign="top" class="value">
						<g:textField name='affiliation' value="${(affiliation ?: 'unknown')?.encodeAsHTML()}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label for="connection">Connection:</label></td>
					<td valign="top" class="value">
						<g:textField name='connection' value="${connection?.encodeAsHTML()}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label>User Level:</label></td>
					<td valign="top" class="value">
						<g:select name='userLevel' value="${userLevel}" optionValue='description'
						          from="${org.transmart.plugin.custom.UserLevel.values()}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label>Auth0 Provider:</label></td>
					<td valign="top" class="value">
						<g:select name='auth0Provider' value="${auth0Provider}" from="${auth0Providers}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name"><label for="providerId">Provider ID / Username:</label></td>
					<td valign="top" class="value">
						<g:textField name='providerId' value="${providerId}"/>
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

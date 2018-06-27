<html>
<head>
	<meta name="layout" content="admin"/>
	<title>User</title>
</head>

<body>
<div class="body">
	<h1>User</h1>
	<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
	</g:if>
	<div class="dialog">
		<table>
			<tbody>

			<tr class="prop">
				<td valign="top" class="name">ID:</td>
				<td valign="top" class="value">${person.id}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Username:</td>
				<td valign="top" class="value">${person.username?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Full Name:</td>
				<td valign="top" class="value">${person.fullName?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Email:</td>
				<td valign="top" class="value">${person.email?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Affiliation:</td>
				<td valign="top" class="value">${person.institution?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Connection:</td>
				<td valign="top" class="value">${person.connection?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Last Updated:</td>
				<td valign="top" class="value">${person.lastUpdated?.encodeAsHTML()}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Access Level:</td>
				<td valign="top" class="value">${person.level}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Show Email:</td>
				<td valign="top" class="value">${person.emailShow}</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Enabled:</td>
				<td valign="top" class="value">${person.enabled}</td>
			</tr>


			<tr class="prop">
				<td valign="top" class="name">Roles:</td>
				<td valign="top" class="value">
					<ul>
						<g:each in="${roleNames}" var='name'>
							<li>${name}</li>
						</g:each>
					</ul>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Groups:</td>
				<td valign="top" class="value">
					<ul>
					<g:each in="${person.groups}" var='group'>
						<li><g:link controller="userGroup" action="show" id="${group.id}">${group.name}</g:link></li>
					</g:each>
					</ul>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Studies Assigned:</td>
				<td valign="top" class="value">
					<ul>
						<g:each in="${soas}" var='soa'>
							<li>${soa.objectAccessName}</li>
						</g:each>
					</ul>
				</td>
			</tr>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Studies with Access(via groups):</td>
				<td valign="top" class="value">
					<ul>
						<g:each in="${ausas}" var='soa'>
							<li><g:link controller="secureObject" action="show" id="${soa.secureObjectId}">${soa.objectAccessName}</g:link></li>
						</g:each>
					</ul>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name">Force user to change the password:</td>
				<td valign="top" class="value">${person.changePassword ? 'Yes' : 'No'}</td>
			</tr>
			</tbody>
		</table>
	</div>

	<div class="buttons">
		<g:form controller='authUser' action='edit' id="${person.id}" style="float:left;">
			<span class="button"><g:submitButton name='edit' value='Edit' class='edit' /></span>
		</g:form>
		<g:form controller='authUser' action='delete' style="float:left;">
			<g:hiddenField name='id' value="${person.id}"/>
			<span class="button"><g:submitButton name='delete' value='Delete' class='delete' onclick="return confirm('Are you sure?');"  /></span>
		</g:form>
	</div>

</div>
</body>
</html>

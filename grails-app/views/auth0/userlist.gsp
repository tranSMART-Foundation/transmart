<%@page import='org.transmart.plugin.custom.UserLevel' %> %{--TODO why isn't the import working?--}%
<html>
<head>
	<meta name="layout" content="admin"/>
	<title>AuthUser List</title>
</head>
<body>
<div class="body">
	<h1>AuthUser List</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="list">
		<table>
			<thead>
			<tr>
				<th>WWID</th>
				<th>Username</th>
				<th>Full Name</th>
				<th>Email</th>
				<th>Connection</th>
				<th>Unique ID</th>
				<th>Access Level</th>
				<th>Last Updated</th>
				<th>&nbsp;</th>
			</tr>
			</thead>
			<tbody>
			<g:each in="${users}" status='i' var='person'>
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${person.id}</td>
					<td>${person.username.encodeAsHTML()}</td>
					<td>${person.fullName?.encodeAsHTML()}</td>
					<td>${person.email?.encodeAsHTML()}</td>
					<td>${person.connection.encodeAsHTML()}</td>
					<td>${person.uniqueId?.encodeAsHTML()}</td>
					<td>${person.level.encodeAsHTML()}</td>
					<td>${person.lastUpdated.toString().encodeAsHTML()}</td>
					<td class="actionButtons">
						<span class="actionButton">
							<g:link controller='authUser' action='show' id="${person.id}">Show</g:link>
						</span>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>

	<div class="paginateButtons">
		<g:paginate total="${users.size()}"/>
	</div>

</div>
</body>
</html>

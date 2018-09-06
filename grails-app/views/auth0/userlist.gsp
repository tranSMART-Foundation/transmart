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
				<g:sortableColumn property='id' title='WWID'/>
				<g:sortableColumn property='username' title='Username'/>
				<g:sortableColumn property='userRealName' title='Full Name'/>
				<g:sortableColumn property='email' title='Email'/>
				<th>Connection</th>
				<g:sortableColumn property='uniqueId' title='Unique ID'/>
				<th>Access Level</th>
				<g:sortableColumn property='lastUpdated' title='Last Updated'/>
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
					<td>${person.lastUpdated.format('yyyy-MM-dd').encodeAsHTML()}</td>
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
		<g:paginate total="${userCount}"/>
	</div>
</div>
</body>
</html>

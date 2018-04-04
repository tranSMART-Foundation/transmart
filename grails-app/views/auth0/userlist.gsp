<%@page import='org.transmart.plugin.custom.UserLevel' %> %{--TODO why isn't the import working?--}%
<html>
<head>
<meta name="layout" content="bootstrap"/>
<title>User List</title>
<link rel="stylesheet" href="https://cdn.datatables.net/1.10.10/css/dataTables.bootstrap.min.css"/>
<style>
.customized {
	width: 120px !important;
}
</style>
</head>
<body>
<g:render template='/layouts/navbar_admin' />
<div class="container well" style="opacity: .85; color: black;">
	<g:if test="${flash.message}">
	<div class="alert alert-info alert-dismissable">
	<a class="panel-close close" data-dismiss="alert">×</a> <i class="fa fa-coffee"></i>
		${flash.message}
	</div>
	</g:if>
	<h2>User List</h2>
	<div class="row">
		<div class="col-lg-12">
			<g:each in="${alerts}" var='alert'>
			<div class="alert alert-danger alert-dismissable">
				<a class="panel-close close" data-dismiss="alert">×</a> <i class="glyphicon glyphicon-link"></i>
				${alert}
			</div>
			</g:each>
			<g:if test="${!userSignupEnabled}">
			<a class="btn btn-primary customized" aria-haspopup="true" data-toggle="modal" data-target="#addUsersModal">Add users</a>
			</g:if>
			<table id="tblUserList" class="table">
				<thead>
				<tr>
					<th>Name</th>
					<th>Affiliation</th>
					<th>Connection</th>
					<th>Last Updated</th>
					<th>Access Level</th>
					<th>More</th>
				</tr>
				</thead>
				<tbody>
					<g:each in="${users}" var='user'>
					<tr>
						<td>
							<b>${user.firstName} ${user.lastName}</b>
							<br/>
							<small>${user.email}</small>
						</td>
						<td>${user.institution}</td>
						<td>${user.connection}</td>
						<td>${user.lastUpdated}</td>
						<td>
							<div class="btn-group">
								<g:set var='levelClass' value='primary' />
								<g:set var='levelDesc' value="Level${user.level.level}" />
								<g:if test="${user.level == org.transmart.plugin.custom.UserLevel.UNREGISTERED}">
									<g:set var='levelClass' value='danger' />
									<g:set var='levelDesc' value='Unregistered' />
								</g:if>
								<g:if test="${user.level == org.transmart.plugin.custom.UserLevel.ZERO}">
									<g:set var='levelClass' value='danger' />
								</g:if>
								<g:if test="${user.level == org.transmart.plugin.custom.UserLevel.ONE}">
									<g:set var='levelClass' value='info' />
								</g:if>
								<g:if test="${user.level == org.transmart.plugin.custom.UserLevel.ADMIN}">
									<g:set var='levelClass' value='warning' />
									<g:set var='levelDesc' value='Admin' />
								</g:if>
								<a class="btn btn-${levelClass} dropdown-toggle customized" data-toggle="dropdown"
								   aria-haspopup="true" aria-expanded="false">
									${levelDesc} <span class="caret"></span>
								</a>
								<ul class="dropdown-menu">
									<li><g:link action='userlist' params="[state: org.transmart.plugin.custom.UserLevel.ZERO]" id="${user.id}">Level0</g:link></li>
									<li><g:link action='userlist' params="[state: org.transmart.plugin.custom.UserLevel.ONE]" id="${user.id}">Level1</g:link></li>
									<li><g:link action='userlist' params="[state: org.transmart.plugin.custom.UserLevel.TWO]" id="${user.id}">Level2</g:link></li>
									<li role="separator" class="divider"></li>
									<li><g:link action='userlist' params="[state: org.transmart.plugin.custom.UserLevel.ADMIN]" id="${user.id}">Admin</g:link></li>
								</ul>
							</div>
						</td>
						<td>
							<g:link controller='admin' action='profileview' id="${user.id}" class='btn'
							        data-toggle='modal' data-target="#userprofile${user.id}">
								<span class="glyphicon glyphicon-option-horizontal" aria-hidden="true"></span>
							</g:link>
						</td>
					</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
</div>
<g:each in="${users}" var='user'>
<div class="modal fade" id="userprofile${user.id}" tabindex="-1" role="dialog" aria-labelledby="userprofile${user.id}">
	<div class="modal-dialog" role="document">
		<div class="modal-content"></div>
	</div>
</div>
</g:each>
<div class="modal fade" id="addUsersModal" tabindex="-1" role="dialog" aria-labelledby="addUsersModal">
	<div class="modal-dialog" role="document">
		<g:set var="level" value=''></g:set>
		<div class="modal-content">
			<g:form controller='admin' action='addUsers'>
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title" id="myModalLabel2">
						<strong>Add users</strong>
					</h4>
				</div>
				<div class="modal-body" style="height: 50%">
					<textArea name="userList" placeholder="Enter usernames. One username per line."
					          style="min-width: 100%; height: 200px;" required></textArea>
					<div id="messageId"></div>
					<select name="accessLevel" id="userLevel" class="btn btn-info dropdown-toggle customized" required>
						<option value="" disabled selected>Access level</option>
						<option value="${org.transmart.plugin.custom.UserLevel.ONE}">${org.transmart.plugin.custom.UserLevel.ONE.description}</option>
						<option value="${org.transmart.plugin.custom.UserLevel.TWO}">${org.transmart.plugin.custom.UserLevel.TWO.description}</option>
					</select>
				</div>
				<div class="modal-footer">
					<button type="submit" class="btn btn-primary customized">Add users</button>
				</div>
			</g:form>
		</div>
	</div>
</div>
<content tag="javascript"><script>
	$(document).ready(function () {
		$('#tblUserList').DataTable({
			"bLengthChange": false,
			"info": false
		});
	});
</script></content>
</body>
</html>

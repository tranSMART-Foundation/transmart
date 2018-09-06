<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
	<meta name="layout" content="none"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="format-detection" content="telephone=no">
	<title>User Registration</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"/>
	<g:render template='/auth0/auth_email_css' />
</head>
<body bgcolor="#F0F0F0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table border="0" width="100%" height="100%" cellpadding="0" cellspacing="0" bgcolor="#F0F0F0">
	<tr>
		<td align="center" valign="top" bgcolor="#F0F0F0" style="background-color: #F0F0F0;"><br />
			<table id="table600" border="0" width="600" cellpadding="0" cellspacing="0" class="container">
				<tr>
					<td id="tdLogo" class="container-padding content" align="left">
						<img src="${emailLogo}" height="50px"/><br/>
						<hr/>
						<div id="divTitle" class="title">New User Registration</div>
						<br/>
						<div id="divBodyText" class="body-text">
							The following user has registered with the <strong>${instanceName}</strong> application.
							<br/><br/>
							<table class="table" width="90%" cellpadding="5px">
								<tbody>
								<tr>
									<th>Username/Email</th>
									<td>${person.email}</td>
								</tr>
								<tr>
									<th>Name</th>
									<td>${person.firstname} ${person.lastname}</td>
								</tr>
								<tr>
									<th>Phone</th>
									<td>${person.phone}</td>
								</tr>
								<tr>
									<th>Affiliation</th>
									<td>${person.institution}</td>
								</tr>
								<tr>
									<th>User Type</th>
									<td>${person.usertype}</td>
								</tr>
								<tr>
									<th>Professional Title</th>
									<td>${person.title}</td>
								</tr>
								<tr>
									<th>Degree</th>
									<td>${person.degree}</td>
								</tr>
								<tr>
									<th>Is this request related to a funded grant?</th>
									<td>${person.is_funded_grant == 'on' ? 'Yes' : 'No'}</td>
								</tr>
								<tr>
									<th>Will the data be used to support a grant proposal?</th>
									<td>${person.is_grant_proposal == 'on' ? 'Yes' : 'No'}</td>
								</tr>
								<tr>
									<th>Will the data be used for teaching a class, or for personal use?</th>
									<td>${person.is_personal_use == 'on' ? 'Yes' : 'No'}</td>
								</tr>
								<tr>
									<th>Disase Interest</th>
									<td>${person.disease}</td>
								</tr>
								<tr>
									<th>Research Goal</th>
									<td>${person.about}</td>
								</tr>
								<tr>
									<td colspan="2" align="center">...</td>
								</tr>
								<tr>
									<th>Server Name</th>
									<td>${appUrl}</td>
								</tr>
								</tbody>
							</table>
							<br/>
							<br/>
						</div>
					</td>
				</tr>
				<tr>
					<td id="tdFooter" class="container-padding footer-text" align="left">
						<g:render template='/notification/email_signature' />
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<meta name="layout" content="none" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="format-detection" content="telephone=no">
<title>User Registration</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" />
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
						<hr />
						<div id="divTitle" class="title">Access Granted</div>
						<br />
						<div id="divBodyText" class="body-text">
							The administrator has granted you <strong>${levelName}</strong> access for <b>${instanceName}</b> data.<br /><br />
							<transmart:ifLevelOne user="${user}">${level1EmailMessage}</transmart:ifLevelOne>
							<transmart:ifLevelTwo user="${user}">${level2EmailMessage}</transmart:ifLevelTwo>
							<transmart:ifLevelAdmin user="${user}">${adminEmailMessage}</transmart:ifLevelAdmin>
							<br /><br />
							To learn more about the application, please visit our <a href="${userGuideUrl}">User's Guide</a><br />
							<br />
							<div class="text-center">
								<p>Now you can <br /><br />
								<a href="${appUrl}" class="btn btn-primary">Login to i2b2/tranSMART</a><br /><br />
								or contact the administrator <br /><br />
								<a class="btn btn-default" href="mailto:${supportEmail}">
									<span class="glyphicon glyphicon-envelope" aria-hidden="true"></span> Support E-mail</a>
								</p>
							</div>
							<br />
							Thank you!<br /><br />
						</div>
					</td>
				</tr>
				<tr>
					<td id="tdFooter" class="container-padding footer-text" align="left">
						<g:render template='/notification/email_signature' />
					</td>
				</tr>
			</table>
			<br /><br />
		</td>
	</tr>
</table>
</body>
</html>

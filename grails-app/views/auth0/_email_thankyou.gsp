<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="format-detection" content="telephone=no">
	<title>User Registration</title>
	<g:render template='/auth0/auth_email_css' />
</head>
<body bgcolor="#F0F0F0" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table border="0" width="100%" height="100%" cellpadding="0" cellspacing="0" bgcolor="#F0F0F0">
	<tr>
		<td align="center" valign="top" bgcolor="#F0F0F0" style="background-color: #F0F0F0;"><br/>
			<table id="table600" border="0" width="600" cellpadding="0" cellspacing="0" class="container">
				<tr>
					<td id="tdLogo" class="container-padding content" align="left">
						<img src="${emailLogo}" height="50px"/> <br/>
						<div id="divTitle" class="title">User Registration</div>
						<br />
						<div id="divBodyText" class="body-text">
							Thank you for registering for the <strong>${instanceName}</strong> application.<br/>
							<br/>
							The following information has been recorded:<br/>
							<br/>
							<center>
								<table width="70%" cellpadding="5px">
									<tbody>
									<tr>
										<th width="25%" align="left">Name</th>
										<td>${firstName} ${lastName}</td>
									</tr>
									<tr>
										<th align="left">Email</th>
										<td>${email}</td>
									</tr>
									</tbody>
								</table>
							</center><br/>
							<br/>
							Please bookmark the following URL: <br/>
							<blockquote>${loginUrl.encodeAsRaw()}</blockquote> for your convenience.<br />
							<br/>
							<transmart:useUserGuide/><br/>
							<br/>
							Thank you!.<br/>
							<br />
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

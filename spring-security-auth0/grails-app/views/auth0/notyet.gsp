<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
	<meta name="layout" content="bootstrap" />
	<title>Registration - Not Yet!</title>
    </head>
    <body>
	<g:render template='/layouts/navbar_nomenu' />
	<div class="top-content">
	    <div class="inner-bg">
		<div class="container">
		    <div class="row well" style="opacity: .85; color: black;">
			<div class="col-sm-12">
			    <div class="description">
				<h2>You have not yet been authorized to access the system!</h2>
				<br />
				<p>The administrator has not yet completed your request and you currently unable to access the system.</p>
				<p>Please wait until you receive an e-mail, stating that your access level has been approved.</p>
				<br />
				Thank you!
			    </div>
			    <g:render template='/notification/email_signature' />
			</div>
		    </div>
		</div>
	    </div>
	</div>
    </body>
</html>

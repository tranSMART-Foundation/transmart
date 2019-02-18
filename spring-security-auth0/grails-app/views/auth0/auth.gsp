<html>
<head>
	<meta name="layout" content="bootstrap" />
	<title>${page_title ?: appTitle}</title>
	<style>
	<cms:section name='auth0.auth.css'>
	${auth0ConnectionCss.encodeAsRaw()}
	a.a0-zocial img {
		width: 28px;
	}
	</cms:section>
	</style>
</head>
<body>
<g:render template='/layouts/navbar_nomenu' />
<div class="container" style="background-color: white;">
	<g:if test="${!auth0AdminExists}">
	<div style="position: absolute; top: 70px; right: 50px;">
		<g:link uri='/login/admin'>Admin Login</g:link>
	</div>
	</g:if>
	<div align="center" style="clear: both; margin-left: auto; margin-right: auto; margin-top: 20px; text-align: center">
		<table style="width: auto; border: 0; text-align: center; margin: auto;" align="center">
			<tr>
				<td style="text-align: center; vertical-align: middle; margin-left: -40px;">
					<img src="${cms.link(name: 'transmartlogoHMS.jpg')}" alt="Transmart" />
				</td>
			</tr>
			<tr id="bannerRow">
				<td>
					<g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if> &nbsp;
				</td>
			</tr>
			<tr id="loginWidgetRow">
				<td colspan=2 valign="middle" style="text-align: center; vertical-align: middle; border: 1px; font-size: 11px" nowrap="nowrap">
					<cms:section name='auth0.auth.login'>
					<div id="frmAuth0Login"></div>
					<g:if test="${guestAutoLogin}">
						<g:render template='public' />
					</g:if>
					</cms:section>
				</td>
			</tr>
		</table>
	</div>
	<br />
	<div align="center" style="clear: both; margin-left: auto; margin-right: auto; margin-top: 20px; text-align: center">
		This application has been secured using standards published by the Harvard University Information Technology (HUIT) group. <br />
		<a href="http://huit.harvard.edu/" target="_blank">
			<g:img dir='images' file='info_security_logo_rgb.png' width='150px' />
		</a>
	</div>
	<br />
</div>
<content tag="javascript">
	<g:if test="${googleAnalyticsTracking}">
	<script>
		(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
			(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
				m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
		})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
		ga('create', '${googleAnalyticsTracking}', 'auto');
		ga('send', 'pageview');
	</script>
	</g:if>
%{-- DI-817 If AutoLogin is enabled, do not display the Auth0 button list. --}%
<g:if test="${!guestAutoLogin}">
<cms:section name='auth0.auth.js'>
<script src="https://cdnjs.cloudflare.com/ajax/libs/require.js/2.3.2/require.min.js"></script>
<script>
	$('#frmAuth0Login').html('Loading Authentication Providers');
	var oauth = {
		client_id: "${auth0ClientId}",
		domain: "${auth0Domain}",
		callbackURL: "${createLink(absolute: true, uri: auth0CallbackUrl)}"
	};
	${auth0ConnectionJs.encodeAsRaw()}
</script>
</cms:section>
</g:if>
<script src="${resource(dir: '/js', file: 'auth.js', plugin: 'spring-security-auth0')}"></script>
</content>
</body>
</html>

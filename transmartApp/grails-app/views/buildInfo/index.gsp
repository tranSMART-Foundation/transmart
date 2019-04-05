<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Build Info</title>
    </head>

    <body>
	<div class="body">
	    <g:render template='/buildInfo/buildInfo' model="[warDeployed: warDeployed]"/><br/>
	    <g:render template='/buildInfo/runtimeStatus' model="[envName: envName, javaVersion: javaVersion]"/><br/>
	    <g:render template='/buildInfo/installedPlugins' model="[plugins: plugins]"/><br/>
	</div>
    </body>
</html>

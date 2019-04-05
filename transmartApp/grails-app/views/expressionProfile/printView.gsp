<!DOCTYPE HTML>
<html>
    <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<asset:image rel="shortcut icon" href="transmart.ico"/>
	<asset:link rel="icon" href="images/transmart.ico"/>
	<asset:stylesheet href="main.css"/>
	<title>${grailsApplication.config.com.recomdata.appTitle}</title>
    </head>

    <body onload="window.print();">
	<table>
	    <tr>
		<td><img src="${createLink(action: 'displayChart') + '?filename=' + filename}"/></td>
	    </tr>
	    <tr>
		<td>&nbsp;</td>
	    </tr>
	    <tr>
		<td>
		    <center>
			<a href="#" onclick="window.print();">
			    <img src="${resource(dir: 'images', file: 'print.png')}"/>
			    Print
			</a>
		    </center>
		</td>
	    </tr>
	</table>
    </body>
</html>

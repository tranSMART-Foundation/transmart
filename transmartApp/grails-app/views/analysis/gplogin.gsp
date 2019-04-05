<!DOCTYPE html>
<html>
    <head>
    </head>
    <body onload="submitForm();">

	<!-- load an image to force browser to get a session cookie... -->
	<img src="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/images/GP-logo.gif"
	     alt="GenePattern" height="48" style="border: 0;" width="229"/>

	<form id="loginForm" name="loginForm" method="get"
	      action="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/pages/login.jsf"
	      enctype="application/x-www-form-urlencoded">
	    <input type="hidden" name="loginForm" value="loginForm"/>
	    <input type="hidden" name="javax.faces.ViewState" id="javax.faces.ViewState" value="j_id1"/>
	    <input type="hidden" id="username" name="username" type="text" value="${userName}"/>
	    <input type="hidden" id="loginForm:signIn" type="text" name="loginForm:signIn" value="Sign in"/>
	    <input id="loginForm:signIn" type="submit" name="loginForm:signIn" value="Sign in"/>
	    <br/>
	</form>

	<script>
	    function extractCookie(result, request) {
		alert("result is " + result);
		parent.gpCookie = result;
	    }

	    function submitFormDelayed() {
		setTimeout("submitForm()", 50);
	    }

	    function submitForm() {
		document.forms["loginForm"].submit();
	    }
	</script>
    </body>
</html>

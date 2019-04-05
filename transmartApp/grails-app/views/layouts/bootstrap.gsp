<!DOCTYPE html>
<html>
    <head>
	<meta charset="utf-8"/>
	<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
	<meta name="viewport" content="width=device-width, initial-scale=1"/>
	%{-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags --}%
	<meta name="description" content=""/>
	<meta name="author" content=""/>
	<title><g:layoutTitle default='tranSMART'/></title>
	<link rel="stylesheet" href="//fonts.googleapis.com/css?family=Roboto:400,100,300,500"/>
	<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet"
	      integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
	<script src="https://code.jquery.com/jquery-3.2.1.min.js"
		integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4="
		crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
		integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
		crossorigin="anonymous"></script>
	<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/bs/dt-1.10.16/datatables.min.css"/>
	<script src="https://cdn.datatables.net/v/bs/dt-1.10.16/datatables.min.js"></script>
	<asset:javascript src="jquery.backstretch.min.js"/>
	<asset:javascript src="retina-1.1.0.min.js"/>
	<style>
	    body {
	    padding-top: 70px;
	    font-size: 150%;
	    }
	    .navbar-brand {
	    line-height: 30px;
	    }
	    .navbar {
	    min-height: 60px;
	    }
	    .navbar-default {
	    background-color: #A41034;
	    border-color: #A41034;
	    }
	    .navbar-default .navbar-nav > li > a {
	    color: white;
	    }
	</style>
	<g:layoutHead/>
    </head>
    <body>
	<g:layoutBody/>
	<script>
	    $(document).ready(function () {
	    $.backstretch('${resource(dir: '/assets/img/backgrounds', file: '1.jpg')}');
	    });
	</script>
	<g:pageProperty name='page.javascript'/>
    </body>
</html>

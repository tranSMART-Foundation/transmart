<!DOCTYPE html>
<html>
    <head>
	<title>Transmart</title>

	<asset:link href="transmart.ico" rel="shortcut icon"/>
	<asset:link href="images/transmart.ico" rel="icon"/>


	<asset:javascript src="jquery-plugin.js"/>
	<asset:stylesheet href="extjs.css"/>
	<asset:javascript src="extjs.min.js"/>
	<asset:stylesheet href="main.css"/>

	<script>
            Ext.BLANK_IMAGE_URL = "${assetPath(src:'s.gif')}";
            // set ajax to 180*1000 milliseconds
            Ext.Ajax.timeout = 180000;
            Ext.QuickTips.init();

            var $j = window.$j = jQuery.noConflict();
	</script>
	<g:layoutHead/>
    </head>

    <body>
	<g:layoutBody/>
    </body>
</html>

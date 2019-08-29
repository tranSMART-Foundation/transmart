<!DOCTYPE html>
<html>
    <head>
	<title>Gene Signature</title>
	<asset:link rel="icon" href="transmart.ico" type="text.x-icon"/>
	<asset:stylesheet href="main.css"/>
	<asset:stylesheet href="ext/resources/css/ext-all.css"/>
	<asset:stylesheet href="ext/resources/css/xtheme-gray.css"/>
	<asset:stylesheet href="genesignature.css"/>
	<asset:javascript src="ext/adapter/ext/ext-base.min.js"/>
	<asset:javascript src="ext/ext-all.min.js"/>
	<asset:javascript src="maintabpanel.js"/>
	<asset:javascript src="toggle.js"/>
	<script charset="utf-8">
            Ext.BLANK_IMAGE_URL = "${assetPath(src:'s.gif')}";

            // set ajax to 90*1000 milliseconds
            Ext.Ajax.timeout = 180000;

            // qtip on
            Ext.QuickTips.init();
	</script>
    </head>

    <body>
	<div id="page">
	    <g:render template='gene_sig_detail' model="[gs: gs]"/>
	</div>
    </body>
</html>

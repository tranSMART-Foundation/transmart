<html>
    <head>
	<title>Gene Signature</title>
	<link rel="shortcut icon" href="${resource(dir: 'images', file: 'transmart.ico')}">
	<link rel="icon" href="${resource(dir: 'images', file: 'transmart.ico')}">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'js/ext/resources/css', file: 'ext-all.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'js/ext/resources/css', file: 'xtheme-gray.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'genesignature.css')}"/>
	<script src="${resource(dir: 'js/ext/adapter/ext', file: 'ext-base.js')}"></script>
	<script src="${resource(dir: 'js/ext', file: 'ext-all.js')}"></script>
	<script src="${resource(dir: 'js', file: 'maintabpanel.js')}"></script>
	<script src="${resource(dir: 'js', file: 'toggle.js')}"></script>
	<script charset="utf-8">
            Ext.BLANK_IMAGE_URL = "${resource(dir:'js/ext/resources/images/default', file:'s.gif')}";

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

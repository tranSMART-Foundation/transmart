<!DOCTYPE html>
<html>
    <head>
	<title><g:layoutTitle default="Gene Signature"/></title>

	<asset:link rel="shortcut icon" href="transmart.ico" type="image/x-ico" />
	<asset:link rel="icon" href="transmart.ico" type="image/x-ico" />

	<asset:stylesheet href="main.css"/>
	<asset:stylesheet href="sanofi.css"/>
	<asset:stylesheet href="genesignature.css"/>
	<asset:stylesheet href="signatureTab.css" />

	<asset:javascript src="jquery-plugin.js" />
	<asset:javascript src="extjs.min.js" />
	<asset:javascript src="session_time.js" />
	<asset:javascript src="signatureTab.js" />
	<asset:javascript src="toggle.js"/>

	<script>
            Ext.BLANK_IMAGE_URL = "${assetPath(src:'s.gif')}";
            Ext.Ajax.timeout = 180000;
            Ext.onReady(function () {
                Ext.QuickTips.init()
            });
            var $j = window.$j = jQuery.noConflict();
	</script>
    </head>

    <body>
	<div id="page">
	    <div id="header-div" class="header-div">
		<g:render template='/layouts/commonheader' model="[app: 'genesignature']"/>
	    </div>

	    <div id="app"><g:layoutBody/></div>
	</div>
    </body>
</html>

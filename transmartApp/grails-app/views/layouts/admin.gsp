<!DOCTYPE html>
<html>
    <head>
	<title>Transmart Administration</title>

	<asset:stylesheet href="adminTab.css"/>

	<asset:javascript src="jquery-plugin.js"/>
	<asset:javascript src="extjs.min.js"/>
	<asset:javascript src="session_time.js"/>
	<asset:javascript src="adminTab.js"/>

	<script type="text/javascript">
	    Ext.BLANK_IMAGE_URL = "${assetPath(src: 's.gif')}";

	    // set ajax to 180*1000 milliseconds
	    Ext.Ajax.timeout = 180000;
	    var pageInfo;

	    Ext.onReady(function() {
	        Ext.QuickTips.init();

	        var helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
	        var contact = '${grailsApplication.config.com.recomdata.contactUs}';
	        var appTitle = '${grailsApplication.config.com.recomdata.appTitle}';
	        var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
				   
	        var viewport = new Ext.Viewport({
	            layout: "border",
	            items:[new Ext.Panel({                          
                        region: "center",  
                        autoScroll:true,                     
                        contentEl: "page"
                    })]
	        });
	        viewport.doLayout();

	        pageInfo = {
	            basePath :"${request.contextPath}"
	        }
	    });
	</script>
    </head>

    <body>
	<div id="page">
	    <div id="header-div" class="header-div">
		<g:render template='/layouts/commonheader' model="[app: 'accesslog']"/>
	    </div>

	    <div id='navbar'><g:render template='/layouts/adminnavbar'/></div>

	    <div id="content"><g:layoutBody/></div>
	</div>
    </body>
</html>

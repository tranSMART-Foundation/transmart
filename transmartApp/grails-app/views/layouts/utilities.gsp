<!DOCTYPE html>
<html>
    <head>
	<title><g:layoutTitle default=""/></title>

	<asset:stylesheet href="adminTab.css"/>

	<asset:javascript src="jquery-plugin.js"/>
	<asset:javascript src="extjs.min.js"/>
	<asset:javascript src="session_time.js"/>
	<asset:javascript src="adminTab.js"/>

	<script>
	    Ext.BLANK_IMAGE_URL = "${assetPath(src:'s.gif')}";

            // set ajax to 90*1000 milliseconds
            Ext.Ajax.timeout = 180000;
            var pageInfo;

            Ext.onReady(function()
            {
                Ext.QuickTips.init();

                var helpURL = "${grailsApplication.config.org.transmartproject.helpUrls.index ?: '/transmartmanual/index.html'}";
                var contact = "${grailsApplication.config.com.recomdata.contactUs}";
                var appTitle = "${grailsApplication.config.com.recomdata.appTitle}";
                var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
                   
                var viewport = new Ext.Viewport({
                    layout: "border",
                    items:[new Ext.Panel({                          
                       region: "center",  
                       //tbar: createUtilitiesMenu(helpURL, contact, appTitle,'${request.getContextPath()}', buildVer, 'admin-utilities-div'),
                       autoScroll:true,                     
                       contentEl: "page"
                    })]
                });
                viewport.doLayout();

                pageInfo = {
                    basePath :"${request.getContextPath()}"
                }
            });

	</script>

	<g:layoutHead/>
    </head>

    <body>
	<div id="page">
	    <div id="header-div"><g:render template="/layouts/commonheader" model="['app': 'admin']"/></div>

	    <div style="float: right; width: 97%;"><g:layoutBody/></div>
	</div>
    </body>
</html>

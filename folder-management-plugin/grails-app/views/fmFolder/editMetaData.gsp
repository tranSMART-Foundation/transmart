<!DOCTYPE html>
<html>
    <head>
        %{-- Force Internet Explorer 8 to override compatibility mode --}%
        <meta http-equiv="X-UA-Compatible" content="IE=edge" >        
        
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>${grailsApplication.config.com.recomdata.appTitle}</title>
        
        %{-- jQuery CSS for cupertino theme --}%
        <asset:stylesheet href="jquery/ui/jquery-ui-1.10.3.custom.css"/>
        <asset:stylesheet href="jquery/skin/ui.dynatree.css"/>
        
        %{-- Our CSS --}%
        <asset:stylesheet href="browseTab.css"/>
        <asset:stylesheet href="folderManagement.css"/>
        <asset:stylesheet href="jquery/simpleModal.css"/>
        <asset:stylesheet href="jquery/multiselect/ui.multiselect.css"/>
        <asset:stylesheet href="jquery/multiselect/common.css"/>
        <asset:stylesheet href="jquery/jqueryDatatable.css"/>
                                
        %{-- jQuery JS libraries --}%
        <asset:javascript src="jquery-plugin.js"/>
        <asset:javascript src="jquery-ui.js"/>
        <asset:javascript src="extjs.min.js"/>
        <asset:javascript src="session_time.js"/>
        <asset:javascript src="browseTab.js"/>

        <script>jQuery.noConflict();</script> 
        
        <asset:javascript src="facetedSearch/facetedSearchBrowse.js"/>

        %{--  SVG Export --}%
        <%--<asset:javascript src="svgExport/rgbcolor.js"/>  --%>

        <asset:javascript src="ext-prototype-adapter.js"/> 
        <script>
            var $j = jQuery.noConflict();
        </script>

        %{-- Our JS --}%
        <asset:javascript src="datasetExplorer.js"/>

        %{-- Protovis Visualization library and IE plugin (for lack of SVG support in IE8 --}%
        <%-- <asset:javascript src="protovis/protovis-r3.2.js"/>
             <asset:javascript src="protovis/protovis-msie.min.js"/> --%>
    </head>

    <body>
	<div style="width:800px">
	    <g:render template="editMetaData" model="[folder:folder, measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, layout:layout]" />
	</div>
    </body>
</html>

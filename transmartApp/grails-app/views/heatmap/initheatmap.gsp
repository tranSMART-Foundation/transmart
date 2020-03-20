<!DOCTYPE html>
<html>
    <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>

    <body onLoad="init()">
	<div style="margin-top:100px;text-align: center;">
	    <asset:image src="loader-large.gif" alt="loading"/>
	</div>

	<div style="margin-top:20px; text-align: center;"><b>Generating Heatmap...</b>
	</div>

	<div style="display:none">
	    <g:form name='showheatmapform1' controller='heatmap' action='showheatmap'/>
	</div>

	<script>
        function init() {
            setTimeout("showheatmap();", 500)
        }

        function showheatmap() {
            document.showheatmapform1.submit();
        }
	</script>
    </body>
</html>

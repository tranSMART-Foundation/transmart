<!DOCTYPE html>
<html>
    <head>
	<title>${grailsApplication.config.com.recomdata.appTitle}</title>
	<asset:stylesheet href="main.css"/>
	<script>
        function refreshParent(newurl) {
            parent.window.close();
            if (parent != null && parent.window.opener != null && !parent.window.opener.closed) {
                parent.window.opener.location = newurl;
            }
        }
	</script>
    </head>
    <body>
	<div id="summary">
	    <p class="Title">
		<span class="Title">
		</span>
	    </p>
	    <div id="SummaryHeader">
		<span class="SummaryHeader">Available Compounds</span>
	    </div>
	    <table class="trborderbottom" width="100%">
		<g:each in="${compounds}" status="i" var="rec">
		    <g:set var="k" value="${rec[0]}"/>
		    <g:set var="c" value="${rec[1]}"/>
		    <tr style="border-bottom:1px solid #CCCCCC;padding-botton:2px;">
			<td style="width:150px;">${createKeywordSearchLink(popup: true, jsfunction: "refreshParent", keyword: k)}</td>
			<td>
			    ${c.genericName}
			    <g:if test="${c.cntoNumber}">, ${c.cntoNumber}</g:if>
			    <g:if test="${c.brandName}">, ${c.brandName}</g:if>
			    <g:if test="${c.number && c.number != k.keyword}">, ${c.number}</g:if>
			    <g:if test="${c.mechanism}">- ${c.mechanism}</g:if>
			</td>
		    </tr>
		</g:each>
	    </table>
	    <br/>
	</div>
    </body>
</html>

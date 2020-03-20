<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
	<div id="divjobstatus" style="background:#fff;height:100%;padding:5px; font:12px tahoma, arial, helvetica, sans-serif;">
	    <table width="100%" style="background:#fff;">
		<g:each status="i" in="${jobStatuses}" var="jobStatus">
		    <tr>
			<td style="height: 20px!important;">${jobStatus}
			    <g:if test="${i < statusIndex}">
				<asset:image src="green_check.png"/>
			    </g:if>
			    <g:elseif test="${i == statusIndex}">
				<asset:image src="loading-balls.gif"/>
			    </g:elseif>
			</td>
		    </tr>
		</g:each>
	    </table>
	</div>
    </body>
</html>

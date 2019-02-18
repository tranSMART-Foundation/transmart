<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>AccessLogEntry List</title>
    </head>

    <body>
	<div class="body">
	    <g:form name="form">
		<table style="width: 700px;">
		    <tr>
			<td>Start Date&nbsp;&nbsp;<input id="startdate" name="startdate" type="text" value="${startdate}"></td>
			<td>End Date&nbsp;&nbsp;<input id="enddate" name="enddate" type="text" value="${enddate}"></td>
			<td>
			    <g:actionSubmit class="filter" value="Filter" action="list"/>
			    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			    <g:actionSubmit class="filter" value="Export to Excel" action="export"/>
			</td>
		    </tr>
		</table>
	    </g:form>
	    <h1>AccessLogEntry List</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="list">
		<table>
		    <thead>
			<tr style="height: 30px;">
			    <th style="vertical-align: middle;">Access Time</th>
			    <th style="vertical-align: middle;">User</th>
			    <th style="vertical-align: middle;">Event</th>
			    <th style="vertical-align: middle;">Event Message</th>
			    <th style="vertical-align: middle;">Request Url</th>
			</tr>
		    </thead>
		    <tbody>
			<g:each in="${accessLogList}" status="i" var="accessLog">
			    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 30px;">
				<td style="width: 180px; vertical-align: top;">${fieldValue(bean: accessLog, field: 'accessTime')}</td>
				<td style="width: 100px; vertical-align: top;">${fieldValue(bean: accessLog, field: 'username')}</td>
				<td style="width: 200px; vertical-align: top;">${fieldValue(bean: accessLog, field: 'event')}</td>
				<td style="vertical-align: top;">${fieldValue(bean: accessLog, field: 'eventMessage')}</td>
				<td style="vertical-align: top;">
				    <g:if test="${accessLog.requestURL}">
					<a href="${accessLog.requestURL}" target="_blank">link</a>
				    </g:if>
				</td>
			    </tr>
			</g:each>
		    </tbody>
		</table>
	    </div>

	    <div class="paginateButtons">
		<g:paginate total="${totalcount}" maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
		    max="${grailsApplication.config.com.recomdata.search.paginate.max}"/>
	    </div>
	</div>
	<r:script>
	    jQuery(function () {
	    jQuery("#startdate").datepicker({ dateFormat: 'dd/mm/yy' });
	    });
	    jQuery(function () {
	    jQuery("#enddate").datepicker({ dateFormat: 'dd/mm/yy' });
	    });
	</r:script>
    </body>
</html>

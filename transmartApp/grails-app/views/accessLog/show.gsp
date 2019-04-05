<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Show AccessLogEntry</title>
    </head>

    <body>
	<div class="body">
	    <h1>Show AccessLogEntry</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>

			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: accessLog, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">User:</td>
			    <td valign="top" class="value">${fieldValue(bean: accessLog, field: 'username')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Event:</td>
			    <td valign="top" class="value">${fieldValue(bean: accessLog, field: 'event')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Event Message:</td>
			    <td valign="top" class="value">${fieldValue(bean: accessLog, field: 'eventMessage')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Access Time:</td>
			    <td valign="top" class="value">${fieldValue(bean: accessLog, field: 'accessTime')}</td>
			</tr>

		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${accessLog?.id}"/>
		    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

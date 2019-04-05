<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Show User Group</title>
    </head>

    <body>

	<div class="body">
	    <h1>User Group</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>
			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Enabled:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'enabled')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Description:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'description')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Group Category:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'groupCategory')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Name:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'name')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Type:</td>
			    <td valign="top" class="value">${fieldValue(bean: ug, field: 'type')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Members:</td>
			    <td valign="top" style="text-align:left;" class="value">
				<ul>
				    <g:each var='m' in="${ug.members}">
					<li><g:link controller='authUser' action='show' id="${m.id}">${m?.encodeAsHTML()}</g:link></li>
				    </g:each>
				</ul>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Access to Studies:</td>
			    <td valign="top" class="value">
				<ul>
				    <g:each in="${soas}" var='soa'>
					<li><g:link controller='secureObject' action='show' id="${soa.secureObjectId}">${soa.objectAccessName}</g:link></li>
				    </g:each>
				</ul>
			    </td>
			</tr>
		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${ug?.id}"/>
		    <span class="button"><g:actionSubmit class='edit' value='Edit'/></span>
		    <span class="button"><g:actionSubmit class='delete' onclick="return confirm('Are you sure?');"
							 value='Delete'/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

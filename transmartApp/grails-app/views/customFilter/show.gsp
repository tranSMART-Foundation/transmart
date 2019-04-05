<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="main"/>
	<title>Show CustomFilter</title>
    </head>

    <body>
	<div class="nav">
	    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
	    <span class="menuButton"><g:link class="list" action="list">CustomFilter List</g:link></span>
	    <span class="menuButton"><g:link class="create" action="create">New CustomFilter</g:link></span>
	</div>

	<div class="body">
	    <h1>Show CustomFilter</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>
			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: customFilter, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Name:</td>
			    <td valign="top" class="value">${fieldValue(bean: customFilter, field: 'name')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Description:</td>
			    <td valign="top" class="value">${fieldValue(bean: customFilter, field: 'description')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Private Flag:</td>
			    <td valign="top" class="value">${fieldValue(bean: customFilter, field: 'privateFlag')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Items:</td>
			    <td valign="top" style="text-align:left;" class="value">
				<ul>
				    <g:each var="i" in="${customFilter.items}">
					<li><g:link controller="customFilterItem" action="show" id="${i.id}">${i?.encodeAsHTML()}</g:link></li>
				    </g:each>
				</ul>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Search User Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: customFilter, field: 'searchUserId')}</td>
			</tr>

		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${customFilter?.id}"/>
		    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

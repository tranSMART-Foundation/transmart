<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Show SecureObject</title>
    </head>

    <body>
	<div class="body">
	    <h1>Show SecureObject</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div class="dialog">
		<table>
		    <tbody>

			<tr class="prop">
			    <td valign="top" class="name">Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: so, field: 'id')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Bio Data Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: so, field: 'bioDataId')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Data Type:</td>
			    <td valign="top" class="value">${fieldValue(bean: so, field: 'dataType')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Bio Data Unique Id:</td>
			    <td valign="top" class="value">${fieldValue(bean: so, field: 'bioDataUniqueId')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Concept Paths:</td>
			    <td valign="top" style="text-align:left;" class="value">
				<ul>
				    <g:each var="c" in="${so.conceptPaths}">
					<li><g:link controller="secureObjectPath" action="show"
						    id="${c.id}">${c?.conceptPath?.encodeAsHTML()}</g:link></li>
				    </g:each>
				</ul>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">Display Name:</td>
			    <td valign="top" class="value">${fieldValue(bean: so, field: 'displayName')}</td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name">User/Group With Access:</td>
			    <td valign="top" class="value">
				<ul>
				    <g:each in="${soas}" var='soa'>
					<g:if test="${soa.principal.type == 'GROUP'}">
					    <li><g:link controller="userGroup" action="show" id="${soa.principalId}">${soa.principalAccessName}</g:link></li>
					</g:if>
					<g:else>
					    <li><g:link controller="authUser" action="show" id="${soa.principalId}">${soa.principalAccessName}</g:link></li>
					</g:else>
				    </g:each>
				</ul>
			    </td>
			</tr>
		    </tbody>
		</table>
	    </div>

	    <div class="buttons">
		<g:form>
		    <input type="hidden" name="id" value="${so?.id}"/>
		    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</g:form>
	    </div>
	</div>
    </body>
</html>

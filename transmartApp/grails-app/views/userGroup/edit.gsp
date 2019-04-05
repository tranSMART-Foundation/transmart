<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Edit UserGroup</title>
	<qsset:javascript src='jquery-plugin.js'/>
    </head>

    <body>

	<div class="body">
	    <h1>Edit UserGroup</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${ug}">
		<div class="errors">
		    <g:renderErrors bean="${ug}" as='list'/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${ug?.id}"/>
		<input type="hidden" name="version" value="${ug?.version}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="enabled">Enabled:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'enabled', 'errors')}">
				    <g:checkBox name='enabled' value="${ug?.enabled}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="description">Description:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'description', 'errors')}">
				    <textarea rows="5" cols="40" name="description" id="description">${fieldValue(bean: ug, field: 'description')}</textarea>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="name">Name:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'name', 'errors')}">
				    <input type="text" id="name" name="name"
					   value="${fieldValue(bean: ug, field: 'name')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label>Members:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: ug, field: 'members', 'errors')}">
				    <table>
					<tr>
					    <td>&nbsp;</td>
					    <td>&nbsp;</td>
					    <td>
						<input name="searchtext" id="searchtext"/>
						<button class=""
							onclick="${remoteFunction(action:'searchUsersNotInGroup',update:[success:'groupmembers', failure:''], id:ug?.id, params:'jQuery(\'#searchtext\').serialize()' )};
								 return false;">Search Users</button>
					    </td>
					</tr>
					<tr>
					    <td>Members of group:</td>
					    <td>&nbsp;</td>
					    <td>Available users:</td>
					</tr>
					<tr id="groupmembers">
					    <g:render template='addremove' bean="${ug}"/>
					</tr>
				    </table>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>
		<div class="buttons">
		    <span class="button"><g:actionSubmit class='save' value='Update'/></span>
		    <span class="button"><g:actionSubmit class='delete' onclick="return confirm('Are you sure?');" value='Delete'/></span>
		</div>
	    </g:form>
	</div>
</body>
</html>

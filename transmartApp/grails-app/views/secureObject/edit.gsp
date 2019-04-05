<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Edit SecureObject</title>
    </head>

    <body>
	<div class="body">
	    <h1>Edit SecureObject</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${so}">
		<div class="errors">
		    <g:renderErrors bean="${so}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${so?.id}"/>
		<input type="hidden" name="version" value="${so?.version}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="bioDataId">Bio Data Id:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'bioDataId', 'errors')}">
				    <input type="text" id="bioDataId" name="bioDataId"
					   value="${fieldValue(bean: so, field: 'bioDataId')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="dataType">Data Type:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'dataType', 'errors')}">
				    <textarea rows="5" cols="40" name="dataType" id="dataType">${fieldValue(bean: so, field: 'dataType')}</textarea>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="bioDataUniqueId">Bio Data Unique Id:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'bioDataUniqueId', 'errors')}">
				    <input type="text" id="bioDataUniqueId" name="bioDataUniqueId"
					   value="${fieldValue(bean: so, field: 'bioDataUniqueId')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="conceptPaths">Concept Paths:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: so, field: 'conceptPaths', 'errors')}">
				    <ul>
					<g:each var="c" in="${so?.conceptPaths ?}">
					    <li><g:link controller="secureObjectPath" action="show"
							id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
					</g:each>
				    </ul>
				    <g:link controller="secureObjectPath" params="['secureObject.id': so?.id]"
					    action="create">Add SecureObjectPath</g:link>

				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="displayName">Display Name:</label>
				</td>
				<td valign="top"
				    class="value ${hasErrors(bean: so, field: 'displayName', 'errors')}">
				    <input type="text" id="displayName" name="displayName"
					   value="${fieldValue(bean: so, field: 'displayName')}"/>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><g:actionSubmit class="save" value="Update"/></span>
		    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/></span>
		</div>
	    </g:form>
	</div>
    </body>
</html>

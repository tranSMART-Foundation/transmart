<!DOCTYPE html>
<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Edit SecureObjectPath</title>
    </head>

    <body>
	<div class="body">
	    <h1>Edit SecureObjectPath</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${sop}">
		<div class="errors">
		    <g:renderErrors bean="${sop}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${sop?.id}"/>
		<input type="hidden" name="version" value="${sop?.version}"/>

		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="conceptPath">Concept Path:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: sop, field: 'conceptPath', 'errors')}">
				    <input type="text" id="conceptPath" name="conceptPath" value="${fieldValue(bean: sop, field: 'conceptPath')}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="secureObject.id">Secure Object:</label>
				</td>
				<td valign="top" class="value ${hasErrors(bean: sop, field: 'secureObject', 'errors')}">
				    <g:select optionKey='id' name='secureObject.id' from="${secureObjects}" value="${sop?.secureObjectId}"/>
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

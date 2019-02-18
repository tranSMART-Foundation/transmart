<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Manage Study Access</title>
    </head>

    <body>
	<div class="body">
	    <h1>Manage Study Access</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${soa}">
		<div class="errors">
		    <g:renderErrors bean="${soa}" as="list"/>
		</div>
	    </g:hasErrors>

	    <div class="dialog">
		<g:form name='secobjaccessform' action='manageAccessBySecObj'>
		    <table>
			<tbody>
			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="secureobjectid">Study:</label>
				    <g:select optionKey='id' optionValue='displayName' name='secureobjectid'
					      onchange='document.secobjaccessform.submit();'
					      from="${secureObjects}" value="${soa?.id}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name">
				    <label for="accesslevelid">Access Level:</label>
				    <g:select optionKey='id' optionValue='accessLevelName' name='accesslevelid'
					      onchange='document.secobjaccessform.submit();'
					      from="${secureAccessLevels}" value="${accesslevelid}"/>
				</td>
			    </tr>
			</tbody>
		    </table>

		    <tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>
			    <input name="searchtext" id="searchtext"/>
			    <input type="submit" value="Search User/Groups"/>
			</td>
		    </tr>
		</g:form>
		<div>
		    <table>
			<tbody>
			    <tr>
				<td><b>User/Group Assigned Access</b></td>
				<td>&nbsp;</td>
				<td><b>User/Group Without Access</b></td>
			    </tr>
			    <tr id="groups">
				<g:render template='addremovePrincipal' model="[userwithoutaccess: userwithoutaccess, soas: soas]"/>
			    </tr>
			</tbody>
		    </table>
		</div>

	    </div>
    </body>
</html>

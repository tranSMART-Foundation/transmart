<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Create User</title>
    </head>

    <body>
	<div class="body">

	    <h1>Create User</h1>

	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${person}">
		<div class="errors">
		    <g:renderErrors bean="${person}" as="list"/>
		</div>
	    </g:hasErrors>

	    <g:form controller='authUser' action='save'>
		<div class="dialog">
		    <table>
			<tbody>

			    <tr class="prop">
				<td valign="top" class="name"><label for="userRealName">Full Name:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'userRealName', 'errors')}">
				    <g:textField name='userRealName' value="${person.userRealName?.encodeAsHTML()}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label for="email">Email:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'email', 'errors')}">
				    <g:textField name='email' value="${person?.email?.encodeAsHTML()}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label>User Level:</label></td>
				<td valign="top" class="value">
				    <g:select name='userLevel' value="${userLevel}" optionValue='description' from="${userLevels}"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label>Auth0 Provider:</label></td>
				<td valign="top" class="value">
				    <g:select name='auth0Provider' value="${auth0Provider}" from="${auth0Providers}"
					      noSelection="${['null': 'Select...']}" onchange="updateToolip()"/>
				</td>
			    </tr>

			    <tr class="prop">
				<td valign="top" class="name"><label for="uniqueId">Provider ID:</label></td>
				<td valign="top" class="value ${hasErrors(bean: person, field: 'uniqueId', 'errors')}">
				    <g:textField name='uniqueId' value="${uniqueId}"/>
				    <g:img id='helpicon' dir='images/help' file='helpicon.png'/>
				</td>
			    </tr>

			</tbody>
		    </table>
		</div>

		<div class="buttons">
		    <span class="button"><g:submitButton name='save' value='Create' class='save' /></span>
		</div>

	    </g:form>

	</div>
	<script>
function updateToolip() {
	var tooltip = '';
	switch($('#auth0Provider').val()) {
		case 'Google':                     tooltip = 'A Gmail address, or the internal 21-digit Google id number'; break;
		case 'GitHub':                     tooltip = 'An email address, or the internal GitHub id number'; break;
		case 'ORCiD':                      tooltip = 'An ORCiD in the form dddd-dddd-dddd-dddd where each d is a digit, and the last may be an "X"'; break;
		case 'Harvard Medical School':     tooltip = 'An email address (@hms.harvard.edu)'; break;
		case 'eRA Commons':                tooltip = 'eRA Commons or NIH email'; break;
		case "Boston Children's Hospital": tooltip = 'BCH login name'; break;
	}
	$('#helpicon').attr('title', tooltip);
}
	</script>
    </body>
</html>

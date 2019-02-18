<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Loggers</title>
    </head>
    <body>

	<label for="logger">Logger:</label>
	<g:textField name='logger' size='75' />
	<g:select name='level' from="${allLevels}" onchange="onSelectChange(this, true);"/>

	<br/><hr/><br/>

	<table>
	    <caption>Loggers</caption>
	    <tbody style="display: block; overflow-y: auto; overflow-x: hidden; height: 300px; width: 80%;">
		<g:each in="${loggers}" var='entry'>
		    <g:set var='loggerName' value="${entry.key}"/>
		    <g:set var='loggerLevel' value="${entry.value}"/>
		    <tr>
			<td>${loggerName}</td>
			<td>
			    <g:select name="level_${loggerName}" from="${allLevels}" value="${loggerLevel}" onchange="onSelectChange(this, false);" />
			</td>
		    </tr>
		</g:each>
	    </tbody>
	</table>

	<br/><hr/><br/>

	Files:<br/>
	<g:each in="${files}" var='entry'>
	    <g:link action='downloadFile' id="${entry.key}">Download ${entry.value}</g:link><br/>
	</g:each>

	<script>
function generateParameters(theSelect, manual) {
	var logger;
	if (manual) {
		logger = jQuery('#logger').val();
	}
	else {
		logger = theSelect.id.substring('level_'.length);
	}

	return 'logger=' + escape(logger) + '&level=' + theSelect.options[theSelect.selectedIndex].value;
}

function onSelectChange(select, manual) {
	jQuery.ajax({
		data: generateParameters(select, manual),
		type: 'POST',
		url:  "${createLink(action: 'setLogLevel')}"
	});
}
	</script>
    </body>
</html>

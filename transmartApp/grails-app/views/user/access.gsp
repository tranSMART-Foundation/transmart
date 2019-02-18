<html>
    <head>
	<meta name="layout" content="bootstrap" />
	<title>User Profile</title>
	<style>
body {
	padding-top: 70px;
	background-color: white;
}
.btn-file {
	position: relative;
	overflow: hidden;
}
.btn-file input[type=file] {
	position: absolute;
	top: 0;
	right: 0;
	min-width: 100%;
	min-height: 100%;
	font-size: 100px;
	text-align: right;
	filter: alpha(opacity = 0);
	opacity: 0;
	outline: none;
	background: white;
	cursor: inherit;
	display: block;
}
	</style>
    </head>
    <body>
	<sec:ifAnyGranted roles='ROLE_ADMIN'>
	    <g:render template='/layouts/navbar_admin' />
	</sec:ifAnyGranted>
	<sec:ifNotGranted roles='ROLE_ADMIN'>
	    <g:render template='/layouts/navbar_user' />
	</sec:ifNotGranted>
	<g:if test="${(user.research_title ?: 'NULL') == 'NULL'}">
	    <div class="container well">
		<h1>Request <b>Level2</b> Access</h1>
		<p>Please ensure that your profile information is up to date.</p>
		<g:uploadForm controller='user' action='access'>
		    <div class="form-group">
			<label for="research_title">Title<sup>*</sup></label>
			<g:textField class='form-control' name='research_title' required='required' />
		    </div>
		    <div class="form-group">
			<label for="research_aims">Aims<sup>*</sup>:</label>
			<g:textArea name='research_aims' required='required' class='form-control' rows='5'
				    placeholder='Some specifics about the research project.' />
		    </div>
		    <div class="form-group">
			<label for="research_summary">Protocol Summary<sup>*</sup>:</label>
			<g:textArea name='research_summary' required='required' class='form-control' rows='10'
				    placeholder='In max. 600 characters, please describe your research protocol.'/>
		    </div>
		    <div class="form-group">
			<label>Please upload IRB approval letter for your project (.pdf or .docx formats only). </label><br />
			<span class="btn btn-default btn-file">Browse ...<input type="file" name="uploaded_file" /></span>
			<div id="uploaded_file_name"></div>
		    </div>
		    <br />
		    <a href="${createLink(controller: 'user', action: 'index')}#tab_accesslevel" class="btn btn-warning pull-left">&lt;&lt; Back</a>
		    <button type="submit" class="btn btn-info pull-right">Submit Request</button>
		</g:uploadForm>
	    </div>
	</g:if>
	<g:else>
	    <div class="container well">
		<h1>Your request has been submitted.</h1>
		<br />
		<p>You will receive a copy of the submitted information and the administrator will evaluate your request. <br /> <br />
		    If your request is missing information, the administrator will contact you to resolve any issues.
		    Once your request is complete, it will be forwarded to the GRDR Data Access Committee. <br /> <br />
		    This process could take up to six weeks.
		</p>
		<p>If you would like to check on the status of your request any time, you can contact Cassandra Perry at <a href="mailto:Cassandra_Perry@hms.harvard.edu">Cassandra_Perry@hms.harvard.edu</a>.<br /> <br />
		    Once the Data Access Committee has approved your request, the administrator will change your access level in the system
		    and an automated e-mail will be generative, informing you that you have new privileges.
		    Your access will be granted for a period of 12 months. Once expired, you may re-apply if access to the data is still needed.
		</p>
	</div>
	</g:else>
	<content tag="javascript"><script>
		$(document).ready(function() {
			$('.btn-file :file').on('fileselect', function(event, numFiles, label) {
				var input = $(this).parents('.input-group').find(':text')
				log = numFiles > 1 ? numFiles + ' files selected' : label;
				if (input.length) {
					input.val(log);
				}
				else if (log) {
					$('#uploaded_file_name').html('File ' + log + ' is selected.')
				}
			});
		});
		$(document).on('change', '.btn-file :file', function() {
			var input = $(this), numFiles = input.get(0).files ? input
					.get(0).files.length : 1, label = input.val()
					.replace(/\\/g, '/').replace(/.*\//, '');
			console.log(input);
			input.trigger('fileselect', [ numFiles, label ]);
		});
	</script></content>
    </body>
</html>

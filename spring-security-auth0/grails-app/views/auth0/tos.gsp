<html>
<head>
	<meta name='layout' content='bootstrap'/>
	<title>Terms Of Service</title>
	<style>
	.row-centered {
		text-align: center;
	}
	.col-centered {
		display: inline-block;
		float: none;
		text-align: left;
		margin-right: -4px;
	}
	</style>
</head>
<body>
<g:render template='/layouts/navbar_nomenu'/>
<div class="container well" style="opacity: 0.85; color: black">
	<g:if test="${flash.message}">
		<div class="alert alert-info alert-dismissable">
			<a class="panel-close close" data-dismiss="alert">×</a> <i class="fa fa-coffee"></i>
			${flash.message}
		</div>
	</g:if>
	<g:each in="${alerts}" var='alert'>
		<div class="alert alert-danger alert-dismissable">
			<a class="panel-close close" data-dismiss="alert">×</a> <i class="glyphicon glyphicon-link"></i>
			${alert}
		</div>
	</g:each>
	<div class="row row-centered">
		<h2>Terms Of Service</h2>
		<p>Effective <b>${tosEffectiveDate}</b></p>
		<br/>
		<div class="col-lg-10 col-centered">
			${tosValue}
		</div>
		<g:if test="${needAgreement}">
		<br/>
		<g:form action='checkTOS'>
			<p>The Terms of Service has changed. Please read the new details and agree to these terms.</p>
			<button class="btn btn-success">I Agree</button>
		</g:form>
		</g:if>
	</div>
</div>
<div class="modal fade" id="confirmModal">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title">Terms Of Service</h4>
			</div>
			<div class="modal-body">
				<p>Are you sure, you do not want to accept the Terms of Service? Please ensure, that if you would like to proceed, you need to select the checkbox and click on the 'Submit' button.</p>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-secondary" data-dismiss="modal">Still no!e</button>
				<button type="button" class="btn btn-primary">OK</button>
			</div>
		</div>
	</div>
</div>
<content tag="javascript"><script>
	$('#registrationForm').on('submit', function () {
		if ($('input[name="agree"]').prop('checked')) {
			return true;
		}
		$('#confirmModal').modal('toggle');
		return false;
	});
</script></content>
</body>
</html>

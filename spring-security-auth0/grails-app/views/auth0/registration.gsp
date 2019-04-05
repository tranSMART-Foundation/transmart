<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
	<meta name="layout" content="bootstrap"/>
	<title>User Registration</title>
	<asset:stylesheet href="parsley.css"/>
	<style>
	.form-section {
		padding-left: 15px;
		border-left: 2px solid #FF851B;
		display: none;
	}
	.form-section.current {
		display: inherit;
	}
	.btn-info, .btn-default {
		margin-top: 10px;
	}
	html.codepen body {
		margin: 1em;
	}
	</style>
    </head>
    <body>
	<g:render template='/layouts/navbar_nomenu' />
	<div class="container">
	    <div class="row well">
		<h1>
		    <strong>${instanceName}</strong> Request for <transmart:useAccessLevelDescription />
		</h1>
		<transmart:useAccessLevelMemo />
		<hr/>
		<div class="bs-callout bs-callout-warning hidden">
		    <h4>Oh snap!</h4>
		    <p>This form seems to be invalid :(</p>
		</div>
		<div class="bs-callout bs-callout-info hidden">
		    <h4>Yay!</h4>
		    <p>Everything seems to be ok :)</p>
		</div>
		<g:form class="demo-form" action='confirm'>
		    <g:hiddenField name='username' value="${user.username}" />
		    <g:render template="/user/personal"/>
		    <g:render template="/user/professional"/>
		    <g:render template="/user/other"/>
		    <br/>
		    <div class="form-navigation">
			<button type="button" class="previous btn btn-info pull-left">&lt; Previous</button>
			<button type="button" class="next btn btn-info pull-right">Next &gt;</button>
			<button class="btn btn-info pull-right" type="submit">
			    <span class="clearfix"></span><transmart:useRegisterButton />
			</button>
		    </div>
		</g:form>
	    </div>
	</div>
	<content tag="javascript">
	    <g:if test="${useRecaptcha}"><script type="text/javascript" src="https://www.google.com/recaptcha/api.js"><script></g:if>
	    <asset:javascript src="parsley.min.js"/>
	    <script type="text/javascript">
$(function () {
	var $sections = $('.form-section');

	function navigateTo(index) {
		// Mark the current section with the class 'current'
		$sections.removeClass('current').eq(index).addClass('current');
		// Show only the navigation buttons that make sense for the current section:
		$('.form-navigation .previous').toggle(index > 0);
		var atTheEnd = index >= $sections.length - 1;
		$('.form-navigation .next').toggle(!atTheEnd);
		$('.form-navigation [type=submit]').toggle(atTheEnd);
	}

	function curIndex() {
		// Return the current index by looking at which section has the class 'current'
		return $sections.index($sections.filter('.current'));
	}

	// Previous button is easy, just go back
	$('.form-navigation .previous').click(function () {
		navigateTo(curIndex() - 1);
	});

	// Next button goes forward iff current block validates
	$('.form-navigation .next').click(function () {
		if ($('.demo-form').parsley().validate({group: 'block-' + curIndex()})) {
			navigateTo(curIndex() + 1);
		}
	});

	// Prepare sections by setting the `data-parsley-group` attribute to 'block-0', 'block-1', etc.
	$sections.each(function (index, section) {
		$(section).find(':input').attr('data-parsley-group', 'block-' + index);
	});
	navigateTo(0); // Start at the beginning
});

$('select[name="usertype"]').on('change', function () {
	if ($(this).val() === 'Other') {
		$('#fldOtherUserType').show();
	}
	else {
		$('#fldOtherUserType').hide();
	}
});
	    </script>
	</content>
    </body>
</html>

<div class="form-section">
    <div class="col-lg-8">
	<div class="checkbox">
	    <label><input type="checkbox" name="is_funded_grant" ${user.is_funded_grant == 'on' ? 'checked' : ''}>
		    Is this request related to a funded grant? </label>
	</div>
	<div class="checkbox">
	    <label> <input type="checkbox" name="is_grant_proposal" ${user.is_grant_proposal == 'on' ? 'checked' : ''}>
		    Will the data be used to support a grant proposal?
	    </label>
	</div>
	<div class="checkbox">
	    <label> <input type="checkbox" name="is_personal_use" ${user.is_personal_use == 'on' ? 'checked' : ''}>
		    Will the data be used for teaching a class, or for personal use?
	    </label>
	</div>
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <g:if test="${useRecaptcha}">
	<div class="col-xs-6">
	    <label>Verification</label>
	    <div class="g-recaptcha" data-sitekey="${captchaSitekey}"></div>
	</div>
    </g:if>
    <div class="col-xs-12">
	<br />
    </div>
</div>

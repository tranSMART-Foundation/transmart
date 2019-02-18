<div class="form-section">
    <g:if test="${!user.email || !user.email.contains('@') || params.controller == 'user'}">
	%{--E-mail is not really an e-mail, so we need to ask for it.--}%
	<div class="col-xs-12">
	    <label for="email">E-mail<sup>*</sup></label>
	    <g:field class='form-control' name='email' required='required' type='email' value="${user.email}" />
	</div>
    </g:if>
    <g:else>
	%{--E-mail seems to be valid, so we just display it--}%
	<div class="col-xs-12">
	    <label for="email">E-mail</label>
	    <g:hiddenField name='email' value="${user.email}" />
	    <p class="form-control-static">${user.email}</p>
	</div>
    </g:else>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-6">
	<label for="firstname">First Name<sup>*</sup></label>
	<g:textField class='form-control' name='firstname' required='required' value="${user.firstname}" />
    </div>
    <div class="col-xs-6">
	<label for="lastname">Last Name<sup>*</sup></label>
	<g:textField class='form-control' name='lastname' required='required' value="${user.lastname}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-12">
	<label for="street">Street<sup>*</sup></label>
	<g:textField class='form-control' name='street' required='required' value="${user.street}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-6">
	<label for="city">City<sup>*</sup></label>
	<g:textField class='form-control' name='city' required='required' value="${user.city}" />
    </div>
    <div class="col-xs-6">
	<label for="state">State/Province<sup>*</sup></label>
	<g:textField class='form-control' name='state' required='required' value="${user.state}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-6">
	<label for="zip">ZIP/Postal Code<sup>*</sup></label>
	<g:textField class='form-control' name='zip' required='required' value="${user.zip}" />
    </div>
    <div class="col-xs-6">
	<label for="country">Country<sup>*</sup></label>
	<g:textField class='form-control' name='country' required='required' value="${user.country}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-12">
	<label for="phone">Phone<sup>*</sup></label>
	<g:field class='form-control' name='phone' required='required' value="${user.phone}" type='tel' />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-12">
	<label for="fax">Fax</label>
	<g:textField class='form-control' name='fax' value="${user.fax}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-12">
	<sup>*</sup> mandatory fields
    </div>
    <div class="col-xs-12">
	<br />
    </div>
</div>

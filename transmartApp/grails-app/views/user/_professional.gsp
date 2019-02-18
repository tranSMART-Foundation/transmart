<g:set var='usertypeOptions' value="${['Academia', 'Government', 'HealthCare Provider', 'Industry (Biotech, Pharma)',
				    'Non-Profit Research Organization', 'Patient', 'Parent of Patient', 'Patient Advocate',
				    'Patient Advocacy Organization', 'Vendor (Software Developer)', 'Other']}" />

<div class="form-section">
    <div class="col-xs-12">
	<label for="usertype">How would you best define yourself?<sup>*</sup></label>
	<g:select name='usertype' class='form-control' from="${usertypeOptions}" value="${user.usertype}"/>
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-6">
	<label for="degree">Degree<sup>*</sup></label>
	<g:textField class='form-control' name='degree' required='required' value="${user.degree}" />
    </div>
    <div class="col-xs-6">
	<label for="title">Academic Position (or Title)<sup>*</sup></label>
	<g:textField class='form-control' name='title' required='required' value="${user.title}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-6">
	<label for="institution">Institution<sup>*</sup></label>
	<g:textField class='form-control' name='institution' required='required' value="${user.institution}" />
    </div>
    <div class="col-xs-6">
	<label for="disease">Disease Interest<sup>*</sup></label>
	<g:textField class='form-control' name='disease' required='required' value="${user.disease}" />
    </div>
    <div class="col-xs-12">
	<br />
    </div>
    <div class="col-xs-12">
	<label for="about">About your research<sup>*</sup></label>
	<g:textArea name="about" required='required' placeholder='Research goal ...' class='form-control' rows='5' maxLength='600' value="${user.about}" />
	<small>Please briefly describe why you are requesting access to <b>${instanceName}</b></small>
    </div>
    <div class="col-xs-12">
	<br />
    </div>
</div>

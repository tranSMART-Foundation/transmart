<!DOCTYPE html>
<html lang="en">
    <head>
	<title>User profile</title>
	<meta name="layout" content="basic"/>
	<asset:stylesheet href="userProfile.css"/>
    </head>

    <body>
	<div class="body" style="padding-left: 15%">
	    <h1 class="menuHeader">User Profile</h1>
	    <g:if test="${flash.message}">
		<div id="flashMessage" class="message">${flash.message}</div>
	    </g:if>
	    <g:if test="${flash.error}">
		<div id="flashMessage" class="warning">${flash.error}</div>
	    </g:if>

	    <div style="min-width: 700px; max-width: 1000px; padding-top: 15px;">

		<input class="profileTab" id="tab1" type="radio" name="tabs" checked/>
		<label class="tabLabel" for="tab1">Personal</label>

		<input class="profileTab" id="tab2" type="radio" name="tabs"/>
		<label class="tabLabel" for="tab2">Professional</label>

		<input class="profileTab" id="tab3" type="radio" name="tabs"/>
		<label class="tabLabel" for="tab3">Access Level</label>

		<input class="profileTab" id="tab4" type="radio" name="tabs"/>
		<label class="tabLabel" for="tab4">IRCT Token</label>

		<section id="content1">
		    <g:form action='save'>
			<br/>
			<g:render template='/user/personal'/>
			<br/>

			<div class="buttons" style="margin-top:20px; margin-bottom:20px">
			    <span class="button">
				<input class="save" type="submit" value="Save"/>
			    </span>
			</div>
		    </g:form>
		</section>
		<section id="content2">
		    <g:form action='save'>
			<br/>
			<g:render template='/user/professional'/>
			<br/>

			<div class="buttons" style="margin-top:20px; margin-bottom:20px">
			    <span class="button">
				<input class="save" type="submit" value="Save"/>
			    </span>
			</div>
		    </g:form>
		</section>
		<section id="content3">
		    <g:render template='/user/access_level'/>
		</section>
		<section id="content4">
		    <g:if test="${level > org.transmart.plugin.custom.UserLevel.ONE}">
			<br/><textarea rows="10" style="width:100%">${token}</textarea>
		    </g:if>
		    <g:else>Token access is not available for your level of access.</g:else>
		</section>
	    </div>
	</div>
    </body>
</html>

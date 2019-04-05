<nav class="navbar navbar-default navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
			        aria-expanded="false" aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
			</button>
			<g:render template='/layouts/navbar_logo'/>
		</div>
		<div id="navbar" class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button"
					   aria-haspopup="true" aria-expanded="false">Home <span class="caret"></span></a>
					<ul class="dropdown-menu">
						<li><g:link controller='datasetExplorer'>Dataset Explorer</g:link></li>
					</ul>
				</li>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
					   aria-expanded="false">Admin <span class="caret"></span></a>
					<ul class="dropdown-menu">
						<li><g:link controller='admin' action='userlist'>User List</g:link></li>
						<li><g:link controller='admin' action='settings'>Settings</g:link></li>
						<li role="separator" class="divider"></li>
						<li><g:link controller='accessLog' action='list'>OldAdminScr.</g:link></li>
					</ul>
				</li>
				<li></li>
				<li class="dropdown">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button"
					   aria-haspopup="true" aria-expanded="false">Profile <span class="caret"></span></a>
					<ul class="dropdown-menu">
						<li><g:link controller='user'>Profile</g:link></li>
						<li role="separator" class="divider"></li>
						<li><a href="${userGuideUrl}" target="_helpWindow">User's Guide</a></li>
						<li role="separator" class="divider"></li>
						<li><g:link controller='logout'>Logout</g:link></li>
					</ul>
				</li>
			</ul>
		</div>
	</div>
</nav>

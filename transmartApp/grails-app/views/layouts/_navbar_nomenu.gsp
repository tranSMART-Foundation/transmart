<nav class="navbar navbar-default navbar-fixed-top" role="navigation" style="background-color: darkred">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
			        aria-expanded="false" aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
			</button>
			<g:render template='/layouts/navbar_logo'/>
		</div>
		<g:if test="${isTOS}">
		<div id="navbar" class="navbar-collapse collapse">
			<ul class="nav navbar-nav navbar-right">
				<g:if test="${isTOS}">
				<li><g:link controller='login' action='tos'>Terms Of Service</g:link></li>
				</g:if>
			</ul>
		</div>
		</g:if>
	</div>
</nav>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Access Log
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='accessLog'>View Access Log</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Groups
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='userGroup'>Group List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='userGroup' action='create'>Create Group</g:link>
		</span>
            </li>

            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='userGroup' action='membership'>Group Membership</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Users
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='authUser' action='list'>User List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='authUser' action='create'>Create User</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<g:if test="${!!grailsApplication.getControllerClass('export.plugin.GalaxyUserDetailsController')}">
    <g:if test="${grailsApplication.config.com.galaxy.export.galaxyEnabled}">
        <div class="navbarBox">
            <div class="navcontainer1">
                <h1 class="panelHeader">
                    Galaxy Users
                </h1>
                <ul class="navlist">
                    <li>
                        <span class="adminMenuButton">
			    <g:link class='list' controller='galaxyUserDetails'>User List</g:link>
			</span>
                    </li>
                    <li>
                        <span class="adminMenuButton">
			    <g:link class='create' controller='galaxyUserDetails' action='create'>Create User</g:link>
			</span>
                    </li>
                </ul>
            </div>
        </div>
    </g:if>
</g:if>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Access Control
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='secureObjectAccess' action='manageAccess'>Access Control by Group</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='secureObjectAccess' action='manageAccessBySecObj'>Access Control by Study</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Study
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='secureObject'>Study List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='secureObject' action='create'>Add Study</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Secure Object Paths
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='secureObjectPath'>SecureObjectPath List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='secureObjectPath' action='create'>Add SecureObjectPath</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Roles
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='role'>Role List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='role' action='create'>Create Role</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            RequestMap Setup
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='requestmap'>Requestmap List</g:link>
		</span>
            </li>
            <li>
                <span class="adminMenuButton">
		    <g:link class='create' controller='requestmap' action='create'>Requestmap Create</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

<g:if test="${!!grailsApplication.getControllerClass('ImportXnatController')}">
    <g:if test="${grailsApplication.config.org.transmart.xnatImporterEnabled}">
        <div class='navbarBox'>
            <div class="navcontainer1">
                <h1 class="panelHeader">
                    Import XNAT clinical data
                </h1>
                <ul class="navlist">
                    <li>
                        <span class="adminMenuButton">
			    <g:link class='list' controller='importXnat'>Configuration List</g:link>
			</span>
                    </li>
                    <li>
                        <span class="adminMenuButton">
			    <g:link class='create' controller='importXnat' action='create'>Create Configuration</g:link>
			</span>
                    </li>
                </ul>
            </div>
        </div>
    </g:if>
</g:if>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Package and Configuration
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton">
		    <g:link class='list' controller='configInfo'>Configuration Detail</g:link>
		</span>
                <span class="adminMenuButton">
		    <g:link class='list' controller='buildInfo'>Build Information</g:link>
		</span>
                <span class="adminMenuButton">
		    <g:link class='list' controller='statusInfo'>Status of Support Connections</g:link>
		</span>
            </li>
        </ul>
    </div>
</div>

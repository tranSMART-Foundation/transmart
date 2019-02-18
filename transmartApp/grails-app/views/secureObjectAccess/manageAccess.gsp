<html>
    <head>
	<meta name="layout" content="admin"/>
	<title>Manage Study Access</title>

	<style>
    p {
        width: 430px;
    }
    .ext-ie .x-form-text {
        position: static !important;
    }
	</style>
    </head>

    <body>
	<div class="body">
	    <h1>Manage Study Access for User/Group</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <div id="divuser" style="width:100%; font:11px tahoma, arial, helvetica, sans-serif">
		<br/><b>Search User/Group</b><br/>
		<input type="text" size="80" id="searchUsers" autocomplete="off"/>
	    </div>
	    <g:form name="accessform" action="manageAccess">
		<table>
		    <tr>
			<td>
			    <label for="accesslevelid"><b>Access Level</b></label>
			    <g:select optionKey='id' optionValue='accessLevelName' name='accesslevelid'
				      onchange='document.accessform.submit();'
				      from="${accessLevelList}" value="${accesslevelid}"/>
			    <input type="hidden" name="currentprincipalid" id="currentprincipalid" value="${principal?.id}"/>
			</td>
			<td>&nbsp;</td>
			<td>
			    <input name="searchtext" id="searchtext"/>
			    <button class="" onclick="return searchtrial.call(this);">Search Study</button>
			</td>
		    </tr>
		    <tr>
			<td>Has Access for these studies</td>
			<td></td>
			<td>Available studies:</td>
		    </tr>
		    <tr id="permissions">
			<g:render template='addremoveAccess' model="[secureObject  : secureObject,
								    soas: soas,
								    objectswithoutaccess  : objectswithoutaccess]"/>
		    </tr>
		</table>
	    </g:form>
	</div>

	<r:script>
var pageInfo = {
    basePath: '${jsContextPath}'
};

createUserSearchBox(pageInfo.basePath +
        '/userGroup/ajaxGetUsersAndGroupsSearchBoxData',
        440,
        '${jsPrincipalName}');

  function searchtrial(){
    var pid = jQuery('#currentprincipalid').val();
    if (!pid) {
	alert("Please select a user/group first");
	return false;
	}
    var form = $(this).closest('form');
        ${remoteFunction(controller: 'secureObjectAccess',
                action: 'listAccessForPrincipal',
                update: [success: 'permissions', failure: ''],
                params: "form.serialize()")};
	return false;
  }
	</r:script>
    </body>
</html>

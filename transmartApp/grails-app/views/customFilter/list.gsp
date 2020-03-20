<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
	<div id="header-div" class="header-div">
	    <g:render template='/layouts/commonheader' model="[app: 'customfilters']"/>
	</div>
	<div style="padding: 20px 10px 10px 10px;">
	    <% topicID = "1017" %>
	    <a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
		<asset:image src="help/helpbutton.jpg" alt="Help" border="0" width="18pt"
			     style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
	    </a>

	    <h1 style="font-weight:bold; font-size:10pt; padding-bottom:5px; color: #006DBA;">
		<g:if test="${params.lastFilterID != null}">
		    <g:link controller="search" action="searchCustomFilter" id="${params.lastFilterID}"
			    style="color: #006DBA;">Search</g:link>&nbsp;> Saved Filters
		</g:if>
		<g:else>
		    <g:link controller="search" style="color: #006DBA;">Search</g:link>&nbsp;> Saved Filters
		</g:else>
	    </h1>
	    <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
	    <div>
		<table>
		    <tr>
			<th>&nbsp;</th>
		    </tr>
		    <g:each in="${customFilters}" status="i" var="customFilter">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			    <td>
				<table class="rnoborder">
				    <tr>
					<td width="100%">
					    <g:link style="color:#000000;" controller="search" action="searchCustomFilter" id="${customFilter.id}">
						${fieldValue(bean: customFilter, field: 'name')}
					    </g:link>
					</td>
					<td>
					    <nobr>
						<g:link controller="search" action="searchCustomFilter" id="${customFilter.id}" class="tiny"
							style="text-decoration:underline;color:blue;font-size:11px;">select</g:link>
						<g:link action="edit" id="${customFilter.id}" class="tiny"
							style="text-decoration:underline;color:blue;font-size:11px;">edit</g:link>
						<g:link action="delete" onclick="return confirm('Are you sure?');" id="${customFilter.id}" class="tiny"
							style="text-decoration:underline;color:blue;font-size:11px;">delete</g:link>
					    </nobr>
					</td>
				    </tr>
				    <tr>
					<td colspan="2">${fieldValue(bean: customFilter, field: 'description')}</td>
				    </tr>
				    <tr>
					<td colspan="2">${customFilter.summary}</td>
				    </tr>
				    <tr>
					<td colspan="2"><b>Shortcut:</b>
					    <g:if test="${customFilter.privateFlag != 'Y'}">
						${createLink(controller: 'search', action: 'searchCustomFilter', absolute: true, id: customFilter.id)}
						${createCustomFilterEmailLink(customFilter: customFilter)}
					    </g:if>
					    <g:else>
						Private
					    </g:else>
					</td>
				    </tr>
				</table>
			    </td>
			</tr>
		    </g:each>
		</table>
	    </div>
	    <br/>
	    <a href="${createLink(controller: 'search')}" style="text-decoration:underline;color:blue;font-size:12px;">Return to Search</a>
	</div>
    </body>
</html>

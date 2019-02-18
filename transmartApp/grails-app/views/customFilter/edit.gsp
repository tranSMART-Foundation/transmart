<html>
    <body>
	<div id="header-div" class="header-div">
	    <g:render template='/layouts/commonheader' model="[app: 'customfilters']"/>
	</div>
	<div class="nav">
	    <span class="menuButton"><g:link class="list" action="list">Saved Filters</g:link></span>
	    <% topicID = "1022" %>
	    <a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
		<img src="${resource(dir: 'images', file: 'help/helpbutton.jpg')}" alt="Help" border=0 width=18pt
		     style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
	    </a>
	</div>

	<div style="padding: 20px 10px 10px 10px;">
	    <h1 style="font-weight:bold; font-size:10pt; padding-bottom:5px;">Edit Filter</h1>
	    <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	    </g:if>
	    <g:hasErrors bean="${customFilter}">
		<div class="errors">
		    <g:renderErrors bean="${customFilter}" as="list"/>
		</div>
	    </g:hasErrors>
	    <g:form>
		<input type="hidden" name="id" value="${customFilter?.id}"/>
		<input type="hidden" name="searchUserId" value="${customFilter?.searchUserId}"/>

		<div class="dialog">
		    <table>
			<tr class="prop">
			    <td valign="top" class="name"> <label>Name:</label></td>
			    <td valign="top" class="value ${hasErrors(bean: customFilter, field: 'name', 'errors')}">
				<g:textField size="80" name="name" value="${fieldValue(bean: customFilter, field: 'name')}"/>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name"> <label>Description:</label></td>
			    <td valign="top" class="value ${hasErrors(bean: customFilter, field: 'description', 'errors')}">
				<g:textArea rows="2" cols="61" name="description"
					    value="${fieldValue(bean: customFilter, field: 'description')}"/>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name"><label>Private Flag:</label></td>
			    <td valign="top" class="value ${hasErrors(bean: customFilter, field: 'privateFlag', 'errors')}">
				<g:checkBox name="privateFlag" value="${fieldValue(bean: customFilter, field: 'privateFlag') == 'Y'}"/>
			    </td>
			</tr>

			<tr class="prop">
			    <td valign="top" class="name"><label>Summary:</label></td>
			    <td valign="top">${customFilter.summary}</td>
			</tr>
		    </table>
		</div>

		<div class="buttons">
		    <g:actionSubmit class="save" value="Update"/>
		    <g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete"/>
		    <g:actionSubmit class="cancel" action="list" value="Cancel"/>
		</div>
	    </g:form>
	</div>
    </body>
</html>

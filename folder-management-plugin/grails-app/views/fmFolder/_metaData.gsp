<%@ page import='annotation.AmTagDisplayValue' %>
<%@ page import='org.transmart.biomart.ConceptCode' %>

<%
    // TODO this is crap - move logic that uses services to a taglib
    def metaDataService = grailsApplication.mainContext.metaDataService
    %>
<g:set var="metaDataService" bean="metaDataService"/>

<asset:stylesheet href="uploadDataTab.css"/>
<asset:stylesheet href="uploadData.css"/>
<script>$j = jQuery.noConflict();</script>
<asset:javascript src="jquery-plugin.js"/>
<asset:javascript src="extjs.min.js"/>
<asset:javascript src="session_time.js"/>
<asset:javascript src="uploadDataTab.js"/>
<asset:javascript src="uploadData.js"/>

<div>
    <g:each in="${metaDataTagItems}" status="i" var="amTagItem">
        <tr>
            <td valign="top" align="right" class="name">${amTagItem.displayName}&nbsp;<g:if test="${amTagItem.required}"><g:requiredIndicator/></g:if>:</td>
            <td valign="top" align="left" class="value">
                %{--FIXED: values stored in object (bio_experiment) --}%
                <g:if test="${amTagItem.tagItemType == 'FIXED'}">
                    <g:if test="${amTagItem.tagItemAttr && bioDataObject?.hasProperty(amTagItem.tagItemAttr)}" >
                        <g:if test="${amTagItem.tagItemSubtype == 'PICKLIST'}">
                            <g:select from="${org.transmart.biomart.ConceptCode.findAllByCodeTypeName(amTagItem.codeTypeName,[sort: 'codeName'])}"
                                      name="${amTagItem.tagItemAttr}" value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}"
                                      optionKey="uniqueId" optionValue="codeName"  noSelection="['':'-Select One-']"/>
                        </g:if>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'MULTIPICKLIST'}">
                            <g:set var='fValue' value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}"/>
                            <g:set var='displayValues' value="${metaDataService.getViewValues(fValue.toString())}"/>
                            <g:render template="extTagSearchField"
                                      model="[fieldName: amTagItem.tagItemAttr, codeTypeName: amTagItem.codeTypeName,
                                             searchAction: 'extSearch', searchController: 'metaData', values: displayValues]"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXT'}">
                            <g:if test="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr).length()<100}">
                                <g:textField size="100" name="${amTagItem.tagItemAttr}" value="${bioDataObject[amTagItem.tagItemAttr] ?: ''}"/>
                            </g:if>
                            <g:else>
                                <g:textArea style="width: 100%" rows="10" name="${amTagItem.tagItemAttr}" value="${bioDataObject[amTagItem.tagItemAttr] ?: ''}"/>
                            </g:else>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXTAREA'}">
                            <g:textArea style="width: 100%" rows="10" name="${amTagItem.tagItemAttr}" value="${bioDataObject[amTagItem.tagItemAttr] ?: ''}"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'DATE'}">
                            <g:textField size="100" name="${amTagItem.tagItemAttr}"
					 value="${bioDataObject[amTagItem.tagItemAttr]? formatDate(format:'yyyy-MM-dd', date:bioDataObject[amTagItem.tagItemAttr]): ''}"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'TIME'}">
                            <g:textField size="100" name="${amTagItem.tagItemAttr}"
					 value="${formatDate(format:'yyyy-MM-dd\'T\'HH:mm',
						date:bioDataObject[amTagItem.tagItemAttr] ?: new Date())}"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'CURRENTTIME'}">
                            <g:textField size="100" name="${amTagItem.tagItemAttr}"
					 value="${formatDate(format:'yyyy-MM-dd\'T\'HH:mm', date:new Date())}"/>
                        </g:elseif>
                        <g:else>ERROR -- Unrecognized tag item subtype</g:else>
                    </g:if>
                </g:if>
                %{--CUSTOM: String values only stored in amapp.am_tag_value --}%
                <g:elseif test="${amTagItem.tagItemType == 'CUSTOM'}">
                    <g:if test="${!amTagItem.editable}">not editable CUSTOM</g:if>
                    <g:else>
                        <g:if test="${folder.uniqueId && amTagItem.id}">
                            <g:set var="tagValues" value="${annotation.AmTagDisplayValue.findAllDisplayValue(folder.uniqueId, amTagItem.id)}"/>
                        </g:if>
                        <g:if test="${amTagItem.tagItemSubtype == 'FREETEXT'}">
                            <g:if test="${(tagValues ? tagValues[0].displayValue : '')?.length()<100}">
                                <g:textField size="100" name="amTagItem_${amTagItem.id}"  value="${tagValues ? tagValues[0].displayValue : ''}"/>
                            </g:if>
                            <g:else>
                                <g:textArea size="100" cols="74" rows="10" name="amTagItem_${amTagItem.id}" value="${tagValues ? tagValues[0].displayValue : ''}"/>
                            </g:else>
                        </g:if>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXTAREA'}">
                            <g:textArea size="100" cols="74" rows="10" name="amTagItem_${amTagItem.id}" value="${tagValues ? tagValues[0].displayValue : ''}"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'PICKLIST'}">
                            <g:select from="${org.transmart.biomart.ConceptCode.findAllByCodeTypeName(amTagItem.codeTypeName, [sort: 'codeName'])}"
                                      name="amTagItem_${amTagItem.id}" value="${tagValues ? tagValues[0].objectUid : ''}"
                                      optionKey="uniqueId" optionValue="codeName" noSelection="['':'-Select One-']"/>
                        </g:elseif>
                        <g:elseif test="${amTagItem.tagItemSubtype == 'MULTIPICKLIST'}">
                            <g:render template="extTagSearchField"
                                      model="[fieldName: 'amTagItem_'+amTagItem.id, codeTypeName: amTagItem.codeTypeName,
					     searchAction: 'extSearch', searchController: 'metaData', values: tagValues]"/>
                        </g:elseif>
                    </g:else>
                </g:elseif>
                %{--OTHER: values with special search processing defined e.g. PROGAM_TARGET, BIO_DISEASE, PLATFORMs --}%
                <g:else>
                    <g:if test="${folder.uniqueId && amTagItem.id}">
                        <g:set var="tagValues" value="${annotation.AmTagDisplayValue.findAllDisplayValue(folder.uniqueId, amTagItem.id)}"/>
                    </g:if>
                    <g:if test="${amTagItem.tagItemSubtype == 'COMPOUNDPICKLIST'}">
                        <g:render template="${amTagItem.guiHandler}"
                                  model="[measurements: measurements, technologies: technologies, vendors: vendors, platforms: platforms,
                                         fieldName: 'amTagItem_' + amTagItem.id, searchAction: amTagItem.guiHandler + 'Search',
                                         searchController: 'metaData', values: tagValues]"/>
                    </g:if>
                    <g:else>
                        <g:render template="extBusinessObjSearch"
                                  model="[fieldName: 'amTagItem_' + amTagItem.id, searchAction: amTagItem.guiHandler + 'Search',
                                         searchController: 'metaData', values: tagValues]"/>
		    </g:else>
		</g:else>
	    </td>
	</tr>
    </g:each>
</div>


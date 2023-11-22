<div style="text-align:center;">
    <div class="welcome"
         style="margin:40px auto; background:#F4F4F4; border:1px solid #DDD; padding:20px; width:80%; text-align:center; border-top-left-radius:20px; border-bottom-right-radius:20px">
        <g:set var="transmartIntro" value="${grailsApplication.config?.com?.recomdata?.welcome?.intro}"/>
        <g:set var="transmartSummary" value="${grailsApplication.config?.com?.recomdata?.welcome?.summary}"/>
        <g:set var="projectName" value="${grailsApplication.config?.com?.recomdata?.projectName}"/>
        <g:set var="projectLogo" value="${grailsApplication.config?.com?.recomdata?.projectLogo}"/>
        <g:set var="projectURL" value="${grailsApplication.config?.com?.recomdata?.projectURL}"/>
        <g:set var="providerName" value="${grailsApplication.config?.com?.recomdata?.providerName}"/>
        <g:set var="providerLogo" value="${grailsApplication.config?.com?.recomdata?.providerLogo}"/>
        <g:set var="providerURL" value="${grailsApplication.config?.com?.recomdata?.providerURL}"/>
        <g:set var="providerNewline" value="${grailsApplication.config?.com?.recomdata?.providerNewline}"/>
        <p><b>Welcome to tranSMART <g:if test="${projectName}">for ${projectName}</g:if></b></p>
	
	<g:if test="${transmartIntro}">
	    <p>Intro:</p>${transmartIntro}
	</g:if>
	<g:else>
            <p>The <b>Browse</b> window lets you search and dive into the information contained in tranSMART,
		including Programs, Studies, Assays and the associated Analyses Results, Subject Level Data and Raw Files.
		This is also the location to export files stored in tranSMART. Note: to edit the Program, Study, or Assay
		information, you must be logged in as an Administrator.
            </p>
	    
            <p>The <b>Analyze</b> window lets you perform a number of analyses either on studies selected
		in the Browse window, or from the global search box located in the top ribbon of your screen.
		More information about the analyses you can perform is available in the “Help" section of the "Utilities" menu.
            </p>
	</g:else>

	<g:if test="${transmartSummary}">
	    <br/>${transmartSummary}
	</g:if>

        <br/><br/>
    </div>

    <g:if test='${grailsApplication?.config?.com?.recomdata?.guestAutoLogin}'>
	<g:if test='${grailsApplication?.config?.com?.recomdata?.motd}'>
	    <div style="margin-right:auto; margin-left:auto; width:80%;">
		<div class="x-box-tl">
		    <div class="x-box-tr">
			<div class="x-box-tc">
			</div>
		    </div>
		</div>
		<div class="x-box-ml">
		    <div class="x-box-mr">
			<div class="x-box-mc" style="text-align:left">
			    <br />
			    <g:if test='${grailsApplication.config.com.recomdata.motd.motd_title}' >
				<h3 class='motd-title'>
				    ${grailsApplication.config.com.recomdata.motd.motd_title}
				</h3>
			    </g:if>
			    <g:if test='${grailsApplication.config.com.recomdata.motd.motd_text}' >
				<div class='motd-text'>
				    ${grailsApplication.config.com.recomdata.motd.motd_text}
				</div>
			    </g:if>
			    <br />
			</div>
		    </div>
		</div>
		<div class="x-box-bl">
		    <div class="x-box-br">
			<div class="x-box-bc">
			</div>
		    </div>
		</div>
		<br/><br/>
	    </div>
	</g:if>

	<div>
	    <g:if test="${projectName || providerName}">
		<g:if test="${projectName}">
		    <g:if test="${projectLogo}">
			<a id="projectpowered" target="_blank" <g:if test="projectURL">href="${projectURL}"</g:if> style="text-decoration:none;">
			    <img src="${projectLogo}" alt="${projectName}" style="height:35px; vertical-align:middle; margin-bottom:12px;">
			</a>
		    </g:if>
		    <g:else>
			<a id="projectpowered" target="_blank" <g:if test="projectURL">href="${projectURL}"</g:if> style="text-decoration:none;">
			    <span style="font-size:10px; display:inline-block; line-height:35px; height:35px;">${projectName}</span>
			</a>
		    </g:else>
		</g:if>
		<g:if test="${projectName && providerName}">
		    <g:if test="${providerNewline == true}">
			<br/><br/>
		    </g:if>
		    <g:else>
			<span style="font-size:20px; display:inline-block; line-height: 35px; height: 35px;">&nbsp;+&nbsp;</span>
		    </g:else>
		</g:if>
		<g:if test="${providerNewline == true}">
		    <br/><br/>
		</g:if>
		<g:if test="${providerName}">
		    <g:if test="${providerLogo}">
			<a id="providerpowered" target="_blank" <g:if test="providerURL">href="${providerURL}"</g:if> style="text-decoration:none;">
                            <img src="${providerLogo}" alt="${providerName}" style="height:35px; vertical-align:middle; margin-bottom:12px;">
			</a>
		    </g:if>
		    <g:else>
			<a id="providerpowered" target="_blank" <g:if test="providerURL">href="${providerURL}"</g:if> style="text-decoration:none;">
			    <span style="font-size:10px; display:inline-block; line-height:35px; height:35px;">${providerName}</span>
			</a>
		    </g:else>
		</g:if>
		<br/><br/>
	    </g:if>
        </div>
    </g:if>

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <div style="margin:auto; padding:0px 16px 16px 16px; border-radius:8px; border:1px solid #DDD; width:20%">
            <h4>Admin Tools</h4>
            <span class="greybutton buttonicon addprogram">Add new program</span>
        </div>
    </sec:ifAnyGranted>

</div>

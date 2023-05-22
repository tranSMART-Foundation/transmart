<h2>Marker Selection - Heatmap</h2>

<p>
    <div class="plot_hint">
        <i>Click on the heatmap image to open it in a new window as this may increase readability.</i>
	<br/><br/>
    </div>

    <g:each var='location' in="${imageLocations}">
        <g:logMsg>markerselection_out getContextPath '${request.getContextPath()}' location '${location}'</g:logMsg>
        <a onclick="window.open('${request.getContextPath()}${location}', '_blank')">
            <img src="${request.getContextPath()}${location}" class='img-result-size' />
        </a>
    </g:each>

    <div>
        <span class='AnalysisHeader'>Table of top Markers</span>
        <g:if test="${grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable}">
            &nbsp;<g:metacoreSettingsButton/>
	    <input type="button" value="Run MetaCore Enrichment Analysis" onClick="markerSelectionRunMetacoreEnrichment();"/>
        </g:if>
        <br />
        <g:if test="${grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable}">
            <g:metacoreEnrichmentResult/>
        </g:if>
    </div>

    ${markerSelectionTable}

    <g:if test="${zipLink}">
        <a class='AnalysisLink' class='downloadLink' href="${request.getContextPath()}${zipLink}">Download raw R data</a>
    </g:if>
</p>

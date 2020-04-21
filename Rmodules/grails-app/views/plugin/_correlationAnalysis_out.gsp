<h2>Correlation Table (p-values on top right half, correlation coefficient on bottom left)</h2>

<p>
    <p>${correlationData}</p>

    <g:each var='location' in="${imageLocations}">
        <img src="${request.getContextPath()}${location}"/>
    </g:each>

    <g:if test="${zipLink}">
        <a class='AnalysisLink' class='downloadLink' href="${request.getContextPath()}${zipLink}">Download raw R data</a>
    </g:if>
</p>

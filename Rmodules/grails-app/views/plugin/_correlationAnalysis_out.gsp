<h2>Correlation Table (p-values on top right half, correlation coefficient on bottom left)</h2>

<p>
    <p>${correlationData}</p>

    <g:each var='location' in="${imageLocations}">
        <img src="${request.getContextPath()}${location}"/>
    </g:each>

    <g:render template="/plugin/downloadRawDataLink" />
</p>

<h2>Line Graph</h2>

<p>
    <g:each var='location' in="${imageLocations}">
        <img src="${request.getContextPath()}${location}" class='img-result-size'/> <br/>
    </g:each>

    <g:render template="/plugin/downloadRawDataLink" />
</p>

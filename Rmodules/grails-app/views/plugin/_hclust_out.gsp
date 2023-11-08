<h2>Heatmap</h2>

<p>
    <div class="plot_hint">
	<i>Click on the heatmap image to open it in a new window as this may increase readability.</i>
	<br/><br/>
    </div>

    <g:each var='location' in="${imageLocations}">
	<a onclick="window.open('${request.getContextPath()}${location}', '_blank')">
            <img src="${request.getContextPath()}${location}" class='img-result-size'/>
	</a>
    </g:each>

    <g:render template="/plugin/downloadRawDataLink" />
</p>

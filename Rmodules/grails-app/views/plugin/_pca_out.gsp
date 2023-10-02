<h2>Component Summary</h2>

<g:logMsg>PCA summaryTable ${summaryTable}</g:logMsg>

${summaryTable}

<br/>

<g:logMsg>PCA getContextPath ${request.getContextPath()}</g:logMsg>
<g:logMsg>PCA imageLocations ${imageLocations}</g:logMsg>

<g:each var='location' in="${imageLocations}">
    <g:logMsg>PCA locations ${location}</g:logMsg>
    <img src="${request.getContextPath()}${location}"/> <br/>
</g:each>

<br/>

<h2>Gene list by proximity to Component</h2>

<g:logMsg>PCA genelistTable ${genelistTable}</g:logMsg>

${geneListTable}

<br/>
    <g:logMsg>PCA ziplink ${ziplink}</g:logMsg>

<g:render template="/plugin/downloadRawDataLink" />

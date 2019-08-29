<form>
    <g:if test="${previewData instanceof List}">
        <h3>Preview result (at most ${previewRowsCount} rows):</h3>
        <table class="analysis">
            <tr>
                <g:each in="${previewData[0]}">
                    <th>${it}</th>
                </g:each>
            </tr>
            <g:each in="${previewData.drop(1)}">
                <tr>
                    <g:each in="${it}">
                        <td>${it}</td>
                    </g:each>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <pre>${previewData}</pre>
    </g:else>
    <g:if test="${zipLink != null && zipLink.size() > 0}">
        <br />
        <a class='AnalysisLink' href="${request.getContextPath()}${zipLink}" class="downloadLink">Download PLINK results</a>
    </g:if>
</form>

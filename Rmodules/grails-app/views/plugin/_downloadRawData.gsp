<g:if test="${grailsApplication.config?.ui?.tabs?.datasetExplorer?.rawDataExport?.enabled && zipLink}">
    <a class='AnalysisLink' class='downloadLink' href="${resource(file: zipLink)}">Download raw R data</a>
</g:if>
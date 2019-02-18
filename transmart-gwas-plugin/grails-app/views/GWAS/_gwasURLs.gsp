<script type="text/javascript" charset="utf-8">
    var gwasRegionBrowseWindow = "${createLink(controller: 'gwasSearch', action: 'getRegionFilter')}";			 // added from 18.1
    var gwasEqtlTranscriptGeneWindow = "${createLink(controller: 'gwasSearch', action: 'getEqtlTranscriptGeneFilter')}"; // added from 18.1
    var gwasTreeURL = "${createLink(controller: 'GWAS', action: 'getDynatree')}";
    var gwasGetQQPlotURL = "${createLink(controller: 'gwasSearch', action: 'getQQPlotImage')}";
    var gwasWebStartURL = "${createLink(controller: 'gwasSearch', action: 'webStartPlotter')}";
    var gwasGetTableDataURL = "${createLink(controller: 'gwasSearch', action: 'getTableResults')}";
    var gwasGetAnalysisDataURL = "${createLink(controller: 'gwasSearch', action: 'getAnalysisResults')}";
    var gwasExportAnalysisURL = "${createLink(controller: 'gwasSearch', action: 'exportAnalysis')}";
    var gwasGetManhattanPlotUrl = "${createLink(controller: 'gwasSearch', action: 'getManhattanPlotImage')}";
</script>

modules = {
    smartR_base {
        resource url: [plugin: 'smart-r', dir: 'css', file: 'smartR.css']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-2.1.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-ui-1.11.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery.tablesorter.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'd3.min.js']
    }

    smartR_heatmap {
        dependsOn 'smartR_base'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'Heatmap.css']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jsrender.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'extjs-helper.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'd3Heatmap.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'bioMarkersModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapValidator.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapService.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapView.js']
    }

    smartR_analysis_common {
        dependsOn 'smartR_base'
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisInit.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'ajaxServices.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'tabAnalysisView.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common/components', file: 'conceptBox.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common/components', file: 'conceptBoxCollection.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common/components', file: 'executionStatus.js']
    }

    smartR_boxplot {
        dependsOn 'smartR_analysis_common'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'Boxplot.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'd3Boxplot.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotController.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotView.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplot.js']
    }
}

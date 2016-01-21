modules = {
    smartR_base {
        resource url: [plugin: 'smart-r', dir: 'css', file: 'smartR.css']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-2.1.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-ui-1.11.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery.tablesorter.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'd3.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'smartR.js']
//        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'polyfill.min.js']
    }

    smartR_heatmap {
        dependsOn 'smartR_base'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'Heatmap.css']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jsrender.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'extjs-helper.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'bioMarkersModel.js']

        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapValidator.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapService.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapView.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'd3Heatmap.js']
    }

    smartR_analysis_common {
        dependsOn 'smartR_base'
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisInit.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'ajaxServices.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'tabAnalysisView.js']
    }

    smartR_boxplot {
        dependsOn 'smartR_analysis_common'
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotController.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Boxplot', file: 'boxplotView.js']
    }
}

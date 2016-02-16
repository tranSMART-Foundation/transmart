modules = {
    smartR_core {
        resource url: [plugin: 'smart-r', dir: 'css', file: 'smartR.css']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'd3.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-2.1.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery-ui-1.11.4.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jquery.tablesorter.min.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jsrender.js']
    }

    smartR_angular_components {
        dependsOn 'angular'
        // app initializer
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular', file: 'smartRApp.js']
        // directives
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'conceptBox.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'tabContainer.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'workflowTab.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'fetchButton.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'runButton.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'ngTranscludeReplace.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/directives', file: 'summaryStatistics.js']
        // services
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/services', file: 'rServeService.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/services', file: 'smartRUtils.js']
    }

    smartR_heatmap_bak { // FIXME: port to angular JS framework (see below)
        dependsOn 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'heatmap.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisInit.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'ajaxServices.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'd3Heatmap.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_common', file: 'analysisInit.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'extjs-helper.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'bioMarkersModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapValidator.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapService.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/Heatmap', file: 'heatmapView.js']
    }

    smartR_heatmap {
        dependsOn 'smartR_angular_components', 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'heatmap.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/controllers', file: 'heatmap.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/viz', file: 'd3Heatmap.js']
    }

    smartR_correlation {
        dependsOn 'smartR_angular_components', 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'correlation.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/controllers', file: 'correlation.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/viz', file: 'd3Correlation.js']
    }

    smartR_boxplot {
        dependsOn 'smartR_angular_components', 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'boxplot.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/controllers', file: 'boxplot.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/viz', file: 'd3Boxplot.js']
    }

    smartR_timeline {
        dependsOn 'smartR_angular_components', 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'timeline.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/controllers', file: 'timeline.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/viz', file: 'd3Timeline.js']
    }

    smartR_volcanoplot {
        dependsOn 'smartR_angular_components', 'smartR_core'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'volcanoplot.css']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/controllers', file: 'volcanoplot.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR/_angular/viz', file: 'd3Volcanoplot.js']
    }
}

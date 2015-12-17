modules = {
    smartR_base {
        // we get the definitions from transmartApp
        // dependsOn 'jquery'
        resource url: [plugin: 'smart-r', dir: 'css', file: 'smartR.css']
    }

    smartR_heatmap {
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'd3.js']
        resource url: [plugin: 'smart-r', dir: 'js/resource', file: 'jsrender.js']

        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'd3Heatmap.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'extjs-helper.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'bioMarkersModel.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'heatmapService.js']
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'heatmapView.js']
    }
}

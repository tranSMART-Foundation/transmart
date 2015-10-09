modules = {
    // plugin needs to be declared when rmodules is used inline
    // see GPRESOURCES-176
    heatmap {
        resource url: [plugin: 'smart-r', dir: 'js/smartR', file: 'heatmap.js']
    }
}

modules = {
    gwasTab {
        /* dependencies are defined in transmartApp... */
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        /* and files in transmartApp */
        resource url: 'css/main.css',                                 disposition: 'head'
        resource url: 'js/facetedSearch/facetedSearchBrowse.js',      disposition: 'head'
        resource url: 'js/maintabpanel.js',                           disposition: 'head'
        resource url: 'js/datasetExplorer/protovis/protovis-r3.2.js', disposition: 'head'
        resource url: 'js/help/D2H_ctxt.js',                          disposition: 'head'

/8        resource url: [plugin: 'transmart-gwas', dir: 'js',  file: 'gwas.js'],  disposition: 'head'*/
        resource url: [plugin: 'transmart-gwas', dir: 'css', file: 'gwas.css'], disposition: 'head'

        // must come after gwas.css because it overrides directives there (!)
        resource url: 'css/sanofi.css', disposition: 'head'
    }
}

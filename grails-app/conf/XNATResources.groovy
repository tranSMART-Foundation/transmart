modules = {
    xnat {
        // This should only be depending on jQuery and jQueryUI
        // But a bug in the resources plugin imposes us to depend on all plugins of the module we overwrite
        // dependsOn 'jquery', 'jquery-ui'
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        resource url: [plugin: 'xnat-plugin', dir: 'js/', file: 'xnat.js'], disposition: 'defer'
        resource url: [plugin: 'xnat-plugin', dir: 'js/', file: 'xnatImage.js'], disposition: 'defer'
        resource url: [plugin: 'xnat-plugin', dir: 'css/', file: 'xnat.css'], disposition: 'head'

    }

    overrides {
        analyseTab {
            dependsOn 'xnat'

            println("Configuring XNAT resources ...")
        }
    }
}

package smartR.plugin

import grails.util.Environment

class SmartRService {
    /**
     *   Gets the directory where all the R scripts are located
     *
     *   @return {str}: path to the script folder
     */
    def getWebAppFolder() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils
                    .getPluginDirForName('smart-r')
                    .getFile()
                    .absolutePath + '/web-app/'
        } else {
            return grailsApplication
                    .mainContext
                    .servletContext
                    .getRealPath('/plugins/') + '/smart-r-0.1/'
        }
    }

    String getScriptsFolder(){
        def scriptsFolderName = "HeimScripts"
        return "${webAppFolder}/${scriptsFolderName}/"
    }

    def getScriptList() {
        def dir = getScriptsFolder()
        return new File(dir).list().findAll { it != 'Wrapper.R' && it != 'Sample.R' }
    }

}

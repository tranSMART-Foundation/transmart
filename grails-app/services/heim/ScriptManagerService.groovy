package heim

import grails.util.Environment

/**
 * Created by piotrzakrzewski on 06/10/15.
 */
class ScriptManagerService {

    String readScript(String workflow, String name) {
        def scriptPath = scriptsFolder + workflow + "/" + name
        return new File(scriptPath).getText('UTF-8')
    }

    def listWorkflows() {
        def workflowNames = []
        new File(scriptsFolder).eachFile { file ->
            if (file.isDirectory()) {
                String name = file.name
                workflowNames.add(name)
            }
        }
        return workflowNames
    }

    def getWebAppFolder() {
        if (Environment.current != Environment.PRODUCTION) {
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

    String getScriptsFolder() {
        def scriptsFolderName = "HeimScripts"
        return "${webAppFolder}/${scriptsFolderName}/"
    }


}

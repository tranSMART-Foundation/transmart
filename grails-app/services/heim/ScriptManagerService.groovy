package heim

import grails.util.Environment
import org.rosuda.REngine.Rserve.RConnection

/**
 * Created by piotrzakrzewski on 06/10/15.
 */
class ScriptManagerService {

    public static String R_SCRIPTS_HOME = "/web-app/HeimScripts/"

    public static String readScript(String workflow, String name) {
        def execDir = getAbsoluteScriptDirectory()
        def scriptPath = execDir + workflow + "/" + name
        return new File(scriptPath).getText('UTF-8')
    }


    private static String getAbsoluteScriptDirectory(){
        def execDir = new File("../SmartR").getAbsolutePath() //System.getProperty("user.dir")
        return execDir+R_SCRIPTS_HOME
    }

    public static String initializeWorkflow(String workflowName){
        executeRScript(workflowName,"init.r")
    }

    public static String runWorkflow(String workflowName){
        executeRScript(workflowName,"run.r")
    }

    public static String executeRScript(String workflowName, String scriptName){
        def rServePort = 6311 //TODO:Piotr Fetch it from the config
        def rServeHost = "localhost"
        def connection = new RConnection(rServeHost, rServePort)
        def script = ScriptManagerService.readScript(workflowName, scriptName)
        return connection.eval(script).asString()
    }

    public static listWorkflows(){
        def workflowNames = []
        new File(getAbsoluteScriptDirectory()).eachFile { file->
            if(file.isDirectory()){
                String name = file.name
                workflowNames.add(name)
            }
        }
        return workflowNames
    }

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


}

package heim

import org.rosuda.REngine.Rserve.RConnection

/**
 * Created by piotrzakrzewski on 06/10/15.
 */
class ScriptManager {

    public static String R_SCRIPTS_HOME = "/web-app/HeimScripts/"

    public static String readScript(String workflow, String name) {
        def execDir = getAbsoluteScriptDirectory()
        def scriptPath = execDir + workflow + "/" + name
        return new File(scriptPath).getText('UTF-8')
    }


    private static String getAbsoluteScriptDirectory(){
        def execDir = System.getProperty("user.dir")
        return execDir+R_SCRIPTS_HOME
    }

    public static String initializeWorkflow(String workflowName){
        executeRScript(workflowName,"init.r")
    }

    public static String runWorkflow(String workflowName){
        executeRScript(workflowName,"run.r")
    }

    private static String executeRScript(String workflowName, String scriptName){
        def rServePort = 6311 //TODO:Piotr Fetch it from the config
        def rServeHost = "localhost"
        def connection = new RConnection(rServeHost, rServePort)
        def script = ScriptManager.readScript(workflowName, scriptName)
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


}

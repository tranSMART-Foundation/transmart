package heim

import org.rosuda.REngine.Rserve.RConnection

/**
 * Created by piotrzakrzewski on 08/10/15.
 */
class RScriptOutputManager {

    def RScriptOutputManager(RConnection conn, String sessionId,String scriptExecutionId) {
        this.conn = conn
        this.outputPath = assignStorage(sessionId,scriptExecutionId)
        initOutputDir(sessionId,scriptExecutionId)
        downloadScriptOutput()
    }

    RConnection conn
    String outputPath
    String basedir = "/tmp/heim/"

    def initOutputDir(String dirname,String scriptExecIdDir){
        new File(basedir).mkdir()
        new File("${basedir}"+dirname).mkdir()
        new File("${basedir}"+dirname+"/${scriptExecIdDir}").mkdir()
    }

    def listFiles() {
        conn.eval("list.files()").asNativeJavaObject()
    }

    def assignStorage(String sessionId,String scriptExecutionId){
        String path = "${basedir}${sessionId}/${scriptExecutionId}/"
        return  path
    }

    def downloadFromRserve(String name) {
        def serverStream = conn.openFile(name)
        byte[] buffer = new byte[8092]
        def anythingLeft = serverStream.read(buffer)
        def fos = new FileOutputStream(outputPath+name)
        fos << buffer
        while (anythingLeft > -1) {
            anythingLeft = serverStream.read(buffer)
            fos << buffer
        }
        fos.close()
    }

    def downloadScriptOutput(){
        def files = listFiles()
        for (file in files){
            downloadFromRserve(file)
        }
    }


    def getScriptOutput(){
        def files = []
        def scriptOutput = new File(outputPath).listFiles()
        if (scriptOutput){
            files = scriptOutput
        }
        return files
    }

}

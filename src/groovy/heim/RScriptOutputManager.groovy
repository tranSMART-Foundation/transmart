package heim

import org.rosuda.REngine.Rserve.RConnection

/**
 * Created by piotrzakrzewski on 08/10/15.
 */
class RScriptOutputManager {

    def RScriptOutputManager(RConnection conn, File directory) {
        this.conn = conn
        this.directory = directory
    }

    RConnection conn
    File directory

    def listFiles() {
        conn.eval("list.files()").asNativeJavaObject()
    }

    def downloadFromRserve(String name) {
        def serverStream = conn.openFile(name)
        byte[] buffer = new byte[8092]
        def anythingLeft = serverStream.read(buffer)
        def fos = FileOutputStream(name)
        fos << buffer
        while (anythingLeft) {
            anythingLeft = serverStream.read(buffer)
            fos << buffer
        }
    }

    def downloadScriptOutput(){
        def files = listFiles()
        for (file in files){
            downloadFromRserve(file)
        }
    }


}

package heim

import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SmartRRuntimeConstants {

    public static getInstance() {
        Holders.applicationContext.getBean(SmartRRuntimeConstants)
    }

    @Autowired
    private GrailsApplication grailsApplication

    File pluginScriptDirectory

    File legacyScriptDirectory

    void setPluginScriptDirectory(File dir) {
        assert dir.isDirectory()
        this.pluginScriptDirectory = dir.absoluteFile
    }

    void setLegacyScriptDirectory(File dir) {
        assert dir.isDirectory()
        this.legacyScriptDirectory = dir.absoluteFile
    }

    void setRemoteScriptDirectoryDir(Object dir) {
        grailsApplication.config.smartR.remoteScriptDirectory = dir as File
        remoteScriptDirectoryDir // for the side effects
    }

    /**
     * Where to copy the R scripts to and execute them from
     * (in the machine where Rserve is running).
     */
    File getRemoteScriptDirectoryDir() {
        def dir = grailsApplication.config.smartR.remoteScriptDirectory as File
        if (!dir.absolute) {
            throw new RuntimeException("Invalid configuration: " +
                    "smartR.remoteScriptDirectory should be an absolute path," +
                    " got '$dir'")
        }
        dir
    }

}

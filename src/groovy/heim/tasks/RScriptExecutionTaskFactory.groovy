package heim.tasks

import grails.util.Holders
import heim.jobs.JobInstance
import heim.rserve.GenericJavaObjectAsJsonRFunctionArg
import heim.rserve.RFunctionArg
import heim.rserve.RServeSession
import heim.session.SessionFiles
import heim.session.SmartRSessionScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.InvalidArgumentsException

/**
 * Created by glopes on 09-10-2015.
 */
@Component
@SmartRSessionScope
class RScriptExecutionTaskFactory implements TaskFactory {

    @Autowired
    private RServeSession rServeSession

    @Autowired
    private JobInstance jobInstance

    @Autowired
    private SessionFiles sessionFiles

    @Value('#{sessionId}')
    private UUID sessionId

    final int order = Ordered.LOWEST_PRECEDENCE

    @Override
    boolean handles(String taskName, Map<String, Object> argument) {
        true // fallback factory, since it has the lowest precedence
    }

    private String readScript(String taskType) {
        File dir = Holders.config.smartR.pluginScriptDirectory
        assert dir != null
        File workflowDir = new File(dir, jobInstance.workflow)
        def file = new File(workflowDir, taskType + '.r')
        if (file.parentFile != workflowDir) {
            throw new InvalidArgumentsException(
                    'Invalid task type name (probably contains /)')
        }

        file.text
    }

    @Override
    Task createTask(String taskName, Map<String, Object> arguments) {
        try {
            String codeToLoad = readScript(taskName)

            new RScriptExecutionTask(
                    sessionFiles:  sessionFiles,
                    sessionId:     sessionId,
                    rServeSession: rServeSession,
                    codeToLoad:    codeToLoad,
                    arguments:     convertArguments(arguments),
            )
        } catch (IOException ioe) {
            throw new InvalidArgumentsException("Bad script '$taskName' for " +
                    "workflow '${jobInstance.workflow}'", ioe)
        }
    }

    List<RFunctionArg> convertArguments(Map<String, Object> arguments) {
        arguments.collect { k, v ->
            new GenericJavaObjectAsJsonRFunctionArg(
                    name: k,
                    object: v)
        }
    }
}

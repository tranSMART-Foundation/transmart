package smartR.plugin.rest

import grails.converters.JSON
import grails.validation.Validateable
import heim.session.SessionService
import heim.tasks.TaskResult

class ScriptExecutionController {

    static scope = 'request'

    SessionService sessionService
    def sendFileService

    static allowedMethods = [
            init:         'POST',
            run:          'POST',
            status:       'GET',
            downloadFile: 'GET',
    ]

    def run(RunCommand runCommand) {
        UUID executionId =
                sessionService.createTask(
                        runCommand.arguments,
                        runCommand.sessionId,
                        runCommand.taskType,)

        render(contentType: 'text/json') {
            [executionId: executionId.toString()]
        }
    }

    def status(StatusCommand statusCommand) {
        Map status = sessionService.getTaskData(
                statusCommand.sessionId,
                statusCommand.executionId,
                statusCommand.waitForCompletion)

        TaskResult res = status.result
        String exceptionMessage
        if (res.exception) {
            exceptionMessage = res.exception.getClass().toString() +
                    ': ' + res.exception.message
        }

        render(contentType: 'text/json') {
            [
                    state: status.state.toString(),
                    result: [
                            successful: res.successful,
                            exception: exceptionMessage,
                            artifacts: res.artifacts,
                    ]
            ]
        }
    }

    def downloadFile(DownloadCommand downloadCommand) {
        sessionService.getFile(
                downloadCommand.sessionId,
                downloadCommand.executionId,
                downloadCommand.filename)

        File selectedFile = sessionService.getFile(
                downloadCommand.sessionId,
                downloadCommand.executionId,
                downloadCommand.filename)

        sendFileService.sendFile servletContext, request, response, selectedFile
    }
}

@Validateable
class StatusCommand {
    UUID sessionId
    UUID executionId
    boolean waitForCompletion

    static constraints = {
        sessionId   blank: false
        executionId blank: false
    }
}

@Validateable
class RunCommand {
    UUID sessionId
    Map arguments = [:]
    String taskType

    static constraints = {
        sessionId blank: false
        taskType  blank: false
    }
}

@Validateable
class DownloadCommand {
    UUID sessionId
    UUID executionId
    String filename

    static constraints = {
        sessionId   blank: false
        executionId blank: false
        filename    blank: false
    }
}

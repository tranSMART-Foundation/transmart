package jobs.steps

import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import jobs.UserParameters

@Slf4j('logger')
class ParametersFileStep implements Step{

    File temporaryDirectory
    UserParameters params

    final String statusName = 'Writing parameters'

    @Override
    void execute() {
	logger.debug 'execute temporaryDirectory {}', temporaryDirectory
        File jobInfoFile = new File(temporaryDirectory, 'jobInfo.txt')

        jobInfoFile.withWriter { BufferedWriter writer ->
            writer.writeLine 'Parameters'
            params.each { key, value ->
		logger.debug 'param "{}" => "{}"', key, value
		writer.writeLine "\t$key -> $value"
	    }
        }

        new File(temporaryDirectory, 'request.json') << JsonOutput.prettyPrint(params.toJSON())
    }
}

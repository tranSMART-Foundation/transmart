package jobs.steps

import groovy.transform.CompileStatic

@CompileStatic
class WriteFileStep implements Step {

    File temporaryDirectory
    String fileName
    String fileContent

    String getStatusName() {
        if (sufficientInformationProvided) {
            'Writing ' + fileName + ' file'
        }
    }

    void execute() {
        if (sufficientInformationProvided) {
            new File(temporaryDirectory, fileName).text = fileContent
        }
    }

    private boolean isSufficientInformationProvided() {
        temporaryDirectory && fileName && fileContent
    }

}

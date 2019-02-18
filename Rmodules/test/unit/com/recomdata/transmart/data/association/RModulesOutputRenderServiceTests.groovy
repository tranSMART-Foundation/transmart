package com.recomdata.transmart.data.association

import com.recomdata.transmart.util.ZipService
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.equalTo

@TestFor(RModulesOutputRenderService)
class RModulesOutputRenderServiceTests {

    private static final String USER_NAME = 'user'
    private static final String FILE_CONTENTS = 'file contents\n'
    private static final String ANALYSIS_NAME = "$USER_NAME-Analysis-100"
    private static final String WORKING_DIRECTORY = 'workingDirectory'

    File temporaryDirectory
    File analysisDirectory
    File workingDirectory

    @Before
    void before() {
        temporaryDirectory = File.createTempDir('analysis_file_test', '')
        grailsApplication.config.RModules.tempFolderDirectory = service.tempFolderDirectory =
	    temporaryDirectory.absolutePath + '/'

        analysisDirectory = new File(temporaryDirectory, ANALYSIS_NAME)
	assert analysisDirectory.mkdirs()
	analysisDirectory.deleteOnExit()

        workingDirectory = new File(analysisDirectory, WORKING_DIRECTORY)
	assert workingDirectory.mkdirs()
	workingDirectory.deleteOnExit()

	service.asyncJobService = [isUserAllowedToExportResults: { currentUserBean, jobName -> true }]
	service.zipService = new ZipService()

        createDummyFile workingDirectory, 'Heatmap&*.png'
        createDummyFile workingDirectory, 'Heatmap.svg'
        createDummyFile workingDirectory, 'jobInfo.txt'
        createDummyFile workingDirectory, 'outputfile.txt'
        createDummyFile workingDirectory, 'request.json'
    }

    void createDummyFile(File directory, String fileName) {
 	File file = new File(directory, fileName)
	file.deleteOnExit()
	file << FILE_CONTENTS
    }

    @Test
    void testInitializeAttributes() {
        List<String> imageLinks = []
        service.initializeAttributes(ANALYSIS_NAME, 'Analysis', imageLinks)

        assertTrue 'File not found: Heatmap__.png', new File(workingDirectory, 'Heatmap__.png').exists()
        assertTrue 'File not found: Heatmap.svg', new File(workingDirectory, 'Heatmap.svg').exists()
        assertTrue 'File not found: jobInfo.txt', new File(workingDirectory, 'jobInfo.txt').exists()
        assertTrue 'File not found: outputfile.txt', new File(workingDirectory, 'outputfile.txt').exists()
        assertTrue 'File not found: request.json', new File(workingDirectory, 'request.json').exists()
        assertThat imageLinks, contains('/analysisFiles/user-Analysis-100/workingDirectory/Heatmap__.png')
        assertThat service.zipLink, equalTo('/analysisFiles/user-Analysis-100/zippedData.zip')
    }
}

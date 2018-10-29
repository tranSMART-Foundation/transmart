package com.recomdata.transmart.rmodules

import com.recomdata.transmart.data.association.RModulesOutputRenderService
import grails.test.mixin.TestFor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.security.AuthUserDetails
import org.transmart.plugin.shared.security.Roles
import org.transmartproject.core.exceptions.InvalidRequestException
import sendfile.SendFileService

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

@TestFor(AnalysisFilesController)
class AnalysisFilesControllerTests {

    private static final String USER_NAME = 'user'
    private static final String OTHER_USER_NAME = 'other_user'
    private static final String ADMIN_NAME = 'admin'
    private static final String EXISTING_FILE_NAME = 'file_that_exists'
    private static final String FILE_CONTENTS = 'file contents\n'
    private static final String ANALYSIS_NAME = USER_NAME + '-Analysis-100'

    private File temporaryDirectory
    private File analysisDirectory
    private File targetFile
    private User principal
    private String username
    private String path

    @Before
    void before() {
        temporaryDirectory = File.createTempDir('analysis_file_test', '')
        analysisDirectory = new File(temporaryDirectory, ANALYSIS_NAME)
        analysisDirectory.mkdir()

        controller.RModulesOutputRenderService = new RModulesOutputRenderService()
        controller.RModulesOutputRenderService.tempFolderDirectory = temporaryDirectory.absolutePath + '/'

	controller.sendFileService = new SendFileService() {
	    void sendFile(ServletContext servletContext, HttpServletRequest request,
		          HttpServletResponse response, File file, Map headers = [:]) {
		assert file == targetFile
	    }
	}

	controller.securityService = new SecurityService() {
	    AuthUserDetails principal() {
		AnalysisFilesControllerTests.this.principal
	    }
	}
    }

    private void setAdmin(boolean admin) {
	Collection<GrantedAuthority> authorities = []
	if (admin) {
	    authorities << new SimpleGrantedAuthority(Roles.ADMIN.authority)
	}
	principal = new AuthUserDetails(username, username,
					true, true, true, true,
					authorities, 1, 'userRealName', 'user@email.com')
    }

    private void setFile(String filename) {
        targetFile = new File(analysisDirectory, filename)
	targetFile.deleteOnExit()
        targetFile << FILE_CONTENTS

        path = filename
    }

    @After
    void after() {
        temporaryDirectory.deleteDir()
    }

    @Test
    void basicTest() {
        // test the normal circumstances (file exists and is allowed)
        username = USER_NAME
	admin = false
        file = EXISTING_FILE_NAME

	controller.download ANALYSIS_NAME, path

        assertThat response.status, is(200)
    }

    @Test
    void testNoPermission() {
        username = OTHER_USER_NAME
        admin        = false

	controller.download ANALYSIS_NAME, path

        assertThat response.status, is(403)
    }

    @Test
    void testAdminAlwaysHasPermission() {
        username = ADMIN_NAME
        admin        = true
        file         = EXISTING_FILE_NAME

	controller.download ANALYSIS_NAME, path

        assertThat response.status, is(200)
    }

    @Test
    void testBadAnalysisName() {
	shouldFail InvalidRequestException, {
	    controller.download 'not_a_valid_analysis_name', path
        }
    }

    @Test
    void testInexistingAnalysisName() {
        username = USER_NAME
	admin = false

        controller.download ANALYSIS_NAME + '1', path

        assertThat response.status, is(404)
    }

    @Test
    void testAccessToExternalFilesNotAllowed() {
        username        = USER_NAME
	admin = false

        file = '../test'

	controller.download ANALYSIS_NAME, path

	assertThat response.status, is(404)
    }

    @Test
    void testNonExistingFile() {
        username = USER_NAME
	admin = false

        controller.download ANALYSIS_NAME, 'file_that_does_not_exist'

        assertThat response.status, is(404)
    }
}

package fm

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.transmart.plugin.shared.SecurityService
import spock.lang.Specification

@TestFor(FmFolderController)
@Mock(FmFile)
class FmFolderControllerSpec extends Specification {

	void setupSpec() {
		defineBeans {
			securityService(SecurityService)
			springSecurityService(SpringSecurityService)
		}
	}

	void 'test basic download'() {
		when:
		String originalFilename = 'test original Name 丈.pdf'
		long fileSize = 2009
		FmFile file = new FmFile(
				displayName: 'test display name',
				originalName: originalFilename,
				fileSize: fileSize)

		then:
		file.save()

		when:
		File f = File.createTempFile('FmFolderControllerSpec', 'txt')
		f.deleteOnExit()
		f << 'foobar'

		controller.fmFolderService = new FmFolderService() {
			File getFile(FmFile fmFile) {
				f
			}
		}

		params.id = file.id
		controller.downloadFile()

		then:
		response.headers('Content-disposition').size() == 1
		response.header('Content-disposition').decodeURL() == "attachment; filename*=UTF-8''" + originalFilename
		response.header('Content-length') == fileSize as String
		response.text == 'foobar'
	}
}

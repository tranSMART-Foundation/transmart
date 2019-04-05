package fm

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.transmart.biomart.BioData
import org.transmart.biomart.Experiment
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.plugin.shared.security.AuthUserDetails
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import spock.lang.Specification

import static com.recomdata.util.FolderType.ANALYSIS
import static com.recomdata.util.FolderType.ASSAY
import static com.recomdata.util.FolderType.FOLDER
import static com.recomdata.util.FolderType.PROGRAM
import static com.recomdata.util.FolderType.STUDY

@Mock([BioData, Experiment, FmFolder, FmFolderAssociation])
@TestFor(FmFolderService)
//@Ignore
class FmFolderServiceSpec extends Specification {

	private AuthUser user = new AuthUser()
	private FmFolder program1 = new FmFolder(
//			id: 1L,
			folderName: 'Test program 1',
			folderFullName: '\\FOL:1\\',
			folderLevel: 0,
			folderType: PROGRAM.name())
	private FmFolder study1 = new FmFolder(
//			id: 11L,
			folderName: 'Test study 11',
			folderFullName: '\\FOL:1\\FOL:11\\',
			folderLevel: 1,
			folderType: STUDY.name(),
			parent: program1)
	private FmFolder assay111 = new FmFolder(
//			id: 111L,
			folderName: 'Test assay 111',
			folderFullName: '\\FOL:1\\FOL:11\\FOL:111\\',
			folderLevel: 2,
			folderType: ASSAY.name(),
			parent: study1)
	private FmFolder folder121 = new FmFolder(
//			id: 121L,
			folderName: 'Test folder 121',
			folderFullName: '\\FOL:1\\FOL:12\\FOL:121\\',
			folderLevel: 2,
			folderType: FOLDER.name(),
			parent: study1)
	private FmFolder study2 = new FmFolder(
//			id: 12L,
			folderName: 'Test study 12',
			folderFullName: '\\FOL:1\\FOL:12\\',
			folderLevel: 1,
			folderType: STUDY.name(),
			parent: program1)
	private FmFolder analysys122 = new FmFolder(
//			id: 122L,
			folderName: 'Test analysys 122',
			folderFullName: '\\FOL:1\\FOL:12\\FOL:122\\',
			folderLevel: 2,
			folderType: ANALYSIS.name(),
			parent: study2)
	private List<FmFolder> studyFolders = [study1, study2]

	void setupSpec() {
		defineBeans {
			securityService(SecurityService)
			springSecurityService(SpringSecurityService) {
				authenticationTrustResolver = new AuthenticationTrustResolverImpl()
			}
			utilService(UtilService)
		}
	}

	void setup() {
		List<FmFolder> allFolders = [program1, study1, assay111, folder121, study2, analysys122]
		allFolders*.description = 'description'
		allFolders*.save(failOnError: true)
		// setup authorization information for study1
		FmFolderAssociation study2folderAssociation = new FmFolderAssociation(
				fmFolder: study1,
				objectType: Experiment.name,
				objectUid: 'Omicsoft:STUDY1')
		study2folderAssociation.save(failOnError: true)
		BioData bioData = new BioData(
				uniqueId: study2folderAssociation.objectUid,
				type: 'EXP')
		bioData.id = -142L
		bioData.save(failOnError: true)
		Experiment bioExperiment = new Experiment(accession: 'STUDY1')
		bioExperiment.id = bioData.id
		bioExperiment.save(failOnError: true)
	}

	void cleanup() {
		SecurityContextHolder.context.authentication = null
	}

	void 'test get access level info for folders no folders'() {
		when:
		login()

		then:
		[:] == service.getAccessLevelInfoForFolders([])
	}

	void 'test get access level info for folders admin'() {
		when:
		login Roles.ADMIN

		Map<FmFolder, String> foldersMap = service.getAccessLevelInfoForFolders(studyFolders)

		then:
		foldersMap
		2 == foldersMap.size()
		studyFolders == foldersMap.keySet() as List
		['ADMIN', 'ADMIN'] == foldersMap.values() as List
	}

	void 'test get access level info for folders dse admin'() {
		when:
		login Roles.DATASET_EXPLORER_ADMIN

		Map<FmFolder, String> foldersMap = service.getAccessLevelInfoForFolders(studyFolders)

		then:
		foldersMap
		2 == foldersMap.size()
		studyFolders == foldersMap.keySet() as List
		['ADMIN', 'ADMIN'] == foldersMap.values() as List
	}

	void 'test get access level info for folders not applicable'() {
		when:
		login Roles.ADMIN, Roles.DATASET_EXPLORER_ADMIN

		Map<FmFolder, String> foldersMap = service.getAccessLevelInfoForFolders([program1])

		then:
		foldersMap
		1 == foldersMap.size()
		[program1] == foldersMap.keySet() as List
		['NA'] == foldersMap.values() as List
	}

	void 'test get access level info for folders locked'() {
		when:
		login()

		service.i2b2HelperService = [
				getSecureTokensForStudies       : { Collection<String> studyIds ->
					assert studyIds == ['STUDY1']
					[STUDY1: 'EXP:STUDY1']
				},
				getSecureTokensWithAccessForUser: { ->
					['EXP:PUBLIC': 'OWN'] // don't return a token for STUDY1 for this user; will be locked
				}
		]

		Map<FmFolder, String> foldersMap = service.getAccessLevelInfoForFolders(studyFolders)

		then:
		foldersMap
		2 == foldersMap.size()
		studyFolders == foldersMap.keySet() as List
		// study2 does not have a folder association linking it to a biomart
		// object and from there to a study. Should be locked too
		['LOCKED', 'LOCKED'] == foldersMap.values() as List
	}

	// TODO: test access granted to regular users

	private void login(Roles... roles) {
		user.authorities = []
		for (Roles role in roles) {
			user.authorities << new Role(authority: role.authority)
		}

		Collection<GrantedAuthority> authorities = roles.collect { Roles role -> new SimpleGrantedAuthority(role.authority) }
		SecurityContextHolder.context.authentication = new TestingAuthenticationToken(
				new AuthUserDetails('username', 'password', true, true,
						true, true, authorities,
						1, 'userRealName', 'email'),
				'password',
				authorities)
	}
}

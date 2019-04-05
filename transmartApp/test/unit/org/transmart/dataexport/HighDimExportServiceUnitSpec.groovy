package org.transmart.dataexport

import com.recomdata.transmart.data.export.HighDimExportService
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern

class HighDimExportServiceUnitSpec extends Specification {

	private HighDimExportService testee = new HighDimExportService()

	private String forbiddenFileNameSymbols = '\"/[]:;|=,'

	void 'test relative folder path for across study node'() {
		when:
		String testFullName = "\\Clinical Information\\${forbiddenFileNameSymbols}\\"

		String relativePath = testee.getRelativeFolderPathForSingleNode([
				getFullName: { -> testFullName },
				getStudy   : { -> },
		] as OntologyTerm)

		Matcher matcher = relativePath =~ "Clinical(.)Information${Pattern.quote(File.separator)}(.+)"

		then:
		matcher.matches()

		!forbiddenFileNameSymbols.contains(matcher[0][1])

		when:
		String encodedFolderName = matcher[0][2]

		then:
		encodedFolderName
		encodedFolderName != forbiddenFileNameSymbols
	}

	void 'test relative folder path for the node inside study'() {
		when:
		String studyFolder = '\\Test Studies\\Study-1\\'
		String testFullName = "${studyFolder}Sub Folder\\${forbiddenFileNameSymbols}\\"

		String relativePath = testee.getRelativeFolderPathForSingleNode([
				getFullName: { -> testFullName },
				getStudy   : { -> [getOntologyTerm: { [getFullName: { studyFolder }] as OntologyTerm }] as Study }
		] as OntologyTerm)

		Matcher matcher = relativePath =~ "Sub(.)Folder${Pattern.quote(File.separator)}(.+)"

		then:
		matcher.matches()

		!forbiddenFileNameSymbols.contains(matcher[0][1])

		when:
		String encodedFolderName = matcher[0][2]

		then:
		encodedFolderName
		encodedFolderName != forbiddenFileNameSymbols
	}
}

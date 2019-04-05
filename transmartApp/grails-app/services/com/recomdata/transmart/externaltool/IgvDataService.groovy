package com.recomdata.transmart.externaltool

import com.recomdata.export.IgvFiles
import groovy.util.logging.Slf4j
import org.transmartproject.core.exceptions.UnexpectedResultException

@Slf4j('logger')
class IgvDataService {

    static transactional = false

    String createJNLPasString(String webRootDir, String sessionFileUrl) {
	new File(webRootDir, '/files/igv.jnlp').text.replaceAll(
	    '</application-desc>',
	    '\t<argument>' + sessionFileUrl + 'argument>\n</application-desc>')
    }

    String createSessionURL(IgvFiles igvFiles, String userName, String locus) {

	File sessionFile = igvFiles.sessionFile
	sessionFile << '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<Session genome="hg19"'
	if (locus) {
	    sessionFile << ' locus="' + locus + '" '
        }
	sessionFile << ' version="4">\n<Resources>\n'

	for (File file in igvFiles.dataFileList) {
	    sessionFile << '<Resource path="' + igvFiles.getFileUrl(file) + '"/>\n'
            if (isVCFfile(file)) {
		createVCFIndexFile file
            }
        }

        sessionFile << '</Resources>\n</Session>'

	igvFiles.getFileUrl sessionFile
    }

    boolean isVCFfile(File file) {
	file.name.toLowerCase().endsWith 'vcf'
    }

    File createVCFIndexFile(File vcfFile) {
        String[] argv = ['index', vcfFile.absolutePath]

        Class igvToolsClass
        try {
            igvToolsClass = Class.forName 'org.broad.igv.tools.IgvTools'
        }
        catch (e) {
            logger.error 'Could not load IgvTools. The igvtools jar is not ' +
                'bundled anymore. You will have to add it as a ' +
                'dependency to the project'
            throw e
        }

        igvToolsClass.newInstance().run argv

        File idxFile = File(vcfFile.absolutePath + '.idx')
	if (!idxFile.exists()) {
	    throw new UnexpectedResultException('Could not create index file for '
						+ vcfFile.absolutePath)
	}

        idxFile
    }
}

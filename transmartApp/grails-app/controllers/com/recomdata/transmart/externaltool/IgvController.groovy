package com.recomdata.transmart.externaltool

import com.recomdata.export.IgvFiles
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.Assert
import org.transmart.plugin.shared.SecurityService

@Slf4j('logger')
class IgvController {

    @Autowired private SecurityService securityService
    @Autowired private IgvDataService igvDataService

    @Value('${com.recomdata.analysis.data.file.dir:}')
    private String fileDirName

    def launchJNLP() {
	String webRootDir = servletContext.getRealPath('/')
	String sessionFileUrl = params.sessionFile
	logger.debug sessionFileUrl
	header 'Content-Type', 'application/x-java-jnlp-file'
	response.outputStream << igvDataService.createJNLPasString(webRootDir, sessionFileUrl)
    }

    //This URL will be launched with the job ID in the query string.
    def launchIGV(String jobName) {
	Assert.hasLength fileDirName, 'property com.recomdata.analysis.data.file.dir is not set '

	String resultfileDir = getIgvFileDirName()
	String newIGVLink = createLink(controller: fileDirName, absolute: true)
        IgvFiles igvFiles = new IgvFiles(getIgvFileDirName(), newIGVLink)

	FileNameFinder finder = new FileNameFinder()

        // find result files -might be multiple
	String pattern = jobName + '*.vcf'
	for (String name in finder.getFileNames(resultfileDir, pattern)) {
	    igvFiles.addFile new File(name)
        }

        // find param files - should have one
	List<String> pFiles = finder.getFileNames(resultfileDir, jobName + '_vcf.params')
	String locus = null
	if (pFiles) {
	    String gene = null
	    String chr = null
	    String snp = null
	    String paramfile = pFiles[0]
	    logger.debug 'find paramfile:{}', paramfile
	    File f = new File(paramfile)
            if (f.exists()) {
		for (String line in f.readLines()) {
		    if (line.startsWith('Chr=') && !chr) {
                        chr = line.substring(4)
                    }
		    if (line.startsWith('Gene=') && !gene) {
                        gene = line.substring(5)
                    }
		    if (line.startsWith('SNP=') && !snp) {
                        snp = line.substring(4)
                    }
                }
            }

            // try to create locus hint for igv
	    locus = snp ?: gene ?: chr
        }

	render view: 'launch', model: [sessionFile: igvDataService.createSessionURL(
	    igvFiles, securityService.currentUsername(), locus)]
    }

    protected String getIgvFileDirName() {
	String webRootName = servletContext.getRealPath('/')
	if (!webRootName.endsWith(File.separator)) {
            webRootName += File.separator
	}
	webRootName + fileDirName
    }
}

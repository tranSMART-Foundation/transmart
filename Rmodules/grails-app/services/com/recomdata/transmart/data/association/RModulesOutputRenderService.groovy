/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

package com.recomdata.transmart.data.association

import com.recomdata.transmart.util.ZipService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.core.users.User

import java.util.regex.Matcher

@Slf4j('logger')
class RModulesOutputRenderService implements InitializingBean {

    static scope = 'request'
    static transactional = false

    def asyncJobService
    User currentUserBean
    ZipService zipService

    String tempDirectory = ''
    private String  jobName = ''
    private String  jobTypeName = ''
    String zipLink = ''
	
    @Value('${RModules.tempFolderDirectory:}')
    private String tempFolderDirectory

    /**
     * The directory where the job data is stored and from where the R scripts run.
     *
     * The odd name ('folderDirectory') is an historical artifact.
     *
     * @return the jobs directory
     */
    String getTempFolderDirectory() {
	tempFolderDirectory
    }

    /**
     * The logical path from which the images will be served.
     * This used to be configurable via <code>RModules.imageURL</code>, but
     * it's now fixed.
     *
     * @return URL path from which images will be served
     */
    String getImageURL() {
        '/analysisFiles/'
    }

    /**
     * The logical path from which the images will be served that is used in
     * array CGH related analyses.
     * @return URL path from which images will be served without backslash as prefix
     */
    String getRelativeImageURL() {
        'analysisFiles/'
    }

    void initializeAttributes(String jobName, String jobTypeName, List<String>links) {
        logger.debug 'initializeAttributes for jobName "{}"; jobTypeName "{}"', jobName, jobTypeName
        logger.debug 'Settings are: jobs directory -> {}, images URL -> {}', tempFolderDirectory, imageURL

        this.jobName = jobName
        this.jobTypeName = jobTypeName

        String analysisDirectory = tempFolderDirectory + jobName + File.separator
        tempDirectory = analysisDirectory + 'workingDirectory' + File.separator

        File tempDirectoryFile = new File(tempDirectory)

        // Rename and copy images if required, build image link list
        tempDirectoryFile.traverse(nameFilter: ~/(?i).*\.png/) { File currentImageFile ->
            // Replace spaces with underscores, as Tomcat 6 is unable
            // to find files with spaces in their name
            String newFileName = currentImageFile.name.replaceAll(/[^.a-zA-Z0-9-_]/, '_')
            File oldImage = new File(currentImageFile.path)
            File renamedImage = new File(tempDirectoryFile, newFileName)
            logger.debug 'Rename {} to {}', oldImage, renamedImage
            oldImage.renameTo renamedImage

            // Build url to image
            String currentLink = imageURL + jobName + File.separator + 'workingDirectory' + File.separator + newFileName
            logger.debug 'New image link: {}', currentLink
            links << currentLink
        }

        try {
	    if (asyncJobService.isUserAllowedToExportResults(currentUserBean, jobName)) {
                String zipLocation = analysisDirectory + 'zippedData.zip'
                if (!new File(zipLocation).isFile()) {
                    zipService.zipFolder tempDirectory, zipLocation
                }
                zipLink = imageURL + jobName + File.separator + 'zippedData.zip'
            }
        }
        catch (e) {
            logger.error e.message, e
        }
    }
	
    String fileParseLoop(File tempDirectoryFile, String fileNamePattern, String fileNameExtractionPattern, Closure fileParseClosure) {
	StringBuilder parseValue = new StringBuilder()

	List<String> txtFiles = []
	tempDirectoryFile.traverse(nameFilter: ~fileNamePattern) { File currentTextFile ->
	    txtFiles << currentTextFile.path
	}

	//Loop through the file path array and parse each of the files. We do this to make different tables if there are multiple files.
	for (String path in txtFiles) {
	    Matcher matcher = path =~ fileNameExtractionPattern
	    if (matcher.matches() && txtFiles.size() > 1) {
		//Add the HTML that will separate the different files.
		parseValue << '<br/><br/><span class="AnalysisHeader">'<< matcher[0][1] << '</span><hr/>'
	    }

	    parseValue << fileParseClosure(new File(path).text)
	}

	parseValue
    }

    String parseVersionFile() {
	fileParseLoop new File(tempDirectory), /.*sessionInfo.*\.txt/,
	/.*sessionInfo(.*)\.txt/, parseVersionFileClosure
    }

    private Closure parseVersionFileClosure = { String statsInStr ->

        StringBuilder sb = new StringBuilder()

        sb << "<br/><a href='#' onclick='\$(\"versionInfoDiv\").toggle()'><span class='AnalysisHeader'>R Version Information</span></a><br/><br/>"

        sb << '<div id="versionInfoDiv" style="display: none;">'

        //This will tell us if we are printing the contents of the package or the session info. We will print the package contents in a table.
        boolean packageCommand = false
        boolean firstPackageLine = true

        for (String line in statsInStr.readLines()) {

            if(line.contains('||PACKAGEINFO||')) {
                packageCommand = true
                continue
            }

            if(!packageCommand) {
                sb << line
                sb << '<br/>'
            }
            else {
                String segments = line.split('\t')

                if(firstPackageLine) {
                    sb << '<br/><br/><table class="AnalysisResults">'
                    sb << '<tr>'
                    for (segment in segments) {
                        sb << '<th>' << segment << '</th>'
                    }
                    sb << '</tr>'

                    firstPackageLine = false
                }
                else {
                    sb << '<tr>'
                    for (segment in segments) {
                        sb << '<td>' << segment << '</td>'
                    }
                    sb << '</tr>'
                }
            }
        }

        sb << '</table>'
        sb << '</div>'

        sb
    }

    void afterPropertiesSet() {
	if (tempFolderDirectory && !tempFolderDirectory.endsWith(File.separator)) {
	    tempFolderDirectory += File.separator
	}
    }
}

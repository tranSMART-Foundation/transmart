/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/
package com.recomdata.grails.plugin.gwas

import com.recomdata.upload.DataUploadResult
import fm.FmFile
import fm.FmFolder
import fm.FmFolderService
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import org.transmart.biomart.AnalysisMetadata
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.ConceptCode
import org.transmart.biomart.Disease
import org.transmart.biomart.Experiment
import org.transmart.biomart.Observation
import org.transmart.plugin.shared.UtilService
import org.transmartproject.db.log.AccessLogService

import java.text.SimpleDateFormat

/**
 * @author DNewton
 */
@Slf4j('logger')
class UploadDataController {

    private static final String DATE_FORMAT = 'yyyyMMddHHmmss'

    @Autowired private AccessLogService accessLogService
    @Autowired private DataUploadService dataUploadService
    @Autowired private FmFolderService fmFolderService
    @Autowired private UtilService utilService

    @Value('${com.recomdata.FmFolderService.filestoreDirectory:}')
    private String filestoreDirectory

    @Value('${com.recomdata.dataUpload.templates.dir:}')
    private String templatesDir

    @Value('${com.recomdata.dataUpload.uploads.dir:}')
    private String uploadsDir

    def index() {
	accessLogService.report 'UploadData-Index', 'Upload Data index page'

	Map model = [uploadDataInstance: new AnalysisMetadata(), uploadFileInstance: new FmFile()]
	addFieldData model, null
	render view: 'uploadData', model: model
    }

    def edit(AnalysisMetadata uploadData) {
	if (!uploadData) {
	    uploadData = new AnalysisMetadata()
	}
	uploadData.sensitiveFlag = '0'

	Map model = [uploadDataInstance: uploadData]
	addFieldData model, uploadData
	render view: 'uploadData', model: model
    }

    def template(String type) {
	if (type) {
	    utilService.sendDownload response, 'text/plain', type + '-template.txt',
		new File(templatesDir, filename).bytes
        }
        else {
	    render status: 500, text: 'No template type given'
        }
    }

    def skipp(AnalysisMetadata upload) {
	DataUploadResult result = new DataUploadResult(success: true)

	if (!upload) {
            upload = new AnalysisMetadata(params)
            result.success = false
            result.error = 'Could not find id for the analysis, something is wrong...'
	    render view: 'complete', model: [result: result, uploadDataInstance: upload]
	    return
        }

	bindData upload, params
        upload.status = 'PENDING'
        upload.save(flush: true)

        try {
            dataUploadService.runStaging(upload.id)
        }
	catch (e) {
	    logger.error e.message, e
        }

        if (upload.hasErrors()) {
            flash.message = 'The metadata could not be saved - please correct the highlighted errors.'
	    Map model = [uploadDataInstance: upload]
	    addFieldData model, upload
	    render view: 'uploadData', model: model
        }
        else {
	    render view: 'complete', model: [result: result, uploadDataInstance: upload]
        }
    }

    def uploadFile(String fileDescription, String displayName) {
	MultipartFile f = request.getFile('uploadFile')
	String accession = params.study

	if (!displayName) {
	    displayName = f.originalFilename
        }

	FmFolder folder = fmFolderService.getFolderByBioDataObject(Experiment.findByAccession(accession))
	File tempFile = new File(filestoreDirectory, f.originalFilename)
	f.transferTo tempFile
	fmFolderService.processFile folder, tempFile, displayName, fileDescription
        tempFile.delete()
	render view: 'fileComplete'
    }

    def upload(AnalysisMetadata upload) {
	if (!upload) {
	    upload = new AnalysisMetadata()
        }
        bindData(upload, params)

        //Handle special cases where separated lists must be saved

	setFromStringOrList upload, 'tags', 'phenotypeIds'
	setFromStringOrList upload, 'genotypePlatform', 'genotypePlatformIds'
	setFromStringOrList upload, 'expressionPlatform', 'expressionPlatformIds'
	setFromStringOrList upload, 'researchUnit', 'researchUnit'

	MultipartFile f = request.getFile('file')
	String filename = null

        if (f && !f.isEmpty()) {
            upload.etlDate = new Date()
	    filename = new SimpleDateFormat(DATE_FORMAT).format(upload.etlDate) + f.originalFilename
            upload.filename = uploadsDir + '/' + filename
        }

	//Save the uploaded file, if any
	DataUploadResult result = new DataUploadResult()

        if (f && !f.isEmpty()) {
	    String fullpath = uploadsDir + '/' + filename
            try {
                result = dataUploadService.writeFile(fullpath, f, upload)
                if (!result.success) {
                    upload.status = 'ERROR'
                    upload.save(flush: true)
		    render view: 'complete', model: [result: result, uploadDataInstance: upload]
                    return
                }
            }
	    catch (e) {
                upload.status = 'ERROR'
                upload.save(flush: true)
		if (e.message) {
		    flash.message2 = e.message + ". If you wish to skip those SNPs, please click 'Continue'. If you wish to reload, click 'Cancel'."
		    Map model = [uploadDataInstance: upload]
		    addFieldData model, upload
		    render view: 'uploadData', model: model
                }
                else {
		    result = new DataUploadResult(success: false, error: 'Could not verify file: unexpected exception occured.' + e.message)
		    render view: 'complete', model: [result: result, uploadDataInstance: upload]
                }
                return
            }

            //If we've reached here, everything is OK - set our state to PENDING to be picked up by ETL
            upload.status = 'PENDING'
        }
        else {
            //This file was previously uploaded with an error - flag this!
	    if (upload.status == 'ERROR') {
                result.error = 'The existing file for this metadata failed to upload and needs to be replaced. Please upload a new file.'
            }
        }

        upload.save(flush: true)
	result.success = upload.status == 'PENDING'

        //If the file is now pending, start the staging process
        if (result.success) {
            try {
                dataUploadService.runStaging(upload.id)
            }
	    catch (e) {
		logger.error e.message, e
            }
        }

        if (upload.hasErrors()) {
            flash.message = 'The metadata could not be saved - please correct the highlighted errors.'
	    Map model = [uploadDataInstance: upload]
	    addFieldData model, upload
	    render view: 'uploadData', model: model
        }
        else {
	    render view: 'complete', model: [result: result, uploadDataInstance: upload]
        }
    }

    private void addFieldData(Map model, AnalysisMetadata upload) {
	Map<String, Map> tagMap = [:]
	Map<String, String> genotypeMap = [:]
	Map<String, String> expressionMap = [:]
	Map<String, String> researchUnitMap = [:]

        if (upload) {
            if (upload.phenotypeIds) {
                for (tag in upload.phenotypeIds.split(';')) {
		    String[] splitTag = tag.split(':')
		    String meshCode = tag
                    if (splitTag.length > 1) {
                        meshCode = splitTag[1]
                    }
		    Disease disease = Disease.findByMeshCode(meshCode)
                    if (disease) {
			tagMap[tag] = [code: disease.meshCode, type: 'DISEASE']
                    }
		    Observation observation = Observation.findByCode(meshCode)
                    if (observation) {
			tagMap[tag] = [code: observation.name, type: 'OBSERVATION']
                    }
                }
            }

            //Platform ID display and ID are both codes
            if (upload.genotypePlatformIds) {
                for (tag in upload.genotypePlatformIds.split(';')) {
		    genotypeMap[tag] = BioAssayPlatform.findByAccession(tag).vendor + ': ' + tag
                }
            }

            if (upload.expressionPlatformIds) {
                for (tag in upload.expressionPlatformIds.split(';')) {
		    expressionMap[tag] = BioAssayPlatform.findByAccession(tag).vendor + ': ' + tag
                }
            }

            if (upload.researchUnit) {
                for (tag in upload.researchUnit.split(';')) {
		    researchUnitMap[tag] = tag
                }
            }

	    model.tags = tagMap
	    model.genotypePlatforms = genotypeMap
	    model.expressionPlatforms = expressionMap
	    model.researchUnit = researchUnitMap
	    model.study = Experiment.findByAccession(upload.study)
        }

        //Vendor names can be null - avoid adding these
	List<String> expVendorlist = []
	List<String> expVendors = BioAssayPlatform.executeQuery('''
		SELECT DISTINCT vendor
		FROM BioAssayPlatform as p
		WHERE p.platformType='Gene Expression'
		ORDER BY p.vendor''')
	for (String expVendor in expVendors) {
            if (expVendor) {
		expVendorlist << expVendor
            }
        }
	model.expVendors = expVendorlist

	List<String> snpVendorlist = []
	List<String> snpVendors = BioAssayPlatform.executeQuery('''
		SELECT DISTINCT vendor
		FROM BioAssayPlatform as p
		WHERE p.platformType='SNP'
		ORDER BY p.vendor''')
	for (String snpVendor in snpVendors) {
            if (snpVendor) {
		snpVendorlist << snpVendor
            }
        }
	model.snpVendors = snpVendorlist

	List<String> researchUnits = []
	List<String> codeNames = ConceptCode.executeQuery('''
		SELECT DISTINCT codeName
		FROM ConceptCode as p
		WHERE p.codeTypeName='RESEARCH_UNIT'
		ORDER BY p.codeName''')
	for (String codeName in codeNames) {
	    if (codeName) {
		researchUnits << codeName
            }
        }
	model.ResearchUnits = researchUnits
    }

    def list() {
	List<AnalysisMetadata> uploads = AnalysisMetadata.createCriteria().list {
            order('id', 'desc')
            maxResults(20)
        }

	[uploads: uploads]
    }

    def studyHasFolder() {
        //Verify that a given study has a folder to upload to.
        //TODO This assumes folder-management
	Map returnData = [:]
        Experiment experiment = Experiment.findByAccession(params.accession)
        if (!experiment) {
            returnData.message = 'No experiment found with accession ' + params.accession
        }

	def folder = fmFolderService.getFolderByBioDataObject(experiment)
        if (!folder) {
            returnData.message = 'No folder association found for accession ' + experiment.accession + ', unique ID ' + experiment.uniqueId?.uniqueId
        }
        else {
	    returnData.found = true
        }
        render returnData as JSON
    }

    private void setFromStringOrList(AnalysisMetadata amd, String paramName, String propertyName) {
	if (params[paramName]) {
	    if (params[paramName] instanceof String) {
		amd[propertyName] = params[paramName]
	    }
	    else {
		amd[propertyName] = params[paramName].join(';')
	    }
	}
	else {
	    amd[propertyName] = ''
	}
    }
}

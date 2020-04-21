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

package com.recomdata.transmart.data.association.pdf

import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.w3c.dom.Document
import org.xhtmlrenderer.pdf.ITextRenderer

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Handles generating PDF file from HTML content.
 * If you need to generate PDF from a GSP page refer to Grails Plugin - Pdf plugin (PdfController, PdfService).
 * 
 * @author SMunikuntla
 */
@Slf4j('logger')
class PDFController {

    @Value('${RModules.tempFolderDirectory:}')
    private String tempFolderDirectory

    def generatePDF(String htmlStr, String filename) {
	// parse our markup into an xml Document
	try {
	    String css = 'file://' + servletContext.getRealPath('') + '/css/datasetExplorer.css'
	    String html = "<html><head><link rel='stylesheet' type='text/css' href='" + css +
		"' media='print'/></head><body>" + htmlStr + '</body></html>'
	    String finalHtml = StringUtils.replace(html.toString(), '/transmart/analysisFiles',
						   'file://' + tempFolderDirectory)
	    logger.info 'generatePDF replacing "{}" ==> "{}"', html, finalHtml

	    //TODO Check if the htmlStr is a Well-Formatted XHTML string
	    if (htmlStr) {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
		    new ByteArrayInputStream(finalHtml.getBytes('UTF-8')))
		ITextRenderer renderer = new ITextRenderer()
		renderer.setDocument doc, null
		renderer.layout()

		response.contentType = 'application/pdf'
		header 'Content-disposition', 'attachment; filename=' + (filename ?: 'document.pdf')
		renderer.createPDF response.outputStream
		response.outputStream.flush()
	    }
	}
	catch (e) {
	    logger.error e.message, e
	}
    }
}

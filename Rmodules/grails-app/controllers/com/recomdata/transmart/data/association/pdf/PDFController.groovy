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
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.commons.lang.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
//import org.w3c.dom.Document
import org.xhtmlrenderer.layout.SharedContext
import org.xhtmlrenderer.pdf.ITextRenderer


/**
 * Handles generating PDF file from HTML content.
 * If you need to generate PDF from a GSP page refer to Grails Plugin - Pdf plugin (PdfController, PdfService).
 * 
 * @author SMunikuntla
 */
@Slf4j('logger')
class PDFController {
    def assetResourceLocator

    @Value('${RModules.tempFolderDirectory:}')
    private String tempFolderDirectory

    def generatePDF(String htmlStr, String filename) {
	// parse our markup into an xml Document
	try {
	    Resource assetDE = assetResourceLocator.findAssetForURI('datasetExplorer.css')
	    Resource assetRM = assetResourceLocator.findAssetForURI('rmodules.css')
	    String cssDE = 'file://' + servletContext.getRealPath('') + assetDE.getPath()
	    String cssRM = 'file://' + servletContext.getRealPath('') + assetRM.getPath()
	    String html = '<html><head>' +
		'<link rel="stylesheet" type="text/css" href="' + cssDE + '" media="print">' +
		'<link rel="stylesheet" type="text/css" href="' + cssRM + '" media="print">' +
//		'<meta name="viewport" content="width=device-width, initial-scale=0.2" />' +
//		'<style>  @page {size: a3 landscape; } </style>' +
		'</head>' +
		'<body>' + htmlStr + '</body></html>'

	    // replace URL path with local path to analysis files
	    String finalHtml = StringUtils.replace(html.toString(), '/transmart/analysisFiles',
						   'file://' + tempFolderDirectory)
//	    finalHtml = StringUtils.replace(finalHtml.toString(), 'img-result-size',
//					    'img-result-pdf')

	    if (htmlStr) {
		logger.info 'generatePDF filename {} finalHtml {}', filename, finalHtml

		// Parse and clean the provided and edited HTML string
		Document doc = Jsoup.parse(finalHtml)
		logger.info 'doc created html: {}', doc.html()

		// Convert the HTML format into XHTML to direct the renderer
		doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		logger.info 'doc converted to XHTML'

		// Generate the PDF file
		ITextRenderer renderer = new ITextRenderer()
		logger.info 'renderer created {}', renderer
		renderer.setDocumentFromString(doc.html())
		logger.info 'renderer setDocument'
		renderer.layout()
		logger.info 'logger.layout done'

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

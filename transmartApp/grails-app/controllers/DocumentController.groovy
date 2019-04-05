import org.springframework.beans.factory.annotation.Autowired
import org.transmart.DocumentFilter
import org.transmart.SearchFilter
import org.transmart.SearchResult

/**
 * @author mmcduffie
 */

class DocumentController {

    private static final Map<String, String> types = [
	doc : 'application/msword',
	docx: 'application/msword',
	htm : 'text/html',
	html: 'text/html',
	pdf : 'application/pdf',
	ppt : 'application/ms-powerpoint',
	pptx: 'application/ms-powerpoint',
	rtf : 'application/rtf',
	txt : 'text/plain',
	xls : 'application/ms-excel',
	xlsx: 'application/ms-excel',
	xml : 'text/xml'].asImmutable()

    @Autowired private DocumentService documentService
    @Autowired private SearchService searchService

    def showDocumentFilter() {
	DocumentFilter filter = sessionSearchFilter().documentFilter
	render template: 'documentFilter', model: [
	    filter: filter,
	    repositories: filter.repositories]
    }

    def filterDocument() {

	DocumentFilter filter = sessionSearchFilter().documentFilter
	for (String repository in filter.repositories.keySet()) {
	    filter.repositories[repository] =
		'on' == params['repository_' + repository.toLowerCase().replace(' ', '_')]
	}
	filter.path = params.path
	filter.type_excel = 'on' == params.type_excel
	filter.type_html = 'on' == params.type_html
	filter.type_pdf = 'on' == params.type_pdf
	filter.type_powerpoint = 'on' == params.type_powerpoint
	filter.type_text = 'on' == params.type_text
	filter.type_word = 'on' == params.type_word
	filter.type_other = 'on' == params.type_other

	SearchResult sResult = new SearchResult()
	sessionSearchFilter().datasource = 'document'
	searchService.doResultCount(sResult, sessionSearchFilter())

	render view: '/search/list', model: [searchresult: sResult]
    }

    def datasourceDocument() {
	SearchResult sResult = new SearchResult(
	    result: documentService.documentData(sessionSearchFilter(), params),
	    documentCount: documentService.documentCount(sessionSearchFilter()))

	render template: 'documentResult', model: [searchresult: sResult]
    }

    def downloadFile() {

	String fileName = URLDecoder.decode(params.file)
	File file = new File(fileName)
	if (!file.exists()) {
	    render view: 'search/error', model: [
		title: 'Unable to Display File',
		message: '"' + file.absolutePath + '" was not found on the server.']
	    return
	}

	if (!file.canRead()) {
	    render view: 'search/error', model: [
		title: 'Unable to Display File',
		message: '"' + file.absolutePath + '" could not be accessed on the server.']
	    return
        }

	String contentType = 'application/octet-stream'
	int start = file.name.lastIndexOf('.');
        if (start != -1) {
	    String ext = file.name.substring(start + 1, file.name.length())
	    contentType = types[ext]
        }

        if (contentType == 'application/pdf') {
	    header 'Content-Type', contentType
	    header 'Content-Disposition', 'inline; filename="' + file.name + '"'
        }
        else if (contentType == 'text/hmtl') {
	    header 'Content-Type', contentType
	    header 'Content-Length', Long.toString(file.size())
        }
        else if (contentType != 'text/html') {
	    header 'Content-Type', contentType
	    header 'Content-Disposition', 'attachment; filename="' + file.name + '"'
        }
	else {
	    header 'Content-Type', 'application/octet-stream'
	    header 'Content-Disposition', 'attachment; filename="' + file.name + '"'
	    header 'Content-Length', Long.toString(file.size())
        }

	file.withInputStream { response.outputStream << it }
        response.outputStream.flush()
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}

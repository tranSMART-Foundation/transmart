package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class DocumentFilter {
    Map<String, Boolean> repositories = [Biomarker: true, Conferences: true, DIP: true, 'Jubilant Oncology': true]
    String path = ''
    boolean type_excel = true
    boolean type_html = true
    boolean type_pdf = true
    boolean type_powerpoint = true
    boolean type_text = true
    boolean type_word = true
    boolean type_other = true

    Map<String, List<String>> getFilters() {

	Map<String, List<String>> filters = [:]

        if (!type_excel || !type_html || !type_pdf || !type_powerpoint || !type_text || !type_word || !type_other) {
	    List<String> types = []
            // Other checked and one or more types checked - use NOTEXTENSION filter
            if (type_other) {
                if (!type_excel) {
		    types << 'xls'
		    types << 'xlsx'
                }
                if (!type_html) {
		    types << 'htm'
		    types << 'html'
                }
                if (!type_pdf) {
		    types << 'pdf'
                }
                if (!type_powerpoint) {
		    types << 'ppt'
		    types << 'pptx'
                }
                if (!type_text) {
		    types << 'txt'
                }
                if (!type_word) {
		    types << 'doc'
		    types << 'docx'
                }
		filters.NOTEXTENSION = types
            }
            else {
                if (type_excel) {
		    types << 'xls'
		    types << 'xlsx'
                }
                if (type_html) {
		    types << 'htm'
		    types << 'html'
                }
                if (type_pdf) {
		    types << 'pdf'
                }
                if (type_powerpoint) {
		    types << 'ppt'
		    types << 'pptx'
                }
                if (type_text) {
		    types << 'txt'
                }
                if (type_word) {
		    types << 'doc'
		    types << 'docx'
                }
		if (types) {
		    filters.EXTENSION = types
                }
            }
        }

	List<String> repos = []
        for (key in repositories.keySet()) {
	    if (repositories[key]) {
		repos << key
            }
        }
	if (repos && repos.size() != repositories.size()) {
	    filters.REPOSITORY = repos
        }
	List<String> paths = []
	if (path) {
	    paths << path
	    filters.PATH = paths
        }

	filters
    }
}

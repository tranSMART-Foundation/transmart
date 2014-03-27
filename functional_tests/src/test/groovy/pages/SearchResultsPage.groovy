package pages

import geb.Page

class SearchResultsPage extends Page {
	static url = 'search/doSearch'

	static at = {
		summary?.value()?.contains("found")
	}

	static content = {
		summary {$("#summarycount-span")}
	}
}

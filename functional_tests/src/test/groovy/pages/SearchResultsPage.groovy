package pages

import geb.Page

class SearchResultsPage extends SearchPage {
	static url = 'search/doSearch'

	static at = {
		summaryCount?.text()?.contains("found")
	}

	static content = {
		summaryCountDiv {$("#summarycount-div")}
		summaryCount {$("#summarycount-span")}
	}
}

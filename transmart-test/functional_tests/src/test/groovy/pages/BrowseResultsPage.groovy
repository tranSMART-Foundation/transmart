package pages

import geb.Page

class BrowseResultsPage extends Page {
	static url = 'RWG/doSearch'

	static at = {
		summary?.value()?.contains("found")
	}

	static content = {
		summary {$("#summarycount-span")}
	}
}

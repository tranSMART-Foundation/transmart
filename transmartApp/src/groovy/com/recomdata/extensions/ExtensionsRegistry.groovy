package com.recomdata.extensions

import groovy.transform.CompileStatic

@CompileStatic
class ExtensionsRegistry {

    List<Map<String, Object>> analysisTabExtensions = []
    List<Map<String, Object>> tabs = new LinkedList<>()

    void registerAnalysisTabExtension(Map<String, Object> config = [:], String extensionId,
	                              String resourcesUrl, String bootstrapFunction) {
	analysisTabExtensions << [extensionId: extensionId, resourcesUrl: resourcesUrl,
		                  bootstrapFunction: bootstrapFunction, config: config]
    }

    void registerTab(Map<String, Object> config = [:], String id, String title, String controller) {
	Map<String, Object> tab = [id: id, title: title, controller: controller, insertBefore: config.insertBefore]
	findAndInsertBefore(tabs, tab as Map) { Map<String, Object> map ->
	    tab.id in map.insertBefore
	}
    }

    private static <T> void findAndInsertBefore(List<T> items, T item, Closure<Boolean> closure) {
	for (ListIterator it = items.listIterator(); it.hasNext();) {
            if (closure(it.next())) {
                // Step back to be before current tab and insert the tab before that
                it.previous()
		it.add item
                return
            }
        }
	items << item
    }

    /**
     * incorporateTab in existing tab list. If insertBefore option specified,
     * then tries to found tab with specified id and insert new tab before that
     */
    private static void incorporateTab(List<Map<String, Object>> tabs, Map<String, Object> tab) {
        def insertBefore = tab.insertBefore
        if (insertBefore) {
	    findAndInsertBefore(tabs, tab) { Map<String, Object> it ->
		it.id in insertBefore
	    }
	}
	else {
            tabs << tab
        }
    }

    List<Map<String, Object>> getTabs(List<Map<String, Object>> defaultTabs = []) {
	List<Map<String, Object>> result = new LinkedList<>(defaultTabs)
	for (tab in tabs) {
	    incorporateTab result, tab
        }
        result
    }
}

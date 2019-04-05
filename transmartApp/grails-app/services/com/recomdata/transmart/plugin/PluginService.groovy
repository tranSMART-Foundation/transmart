package com.recomdata.transmart.plugin

import org.json.JSONArray
import org.json.JSONObject

class PluginService {

    static transactional = false

    JSONObject getPluginModulesAsJSON(String pluginName) {
	Plugin plugin = Plugin.findByName(pluginName)
	JSONObject result = new JSONObject()
	JSONArray modulesJSON = new JSONArray()

        if (pluginName) {
	    List<Object[]> modules = PluginModule.createCriteria().list {
                projections {
                    property 'moduleName', 'moduleName'
                    property 'name', 'name'
                    property 'category', 'category'
                }
		eq 'plugin', plugin
		eq 'active', true
		order 'name', 'asc'
		order 'category', 'asc'
            }

	    if (modules) {
		result.put 'success', true
		for (Object[] module in modules) {
                    // Since each module has the heavy params object,
                    // we will not use 'obj as JSON' as we don't want to pass the heavy params to the UI
                    if (module.length == 3) {
			modulesJSON.put new JSONObject(id: module[0], text: module[1], group: module[2])
                    }
                }
            }
            else {
		result.put 'success', false
            }
	    result.put 'count', modules?.size() ?: 0
	    result.put 'modules', modulesJSON
        }
        else {
	    result.put 'success', false
	    result.put 'message', 'Plugin name is missing!!! Modules cannot be looked up.'
	}

	result
    }

    PluginModule findPluginModuleByModuleName(String moduleId) {
	PluginModule.findByModuleName moduleId
    }
}

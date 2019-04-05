package com.recomdata.transmart.plugin

import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.json.JSONObject
import org.springframework.dao.DataIntegrityViolationException

class PluginModuleController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', validateModuleParams: 'POST']
    static defaultAction = 'list'

    def list() {
	params.max = Math.min(params.int('max', 10), 100)
	if (null == params.sort) {
	    params.sort = 'name'
	}
	if (null == params.order) {
	    params.order = 'asc'
	}
	[pms: PluginModule.list(params), pmCount: PluginModule.count()]
    }

    def show(PluginModule pluginModule) {
	if (pluginModule) {
	    [pm: pluginModule, paramsStr: pluginModule.paramsStr]
        }
        else {
	    flash.message = "PluginModule not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(PluginModule pluginModule) {
	if (pluginModule) {
            try {
		pluginModule.delete()
		flash.message = "PluginModule ${params.id} deleted"
		redirect action: 'list'
            }
	    catch (DataIntegrityViolationException e) {
		flash.message = "PluginModule ${params.id} could not be deleted"
		redirect action: 'show', id: params.id
            }
        }
        else {
	    flash.message = "PluginModule not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def edit(PluginModule pluginModule) {
	if (pluginModule) {
	    createOrEditModel pluginModule
        }
        else {
	    flash.message = "PluginModule not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def validateModuleParams() {
	JSONObject json = new JSONObject()
        try {
	    json.put 'status', true
	    json.put 'message', 'Plugin Module has valid parameters'
        }
        catch (ConverterException e) {
	    json.put 'status', false
	    json.put 'message', 'Parameters should be a well formed JSON string : \n' + e.message
        }

	json
    }

    def update(PluginModule pluginModule) {
	if (pluginModule) {
            if (params.version) {
		long version = params.long('version', 0)
		if (pluginModule.version > version) {
		    pluginModule.errors.rejectValue  'version',
			'pluginModule.optimistic.locking.failure',
			'Another user has updated this PluginModule while you were editing.'
		    render view: 'edit', model: createOrEditModel(pluginModule)

                    return
                }
            }

	    pluginModule.properties = params
            try {
		pluginModule.setParamsStr(params.paramsStr)
            }
            catch (ConverterException e) {
		pluginModule.errors.rejectValue  'params',
		    'Parameters should be a well formed JSON string : ' + e.message +
		    ' : ' + e.cause?.message?.substring(0, 50) + '...'
            }
	    if (!pluginModule.hasErrors() && pluginModule.save()) {
		flash.message = "PluginModule ${params.id} updated"
		redirect action: 'show', id: pluginModule.id
            }
            else {
		render view: 'edit', model: createOrEditModel(pluginModule)
            }
        }
        else {
	    flash.message = "PluginModule not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	PluginModule pluginModule = new PluginModule(params)
	createOrEditModel pluginModule
    }

    def save() {
	PluginModule pluginModule = new PluginModule(params)
	pluginModule.setParamsStr(params.paramsStr)
	if (!pluginModule.hasErrors() && pluginModule.save()) {
	    flash.message = "PluginModule ${pluginModule.id} created"
	    redirect action: 'show', id: pluginModule.id
        }
        else {
	    render view: 'create', model: createOrEditModel(pluginModule)
        }
    }

    private Map createOrEditModel(PluginModule pm) {
	[pm: pm,
	 paramsStr: pm.paramsStr,
	 categories: PluginModuleCategory.values(),
	 plugins: Plugin.list()]
    }
}

package com.recomdata.transmart.plugin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

class PluginController {

    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
    static defaultAction = 'list'

    @Autowired private  PluginService pluginService

    def list() {
	params.max = Math.min(params.int('max', 10), 100)
	[plugins: Plugin.list(params), pluginCount: Plugin.count()]
    }

    def modules(String pluginName) {
	response.contentType = 'text/json'
	response.outputStream << pluginService.getPluginModulesAsJSON(
	    pluginName?.trim() ?: 'R-Modules')?.toString()
    }

    def show(Plugin plugin) {
	if (plugin) {
	    [plugin: plugin]
        }
        else {
	    flash.message = "Plugin not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def delete(Plugin plugin) {
	if (plugin) {
            try {
		plugin.delete()
		flash.message = "Plugin ${params.id} deleted"
		redirect action: 'list'
	    }
	    catch (DataIntegrityViolationException e) {
		flash.message = "Plugin ${params.id} could not be deleted"
		redirect action: 'show', id: params.id
            }
        }
        else {
	    flash.message = "Plugin not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def edit(Plugin plugin) {
	if (plugin) {
	    [plugin: plugin]
        }
        else {
	    flash.message = "Plugin not found with id ${params.id}"
	    redirect action: 'list'
        }
    }

    def update(Plugin plugin) {
	if (!plugin) {
	    flash.message = "Plugin not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
            return
        }

	if (params.version) {
	    long version = params.long('version', 0)
	    if (plugin.version > version) {
		plugin.errors.rejectValue 'version',
		    'plugin.optimistic.locking.failure',
		    'Another user has updated this Plugin while you were editing.'
		render view: 'edit', model: [plugin: plugin]
		return
            }
        }

	plugin.properties = params
	if (!plugin.hasErrors() && plugin.save()) {
	    flash.message = "Plugin ${params.id} updated"
	    redirect action: 'show', id: plugin.id
        }
        else {
	    render view: 'edit', model: [plugin: plugin]
        }
    }

    def create() {
	[plugin: new Plugin(params)]
    }

    def save() {
	Plugin plugin = new Plugin(params)
	if (!plugin.hasErrors() && plugin.save()) {
	    flash.message = "Plugin ${plugin.id} created"
	    redirect action: 'show', id: plugin.id
        }
        else {
	    render view: 'create', model: [plugin: plugin]
        }
    }
}

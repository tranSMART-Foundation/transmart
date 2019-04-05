package com.recomdata.transmart.plugin

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Plugin, PluginModule])
class PluginModuleSpec extends Specification {

	void 'test CRUD'() {
		when:
		Plugin plugin = new Plugin(
				active: false,
				defaultLink: 'defaultLink',
				hasForm: false,
				hasModules: false,
				name: 'name',
				pluginName: 'pluginName').save(failOnError: true)

		PluginModule pluginModule = new PluginModule(
				name: 'Test Plugin Module',
				category: PluginModuleCategory.DEFAULT,
				moduleName: 'TestPluginModule',
				version: '0.1',
				active: true,
				hasForm: false,
				params: 'params',
				plugin: plugin)
		pluginModule.save(failOnError: true)

		pluginModule = PluginModule.findByModuleName('TestPluginModule')

		then:
		pluginModule

		when:
		pluginModule.name = 'Test Plugin Module updated'
		pluginModule.save()

		pluginModule.delete()

		then:
		noExceptionThrown()
	}
}

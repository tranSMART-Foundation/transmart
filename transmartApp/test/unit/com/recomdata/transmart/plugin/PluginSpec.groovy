package com.recomdata.transmart.plugin

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Plugin)
class PluginSpec extends Specification {

	void 'test CRUD'() {
		when:
		new Plugin(
				name: 'Test Plugin',
				pluginName: 'TestPlugin',
				active: true,
				hasForm: false,
				hasModules: false,
				defaultLink: 'defaultLink').save(failOnError: true)

		Plugin plugin = Plugin.findByPluginName('TestPlugin')

		then:
		plugin

		when:
		plugin.name = 'Test Plugin updated'
		plugin.save()

		plugin.delete()

		then:
		noExceptionThrown()
	}
}

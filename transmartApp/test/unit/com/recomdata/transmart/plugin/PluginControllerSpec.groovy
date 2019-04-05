package com.recomdata.transmart.plugin

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PluginController)
@Mock(Plugin)
class PluginControllerSpec extends Specification {

	void setupSpec() {
		defineBeans {
			pluginService(PluginService)
		}
	}

	void setup() {
		new Plugin(
				name: 'Test Plugin',
				pluginName: 'TestPlugin',
				active: true,
				hasForm: false,
				defaultLink: 'bogus_link',
				hasModules: false).save(failOnError: true)
	}

	void 'test list'() {
		when:
		Map<String, Object> model = controller.list()

		then:
		model.pluginCount == 1
		model.plugins[0].name == 'Test Plugin'
	}

	void 'test show'() {
		when:
		//MockDomain by default sets the id of the domain objects from 1..N (unless you specify the id explicitly)
		//where N is the number of domain objects that we plan to mock
		params.id = 1
		def returnMap = controller.show()

		then:
		//If it fails to load the plugin there will be a message
		flash.message == null
		'Test Plugin' == returnMap?.plugin?.name
	}
}

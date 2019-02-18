package com.recomdata.transmart.plugin

import groovy.transform.CompileStatic

/**
 * @author Florian
 */
@CompileStatic
enum PluginModuleCategory {
    DEFAULT('Default'),
	HEATMAP('Heatmap')

    final String value

    private PluginModuleCategory(String value) {
	this.value = value
    }

    String toString() { value }

    String getKey() { name() }
}

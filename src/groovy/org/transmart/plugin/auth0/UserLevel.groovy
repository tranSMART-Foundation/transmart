package org.transmart.plugin.auth0

import groovy.transform.CompileStatic

@CompileStatic
enum UserLevel {

	UNREGISTERED('Unregistered', -1),
	ZERO('Level 0', 0),
	ONE('Level 1', 1),
	TWO('Level 2', 2),
	ADMIN('Admin', 99)

	final String description
	final int level

	private UserLevel(String description, int level) {
		this.description = description
		this.level = level
	}
}

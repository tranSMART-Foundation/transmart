package org.transmart.plugin.auth0

import groovy.transform.CompileStatic

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
enum Roles {
	ADMIN,
	DATASET_EXPLORER_ADMIN,
	PUBLIC_USER,
	SPECTATOR,
	STUDY_OWNER,
	TRAINING_USER

	final String authority

	private Roles() {
		authority = 'ROLE_' + name()
	}
}

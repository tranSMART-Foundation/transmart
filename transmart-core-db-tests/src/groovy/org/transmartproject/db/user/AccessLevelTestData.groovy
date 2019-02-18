/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.user

import groovy.util.logging.Slf4j
import org.transmart.plugin.shared.security.Roles
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.accesscontrol.AccessLevel
import org.transmartproject.db.accesscontrol.SecuredObject
import org.transmartproject.db.accesscontrol.SecuredObjectAccess
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.ontology.I2b2Secure

import static org.transmartproject.db.ontology.ConceptTestData.createI2b2Secure

@Slf4j('logger')
class AccessLevelTestData extends AbstractTestData {

	public static final String EVERYONE_GROUP_NAME = 'EVERYONE_GROUP'

	/**
	 * The public study (has token EXP:PUBLIC)
	 */
	public static final String STUDY1 = 'STUDY_ID_1'

	/**
	 * The private study (has token EXP:STUDY_ID_2)
	 */
	public static final String STUDY2 = 'STUDY_ID_2'

	/**
	 * Private study (token EXP:STUDY_ID_3), but EVERYONE_GROUP has permissions here
	 */
	public static final String STUDY3 = 'STUDY_ID_3'

	public static final String STUDY2_SECURE_TOKEN = 'EXP:STUDY_ID_2'
	public static final String STUDY3_SECURE_TOKEN = 'EXP:STUDY_ID_3'

	static AccessLevelTestData createDefault() {
		new AccessLevelTestData(ConceptTestData.createDefault())
	}

	/*
	 * The alternative concept data still has hard requirements;
	 * the study names should be a subset of STUDY_ID_{1,2,3}
	 */
	static AccessLevelTestData createWithAlternativeConceptData(ConceptTestData conceptTestData) {
		new AccessLevelTestData(conceptTestData)
	}

	ConceptTestData conceptTestData
	List<String> studies
	List<I2b2Secure> i2b2Secures
	List<SecuredObject> securedObjects
	List<AccessLevel> accessLevels
	List<RoleCoreDb> roles
	List<SecuredObjectAccess> securedObjectAccesses
	List<Group> groups
	List<User> users

	AccessLevelTestData(ConceptTestData conceptTestData) {
		this.conceptTestData = conceptTestData

		studies = conceptTestData.i2b2List*.cComment.collect {
			String[] split = it?.split(':')
			split ? split[1] : null
		}.findAll().unique()

		i2b2Secures = conceptTestData.i2b2List.collect { I2b2 i2b2 ->
			I2b2Secure i2b2sec = createI2b2Secure(
					i2b2.metaClass.properties.findAll {
						it.name in ['level', 'fullName', 'name', 'cComment']
					}.collectEntries {
						[it.name, it.getProperty(i2b2)]
					})
			if (i2b2sec.fullName.contains('study1') || i2b2.cComment == null) {
				i2b2sec.secureObjectToken = 'EXP:PUBLIC'
			}
			else {
				i2b2sec.secureObjectToken = i2b2.cComment.replace('trial', 'EXP')
			}
			i2b2sec
		}

		Set<String> tokens = i2b2Secures*.secureObjectToken as Set
		tokens.remove 'EXP:PUBLIC'

		long id = -500
		securedObjects = tokens.collect { String token ->
			SecuredObject secObj = new SecuredObject(
					dataType: 'BIO_CLINICAL_TRIAL',
					bioDataUniqueId: token)
			secObj.id = --id
			secObj
		}

		createTestData()
	}

	static List<User> createUsers(int count, long baseId) {
		(1..count).collect { int i ->
			long id = baseId - i
			String username = 'user_' + id
			User ret = new User(username: username, uniqueId: username, enable: true)
			ret.id = id
			ret
		}
	}

	static List<Group> createGroups(int count, long baseId) {
		(1..count).collect {
			long id = baseId - it
			String name = 'group_' + id
			Group ret = new Group(category: name, uniqueId: name, enabled: true)
			ret.id = id
			ret
		}
	}

	void saveAll() {
		conceptTestData.saveAll()

		saveAll i2b2Secures, logger
		saveAll securedObjects, logger
		saveAll accessLevels, logger
		saveAll roles, logger
		saveAll groups, logger
		saveAll users, logger
		saveAll securedObjectAccesses, logger
	}

	private void createTestData() {
		long id = -600
		accessLevels = [
			[name: 'OWN', value: 255],
			[name: 'EXPORT', value: 8],
			[name: 'VIEW', value: 1]].collect { Map props ->
				AccessLevel accessLevel = new AccessLevel(props)
				accessLevel.id = --id
				accessLevel
			}

		id = -100
		roles = 	[
				[authority: Roles.ADMIN.authority, description: 'admin user'],
				[authority: Roles.STUDY_OWNER.authority, description: 'study owner'],
				[authority: Roles.SPECTATOR.authority, description: 'spectator user'],
				[authority: Roles.DATASET_EXPLORER_ADMIN.authority, description: 'dataset Explorer admin users - can view all trials'],
				[authority: Roles.PUBLIC_USER.authority, description: 'public user']].collect { Map props ->
			RoleCoreDb role = new RoleCoreDb(props)
			role.id = --id
			role
		}

		groups = []
		Group everyoneGroup = new Group(category: EVERYONE_GROUP_NAME, uniqueId: EVERYONE_GROUP_NAME)
		everyoneGroup.id = -1L
		groups << everyoneGroup

		groups.addAll createGroups(2, -200L)

		users = createUsers(6, -300L)
		users[0].addToRoles roles.find { RoleCoreDb it -> it.authority == Roles.ADMIN.authority }
		users[1].addToGroups groups.find { Group it -> it.category == 'group_-201' }

		/* 1 first user is admin
		 * 2 second user is in group test_-201, which has access to study 2
		 * 3 third user has direct access to study 2
		 * 4 fourth user has no access to study 2
		 * 5 fifth user has only VIEW permissions on study 2
		 * 6 sixth user has both VIEW and EXPORT permissions on study2 (this
		 *   probably can't happen in transmart anyway).
		 * 7 EVERYONE_GROUP has access to study 3
		 */
		securedObjectAccesses = []
		if (STUDY2 in studies) {
			securedObjectAccesses << new SecuredObjectAccess( // 2
					principal: groups.find { it.category == 'group_-201' },
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'EXPORT' })
			securedObjectAccesses << new SecuredObjectAccess( // 3
					principal: users[2],
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'OWN' })
			securedObjectAccesses << new SecuredObjectAccess( // 5
					principal: users[4],
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'VIEW' })
			securedObjectAccesses << new SecuredObjectAccess( // 6 (1)
					principal: users[5],
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'VIEW' })
			securedObjectAccesses << new SecuredObjectAccess( // 6 (2)
					principal: users[5],
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY2_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'EXPORT' })
		}

		if (STUDY3 in studies) {
			securedObjectAccesses << new SecuredObjectAccess( // 7
					principal: groups.find { it.category == EVERYONE_GROUP_NAME },
					securedObject: securedObjects.find { it.bioDataUniqueId == STUDY3_SECURE_TOKEN },
					accessLevel: accessLevels.find { it.name == 'EXPORT' })
		}

		id = -700
		securedObjectAccesses.each { it.id = --id }
	}
}

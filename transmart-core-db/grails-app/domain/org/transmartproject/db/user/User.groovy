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
import org.hibernate.FetchMode
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.users.ProtectedOperation
import org.transmartproject.core.users.ProtectedResource
import org.transmartproject.db.accesscontrol.AccessControlChecks

@Slf4j('logger')
class User extends PrincipalCoreDb implements org.transmartproject.core.users.User {

    @Autowired
    AccessControlChecks accessControlChecks

    String  email
    Boolean emailShow
    String  hash
    String  realName
    String  username

    /* not mapped (only on thehyve/master) */
    //String federatedId

	static hasMany = [groups: Group,
	                  roles : RoleCoreDb]

    static transients = ['accessControlChecks', 'admin', 'accessibleStudies']

    static mapping = {
		table 'searchapp.search_auth_user'
		version false
		cache usage: 'read-only', include: 'non-lazy' /* don't cache groups */

        hash    column: 'passwd'

        // no way to fetch the roles' properties themselves :(
        // http://stackoverflow.com/questions/4208728
        roles   joinTable: [name:   'searchapp.search_role_auth_user',
                            key:    'authorities_id',
                            column: 'people_id'], // insane column naming!
                fetch: FetchMode.JOIN

        groups  joinTable: [name:   'searchapp.search_auth_group_member',
                            key:    'auth_user_id',
                            column: 'auth_group_id']

        discriminator name: 'USER', column: 'unique_id'

        realName column: 'user_real_name'

    }

    static constraints = {
        email        nullable: true, maxSize: 255
        emailShow    nullable: true
        hash         nullable: true, maxSize: 255
        realName     nullable: true, maxSize: 255
        username     nullable: true, maxSize: 255
        //federatedId nullable: true, unique: true
    }

    // not in api

    boolean isAdmin() {
        roles.find { it.authority == RoleCoreDb.ROLE_ADMIN_AUTHORITY }
    }

    boolean canPerform(ProtectedOperation protectedOperation, ProtectedResource protectedResource) {

        if (!accessControlChecks.respondsTo('canPerform',
                [User, ProtectedOperation, protectedResource.getClass()] as Object[])) {
            throw new UnsupportedOperationException('Do not know how to check access for user  ' +
	    	      	this + ', operation ' + protectedOperation + ' on ' + protectedResource)
        }

        if (admin) {
            /* administrators bypass all the checks */
            logger.debug 'Bypassing check for ' + protectedOperation + ' on ' +
                    '' + protectedResource + ' for user ' + this + ' because he is an administrator'
            return true
        }

        accessControlChecks.canPerform this, protectedOperation, protectedResource
    }

    // not in API

    Set<Study> getAccessibleStudies() {
        Set<Study> studies = accessControlChecks.getAccessibleStudiesForUser(this)
        logger.debug 'User ' + this + ' has access to studies: ' + studies*.id
        studies
    }
}

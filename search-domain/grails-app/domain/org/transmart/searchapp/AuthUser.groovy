/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/
package org.transmart.searchapp

import org.transmart.plugin.shared.security.Roles

import static org.transmart.searchapp.Principal.PrincipalType.USER

class AuthUser extends Principal {
    Boolean changePassword
    String email
    boolean emailShow
    String federatedId
    Integer loginAttempts = 0
    String pass = '[secret]' // plain password to create a hashed password
    String passwd
    String username
    String userRealName

    static transients = ['pass']

    static hasMany = [authorities: Role, groups: UserGroup]

    static belongsTo = [Role, UserGroup]

    static mapping = {
	table 'SEARCHAPP.SEARCH_AUTH_USER'
        version false

	authorities joinTable: [name: 'SEARCHAPP.SEARCH_ROLE_AUTH_USER', key: 'AUTHORITIES_ID', column: 'PEOPLE_ID']
        changePassword column: 'CHANGE_PASSWD'
	groups joinTable: [name: 'SEARCHAPP.SEARCH_AUTH_GROUP_MEMBER', column: 'AUTH_GROUP_ID', key: 'AUTH_USER_ID']
	loginAttempts column: 'LOGIN_ATTEMPT_COUNT'
    }

    static constraints = {
	changePassword nullable: true
	email nullable: true
	federatedId nullable: true
	passwd blank: false
	username blank: false, unique: true
	userRealName blank: false
    }

    /*
     * Should be called with an open session and active transaction
     */
    static AuthUser createFederatedUser(String federatedId, String username, String realName, String email) {
	new AuthUser(federatedId: federatedId, username: username ?: federatedId,
		     userRealName: realName ?: '<NONE PROVIDED>', name: realName,
		     email: email, passwd: 'NO_PASSWORD', enabled: true).addToAuthorities(
	    Role.findByAuthority(Roles.SPECTATOR.authority))
    }

    // TODO BB move to tx service
    static void removeAll(Role role) {
	List<AuthUser> usersWithRole = withCriteria {
	    authorities {
		eq('id', role.id)
	    }
	}
	for (AuthUser user in usersWithRole) {
	    user.removeFromAuthorities role
	}
    }

    AuthUser() {
	type = USER
    }

    String toString() {
	userRealName + ' - ' + username
    }

    def beforeInsert() {
	if (!name) {
	    name = userRealName
        }
    }

    def beforeUpdate() {
	name = userRealName
    }
}

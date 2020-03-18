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

import org.transmartproject.db.test.H2Views

class TransmartCoreDbTestsGrailsPlugin {
    def version = '19.0'
    def grailsVersion = '2.5.4 > *'

    def title = 'Transmart Core Db Tests Plugin'
    def author = 'i2b2-Transmart Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def description = '''\
Reuses logic for populating db with test data; also contains tests for core-db project to prevent circular plugin dependencies
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [
        [ name: 'Ruslan Forostianov', email: 'ruslan@thehyve.nl' ],
	[name: 'Peter Kok', email: 'peter@thehyve.nl'],
	[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
    	[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']
    ]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-core-db-tests']

    def doWithSpring = {
        h2Views(H2Views)
    }
}

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
    def version = '16.4-SNAPSHOT'
    def grailsVersion = '2.5.4 > *'

    def title = 'Transmart Core Db Tests Plugin'
    def author = 'Transmart Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def description = 'Reuses logic for populating db with test data; also contains tests for core-db project to prevent circular plugin dependencies'
    def documentation = 'http://transmartproject.org'
    def license = 'GPL3'
    def organization = [name: 'TODO', url: 'TODO']
    def developers = [
        [ name: 'Ruslan Forostianov', email: 'ruslan@thehyve.nl' ],
	[name: 'Peter Kok', email: 'peter@thehyve.nl'],
	[name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']
    ]
    def issueManagement = [system: 'TODO', url: 'TODO']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart-core-db']

    def doWithSpring = {
        h2Views(H2Views)
    }
}

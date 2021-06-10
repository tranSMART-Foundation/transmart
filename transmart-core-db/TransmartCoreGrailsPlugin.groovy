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

import org.springframework.stereotype.Component
import org.transmartproject.db.dataquery.clinical.variables.ClinicalVariableFactory
import org.transmartproject.db.dataquery.highdim.AbstractHighDimensionDataTypeModule
import org.transmartproject.db.http.BusinessExceptionResolver
import org.transmartproject.db.ontology.AcrossTrialsConceptsResourceDecorator
import org.transmartproject.db.ontology.DefaultConceptsResource
import org.transmartproject.db.support.DatabasePortabilityService

class TransmartCoreGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5.4 > *'

    def title = 'Transmart Core DB Plugin'
    def author = 'Transmart Foundation'
    def authorEmail = 'support@transmartfoundation.org'
    def description = '''\
A runtime dependency for tranSMART that implements the Core API
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'GPL3'
    def organization = [name: 'i2b2-tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [
        [name: 'Kees van Bochove', email: 'kees@thehyve.nl'],
        [name: 'Gustavo Lopes',    email: 'gustavo@thehyve.nl'],
	[name: 'Peter Rice',       email: 'ricepeterm@yahoo.co.uk'],
    	[name: 'Burt Beckwith',    email: 'burt_beckwith@hms.harvard.edu']
    ]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-core-db']

    def doWithSpring = {
        xmlns context:'http://www.springframework.org/schema/context'

        def conf = application.config.org.transmartproject

        // unless explicitly disabled, enable across trials functionality
        boolean haveAcrossTrials = conf.enableAcrossTrials != false

        businessExceptionResolver(BusinessExceptionResolver)

        clinicalVariableFactory(ClinicalVariableFactory) {
            disableAcrossTrials = !haveAcrossTrials
        }

        if (haveAcrossTrials) {
            conceptsResourceService(AcrossTrialsConceptsResourceDecorator) {
                inner = new DefaultConceptsResource()
            }
        }
        else {
            conceptsResourceService(DefaultConceptsResource)
        }

        context.'component-scan'('base-package': 'org.transmartproject.db.dataquery.highdim') {
            context.'include-filter'(type: 'assignable', expression: AbstractHighDimensionDataTypeModule.canonicalName)
        }

        context.'component-scan'('base-package': 'org.transmartproject.db') {
            context.'include-filter'(type: 'annotation', expression: Component.canonicalName)
        }

        if (!conf.i2b2.user_id) {
            conf.i2b2.user_id = 'i2b2'
        }
        if (!conf.i2b2.group_id) {
            conf.i2b2.group_id = 'Demo'
        }
    }

    def doWithApplicationContext = { ctx ->
        // Force this bean to be initialized, as it has some dynamic methods to register during its init() method
        ctx.getBean DatabasePortabilityService
    }
}

/*
 * Copyright (c) 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class XnatViewerGrailsPlugin {
    def version = '19.1'
    def grailsVersion = '2.5.4 > *'
    def title = 'XNAT Viewer'
    def author = 'Sijin He'
    def authorEmail = 'sh107@imperial.ac.uk'
    def description = '''\
Connects tranSMART with XNAT providing an "Image View" tab under Analyze to display links to images
'''
    def documentation = 'https://wiki.transmartfoundation.org/'
    def license = 'APACHE'
    def organization = [name: 'i2b2/tranSMART Foundation', url: 'https://www.transmartfoundation.org/']
    def developers = [[name: 'Peter Rice',    email: 'ricepeterm@yahoo.co.uk'],
		      [name: 'Burt Beckwith', email: 'burt_beckwith@hms.harvard.edu']]
    def issueManagement = [system: 'JIRA', url: 'https://jira.transmartfoundation.org/browse/TRANSREL']
    def scm = [url: 'https://github.com/tranSMART-Foundation/transmart/tree/master/transmart-xnat-viewer']
}

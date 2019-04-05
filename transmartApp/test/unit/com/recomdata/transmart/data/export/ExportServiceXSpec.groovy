package com.recomdata.transmart.data.export

import grails.converters.JSON
import grails.test.mixin.TestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmartproject.db.accesscontrol.AccessControlChecks
import org.transmartproject.db.ontology.DefaultConceptsResource
import org.transmartproject.db.ontology.StudiesResourceService
import org.transmartproject.db.user.User
import spock.lang.Specification

/**
 * Don't name this ExportServiceSpec, grails is overly smart and this is
 * equivalent to annotating the class with @TestFor(ExportService).
 * But we need to define a bean before (see setup()).
 *
 * Add ControllerUnitTestMixin so Grails sets up the JSON converter.
 * See http://stackoverflow.com/a/15485593
 */
@TestMixin([ServiceUnitTestMixin, ControllerUnitTestMixin])
class ExportServiceXSpec extends Specification {

    private ExportService exportService

    void setup() {
        defineBeans {
	    accessControlChecks(AccessControlChecks)
	    conceptsResource(DefaultConceptsResource)
	    studiesResource(StudiesResourceService)
	    "$CurrentUserBeanProxyFactory.BEAN_BAME"(User)
        }

	exportService = testFor(ExportService)
    }

    void 'test getHighDimDataTypesAndFormats basic functionality'() {
        given: 'a set of selected checkboxes'
	List<String> selectedCheckBoxList = [
	    [subset: 'subset1', dataTypeId: 'mrna', fileType: 'TXT'],
	    [subset: 'subset1', dataTypeId: 'mrna', fileType: 'TXT'],
	    [subset: 'subset1', dataTypeId: 'mrna', fileType: 'XLS'],
	    [subset: 'subset1', dataTypeId: 'mirna', fileType: 'TXT'],
	    [subset: 'subset2', dataTypeId: 'mrna', fileType: 'TXT']
        ].collect { (it as JSON).toString() }

        when: 'the strings are parsed'
	Map formats = exportService.getHighDimDataTypesAndFormats(selectedCheckBoxList)

        then: 'the output is a properly formatted map'
        // Expected
        //      subset1={mrna={TXT=[GPL570, GPL571], XLS=[GPL570]}, mirna={TXT=[GPL570]}}
        //      subset2={mrna={TXT=[GPL570]}}
        formats.keySet().size() == 2
	formats.containsKey 'subset1'
	formats.containsKey 'subset2'

        formats.subset1.keySet().size() == 2
	formats.subset1.containsKey 'mrna'
	formats.subset1.containsKey 'mirna'

        formats.subset1.mrna.keySet().size() == 2
	formats.subset1.mrna.containsKey 'TXT'
	formats.subset1.mrna.containsKey 'XLS'

        formats.subset1.mirna.keySet().size() == 1
	formats.subset1.mirna.containsKey 'TXT'

        formats.subset2.keySet().size() == 1
	formats.subset2.containsKey 'mrna'

        formats.subset2.mrna.keySet().size() == 1
	formats.subset2.mrna.containsKey 'TXT'
    }
}

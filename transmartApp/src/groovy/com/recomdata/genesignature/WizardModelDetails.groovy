/**
 * model details class for the create/edit wizard in the gene signature module
 */
package com.recomdata.genesignature

import com.recomdata.util.ModelDetails
import groovy.transform.CompileStatic
import org.transmart.biomart.ConceptCode
import org.transmart.searchapp.GeneSignature

/**
 * @author jspencer
 */
@CompileStatic
class WizardModelDetails extends ModelDetails {

    // wizard tyes
    static final int WIZ_TYPE_CREATE = 0
    static final int WIZ_TYPE_EDIT = 1
    static final int WIZ_TYPE_CLONE = 2

    // default is create
    int wizardType = WIZ_TYPE_CREATE

    // pick lists
    def analysisMethods
    def analyticTypes
    def compounds
    def expTypes
    def foldChgMetrics
    def mouseSources
    def normMethods
    def owners
    def platforms
    def pValCutoffs
    def schemas
    def sources
    def species
    def tissueTypes

    GeneSignature geneSigInst

    // id of domain being edited
    def editId
    def cloneId

    /**
     * add an empty other ConceptCode item
     */
    static void addOtherItem(List<ConceptCode> items, String optionId) {
	items << new ConceptCode(bioConceptCode: optionId ?: 'other', codeName: 'other')
    }
}

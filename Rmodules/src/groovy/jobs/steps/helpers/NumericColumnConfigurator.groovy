package jobs.steps.helpers

import groovy.util.logging.Slf4j

import jobs.table.Column
import jobs.table.columns.SimpleConceptVariableColumn
import jobs.table.columns.TransformColumnDecorator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.clinical.ClinicalVariable

@Component
@Scope('prototype')
@Slf4j('logger')
class NumericColumnConfigurator extends ColumnConfigurator {

    private static final List<String> propertyNames = ['header', 'projection', 'keyForConceptPath',
	                                               'keyForDataType', 'keyForSearchKeywordId', 'multiRow'].asImmutable()

    public static final String CLINICAL_DATA_TYPE_VALUE = 'CLINICAL'

    String projection             // only applicable for high dim data

    String keyForConceptPath
    String keyForDataType        // CLINICAL for clinical data
    String keyForSearchKeywordId // only applicable for high dim data
    String keyForLog10

    boolean multiRow = false      // only applicable for high dim data

    boolean alwaysClinical = false

    @Autowired
    private ClinicalDataRetriever clinicalDataRetriever

    @Autowired
    private ResultInstanceIdsHolder resultInstanceIdsHolder

    @Autowired
    private HighDimensionColumnConfigurator highDimensionColumnConfigurator

    @Override
    protected void doAddColumn(Closure<Column> columnDecorator) {
        def resultColumnDecorator = log10 ? compose(columnDecorator, createLog10ColumnDecorator()) : columnDecorator
//	logger.info 'doAddColumn log10 {} isClinical {} resultColumnDecorator {}', log10, isClinical(), resultColumnDecorator
        if (isClinical()) {
            addColumnClinical resultColumnDecorator
        }
        else {
            addColumnHighDim resultColumnDecorator
        }
    }

    private Closure<Column> createLog10ColumnDecorator() {
        { Column originalColumn ->
            new TransformColumnDecorator(
                    inner: originalColumn,
                    valueFunction: { value -> Math.log10(value) })
        }
    }

    boolean isLog10() {
//	logger.info 'isLog10 getStringParam {} {}', keyForLog10, getStringParam(keyForLog10, false)
        'true'.equalsIgnoreCase(getStringParam(keyForLog10, false))
    }

    boolean isClinical() {
        alwaysClinical || getStringParam(keyForDataType) == CLINICAL_DATA_TYPE_VALUE
    }

    private void addColumnHighDim(Closure<Column> decorateColumn) {
        for (prop in propertyNames) {
	    highDimensionColumnConfigurator[prop] = this[prop]
	}
        highDimensionColumnConfigurator.addColumn decorateColumn
    }

    private void addColumnClinical(Closure<Column> decorateColumn) {
        ClinicalVariable variable = clinicalDataRetriever.createVariableFromConceptPath getStringParam(keyForConceptPath).trim()
        variable = clinicalDataRetriever << variable

        clinicalDataRetriever.attachToTable table

        table.addColumn(
                decorateColumn.call(
                        new SimpleConceptVariableColumn(
                                column:      variable,
                                numbersOnly: true,
                                header:      header)),
                [ClinicalDataRetriever.DATA_SOURCE_NAME] as Set)
    }

    /**
     * Sets parameter keys based on optional base key part
     * @param keyPart
     */
    void setKeys(String keyPart = '') {
	String cap = keyPart.capitalize()
        keyForConceptPath     = keyPart + 'Variable'
        keyForDataType        = 'div' + cap + 'VariableType'
        keyForSearchKeywordId = 'div' + cap + 'VariablePathway'
        keyForLog10           = 'div' + cap + 'VariableLog10'
    }

}

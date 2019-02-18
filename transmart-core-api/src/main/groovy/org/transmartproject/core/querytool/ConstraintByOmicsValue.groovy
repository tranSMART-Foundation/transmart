package org.transmartproject.core.querytool

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ConstraintByOmicsValue {

    /**
     * The operator that will be used to compare the value with the constraint.
     */
    Operator operator

    /**
     * The second operand in the constraint.
     */
    String constraint

    /**
     * The highdimension data type
     */
    OmicsType omicsType

    /**
     * String to represent what part of highdimension data to check. This can hold e.g. a gene symbol
     */
    String selector

    /**
     * String to represent what property of the high dimensional data to check the selector against, e.g. geneSymbol
     */
    String property

    /**
     * The projection, should correspond to one of the static strings in {@link org.transmartproject.core.dataquery.highdim.projections.Projection}
     */
    String projectionType

    @CompileStatic
    static enum Operator {

        LOWER_THAN          ('LT'),
            LOWER_OR_EQUAL_TO   ('LE'),
            EQUAL_TO            ('EQ'),
            BETWEEN             ('BETWEEN'),
            GREATER_THAN        ('GT'),
            GREATER_OR_EQUAL_TO ('GE');

        final String value

        private Operator(String value) {
            this.value = value
        }

	static Operator forValue(String value) {
	    Operator operator = values().find { Operator it -> value == it.value }
	    if (operator) {
		operator
	    }
	    else {
		throw new IllegalArgumentException('No operator for value ' + value)
	    }
	}
    }

    @CompileStatic
    static enum OmicsType {
        GENE_EXPRESSION ('Gene Expression'),
        RNASEQ ('RNASEQ'),
        RNASEQ_RCNT ('RNASEQ_RCNT'),
        PROTEOMICS ('PROTEOMICS'),
        CHROMOSOMAL ('Chromosomal'),
        MIRNA_QPCR ('MIRNA_QPCR'),
        MIRNA_SEQ ('MIRNA_SEQ'),
        METABOLOMICS ('METABOLOMICS'),
        RBM ('RBM'),

        VCF ('VCF')

        final String value

        private OmicsType(String value) {
            this.value = value
        }

	static OmicsType forValue(String value) {
	    OmicsType omicsType = values().find { OmicsType it -> value == it.value }
	    if (omicsType) {
		omicsType
	    }
	    else {
		throw new IllegalArgumentException('No OmicsType for value ' + value)
	    }
	}
    }
}

package org.transmartproject.db.dataquery.highdim.tworegion

import com.google.common.collect.AbstractIterator
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.tworegion.Junction

/**
 * Implements a (sparse) row with exactly one assay with data (a junction).
 */
@CompileStatic
class JunctionRow implements DataRow<AssayColumn, Junction> {

    private final AssayColumn assay
    private final int assayIndex
    private final int numAssays
    private final DeTwoRegionJunction junction

    JunctionRow(AssayColumn assay, int assayIndex, int numAssays, DeTwoRegionJunction r) {
	this.assay = assay
	this.assayIndex = assayIndex
	this.numAssays = numAssays
	junction = r
    }

    String getLabel() {
	"Junction at $junction.upChromosome[$junction.upPos] - " +
	    "$junction.downChromosome[$junction.downPos] for $assay.label"
    }

    Junction getAt(int index) {
        if (index == assayIndex) {
            junction
	}
    }

    Junction getAt(AssayColumn assay) {
        if (assay == this.assay) {
            junction
	}
    }

    Iterator<Junction> iterator() {
        new AbstractIterator<Junction>() {
            int i = 0

            protected Junction computeNext() {
                def res
                if (i == assayIndex) {
                    res = junction
                }
                else if (i >= numAssays) {
                    endOfData()
                }
                i++

                res
            }
        }
    }
}

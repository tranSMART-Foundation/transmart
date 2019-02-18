package org.transmartproject.core.dataquery.highdim.acgh

import groovy.transform.CompileStatic

/**
 * Represents the set of possible "hard calls" about copy number variations
 * (loss, normal, gain and amplification) for a specific segment or region in copy
 * number variation trials (e.g. an array comparative genomic hybridization trial).
 */
@CompileStatic
enum CopyNumberState {

    /**
     * Indicates a 'homozygous deletion' hard call; a loss copy-number state for the segment or
     * region
     */
    HOMOZYGOUS_DELETION  (-2),

    /**
     * Indicates a 'loss' hard call; a loss copy-number state for the segment or
     * region
     */
    LOSS                 (-1),

    /**
     * Indicates a 'normal' hard call; a normal copy-number state for the
     * segment or region
     */
    NORMAL               (0),

    /**
     * Indicates a 'gain' hard call; a single gain copy-number state for the
     * segment or region
     */
    GAIN                 (1),

    /**
     * Indicates an 'amplification' hard call; a multiple gain copy-number
     * state for the segment or region
     */
    AMPLIFICATION        (2),

    /**
     * Indicates invalid data. This can result from either a bug or corrupted
     * data
     */
    INVALID              (11)

    /**
     * The integer value used to represent this state. There is an injection
     * from the enumeration values to the integer domain.
     */
    Integer intValue

    private CopyNumberState(int value) {
        intValue = value
    }

    private static Map<Integer, CopyNumberState> ELEMENTS_SET =
	values().collectEntries { CopyNumberState it -> [it.intValue, it] }

    /**
     * Returns the enumeration value mapped to the passed integer,
     * or {@link CopyNumberState#INVALID} if there is none.
     *
     * @param i the integer mapped to the value we want
     * @return the enumeration value
     */
    static CopyNumberState forInteger(Integer i) {
        ELEMENTS_SET[i] ?: INVALID
    }
}

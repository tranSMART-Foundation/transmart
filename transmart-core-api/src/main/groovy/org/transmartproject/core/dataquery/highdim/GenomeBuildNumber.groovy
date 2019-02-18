package org.transmartproject.core.dataquery.highdim

import groovy.transform.CompileStatic

@CompileStatic
enum GenomeBuildNumber {

    GRCh36('GRCH36'),
    GRCh37('GRCH37'),
    GRCh38('GRCH38'),

    UNKNOWN('UNKNOWN')

    private static final Map<String, String> gBuildNumberMap =
	[HG18: 'GRCH36', HG19: 'GRCH37', HG38: 'GRCH38'].asImmutable() as Map

    /**
     * The value of this object for storage purposes.
     */
    final String id  

    private GenomeBuildNumber(id) {
        this.id = id
    }

    static GenomeBuildNumber forId(String id) {
        String ucid = id ? id.toUpperCase() : id
        ucid = gBuildNumberMap[ucid] ?: ucid
        values().find { GenomeBuildNumber it -> it.id == ucid } ?: UNKNOWN
    }
}

/*
 * assert  GenomeBuildNumber.forId('hg18') == GenomeBuildNumber.GRCh36
 * assert  GenomeBuildNumber.forId('hg38') == GenomeBuildNumber.GRCh38
 * assert  GenomeBuildNumber.forId('hg40') == GenomeBuildNumber.UNKNOWN
 */


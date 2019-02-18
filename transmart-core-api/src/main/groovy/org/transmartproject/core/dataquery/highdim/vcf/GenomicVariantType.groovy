package org.transmartproject.core.dataquery.highdim.vcf

import groovy.transform.CompileStatic

@CompileStatic
enum GenomicVariantType {
	SNP,
	INS,
	DEL,
	DIV
    
    /**
     * The Genomic Variant Type for a given reference and alternative.
     */
    static GenomicVariantType getGenomicVariantType(String ref, String alt) {
        String refCleaned = (ref ?: '').replaceAll(/[^ACGT]/, '')
        String altCleaned = (alt ?: '').replaceAll(/[^ACGT]/, '')

        if (refCleaned.length() == 1 && altCleaned.length() == 1) {
            SNP
	}
        else if (altCleaned.length() > refCleaned.length() && altCleaned.contains(refCleaned)) {
            INS
	}
        else if (altCleaned.length() < refCleaned.length() && refCleaned.contains(altCleaned)) {
            DEL
	}
	else {
            DIV
	}
    }
}

package de

class DeSNPInfo {
    String alt
    String aminoAcidChange
    String clinsig
    String codonChange
    String disease
    String effect
    String exonId
    String functionalClass
    String geneBiotype
    String geneId
    String geneName
    String impact
    Long maf
    String ref
    String rsId
    String strand
    String transcriptId
    String variationClass

    static mapping = {
	table 'DEAPP.DE_RC_SNP_INFO'
	id column: 'SNP_INFO_ID', generator: 'sequence', params: [sequence: 'DEAPP.DE_RC_SNP_INFO_SEQ']
        version false

        geneId column: 'ENTREZ_ID' // or GENE_INFO
        maf column: 'GMAF'
    }
}

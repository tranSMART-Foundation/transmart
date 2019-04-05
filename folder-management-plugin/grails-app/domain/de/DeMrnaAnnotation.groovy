package de

class DeMrnaAnnotation implements Serializable {
    private static final long serialVersionUID = 1

    Long geneId
    String geneSymbol
    String gplId
    String organism
    String probeId
    Long probesetId

    static mapping = {
	table 'DEAPP.DE_MRNA_ANNOTATION'
        id column: 'DE_MRNA_ANNOTATION_ID'
	version false
    }

    static constraints = {
	geneId nullable: true
	geneSymbol nullable: true
    }
}

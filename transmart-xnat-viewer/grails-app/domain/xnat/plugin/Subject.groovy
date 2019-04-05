package xnat.plugin

/**
 * @author myyong
 */
class Subject implements Serializable{
    private static final long serialVersionUID = 1

    String transmartSubjectId
    String xnatProject
    String xnatSubjectId

    List<Session> sessions = []

    static transients = ['sessions']

    static mapping = {
        table 'SEARCHAPP.XNAT_SUBJECT'
	id column: 'ID',
	    generator: 'sequence',
	    params: [sequence: 'SEARCHAPP.SEQ_SEARCH_DATA_ID']
        version false

	transmartSubjectId column: 'TSMART_SUBJECTID'
        xnatSubjectId column: 'XNAT_SUBJECTID'
    }
}

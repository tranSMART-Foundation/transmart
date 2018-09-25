package xnat.plugin

/**
 * Created with IntelliJ IDEA.
 * User: myyong
 * Date: 20/10/2014
 * Time: 12:51
 * To change this template use File | Settings | File Templates.
 */


class Subject implements Serializable{
    Long id

    String transmartSubjectId
    String xnatSubjectId
    String xnatProject
    List<Session> sessions = []

    static transients = ['sessions']

    static mapping = {
        table 'SEARCHAPP.XNAT_SUBJECT'
	id generator: 'increment', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_DATA_ID', column: 'id']
        version false

	transmartSubjectId column: 'TSMART_SUBJECTID'
        xnatSubjectId column: 'XNAT_SUBJECTID'
        xnatProject column: 'XNAT_PROJECT'
    }

}

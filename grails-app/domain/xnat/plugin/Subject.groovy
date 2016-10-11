package xnat.plugin

/**
 * Created with IntelliJ IDEA.
 * User: myyong
 * Date: 20/10/2014
 * Time: 12:51
 * To change this template use File | Settings | File Templates.
 */


class Subject implements Serializable{
    int id
    String tranSMART_subjectID
    String xnat_subjectID
    String xnat_project
    List<Session> sessions = new ArrayList<Session>();


    static mapping = {
        table name: 'SUBJECT', schema: 'XNAT'
        version false
        id column: 'ID',
                generator: 'increment'
        tranSMART_subjectID column: 'TSMART_SUBJECTID'
        xnat_subjectID column: 'XNAT_SUBJECTID'
        xnat_project column: 'XNAT_PROJECT'
    }

    static constraints = {
        id(nullable: false)
    }

}
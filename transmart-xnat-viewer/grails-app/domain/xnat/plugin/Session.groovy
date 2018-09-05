package xnat.plugin

/**
 * Created with IntelliJ IDEA.
 * User: myyong
 * Date: 20/10/2014
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */


class Session {
    int id
    String subjectID
    String sessionID
    String sessionName
    List<Scan> scans = new ArrayList<Scan>();


}
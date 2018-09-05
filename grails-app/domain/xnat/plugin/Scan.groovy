package xnat.plugin

/**
 * Created with IntelliJ IDEA.
 * User: myyong
 * Date: 20/10/2014
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */



class Scan {
    int id
    String sessionID
    String scanID
    String seriesDesc
    List<Snapshot> snapshots = new ArrayList<Snapshot>();


}
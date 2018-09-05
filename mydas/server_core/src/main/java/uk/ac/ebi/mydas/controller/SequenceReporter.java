package uk.ac.ebi.mydas.controller;

/**
 * Created by IntelliJ IDEA.
 * User: ljgarcia
 * Date: 27-Jul-2010
 * Time: 16:31:10
 */
public interface SequenceReporter {
    
    /**
     * The start coordinate:
     * <ul>
     *     <li>As requested, if the start coordinate was specified in the request.</li>
     *     <li>If not requested, the actual startcoordinates of the segment returned from the dsn </li>
     *     <li>null if no coordinates were requested and the segment has not been found.</li>
     * </ul>
     * @return the start coordinate to be reported.
     */
    Integer getStart();

    /**
     * The stop coordinate:
     * <ul>
     *     <li>As requested, if the stop coordinate was specified in the request.</li>
     *     <li>If not requested, the actual stop coordinates of the segment returned from the dsn </li>
     *     <li>null if no coordinates were requested and the segment has not been found.</li>
     * </ul>
     * @return the stop coordinate to be reported.
     */
    Integer getStop();

    /**
     * The id of the segment.
     * @return the id of the segment.
     */
    String getSegmentId();
}

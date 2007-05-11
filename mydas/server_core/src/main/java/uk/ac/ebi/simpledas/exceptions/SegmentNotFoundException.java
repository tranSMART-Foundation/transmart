package uk.ac.ebi.simpledas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:12:53
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Exception class specifically to indicate that a particular segment
 * cannot be found by the AnnotationDataSource, in the absence of any other errors
 * (i.e. should only be thrown if the AnnotationDataSource is otherwise healthy)
 */
public class SegmentNotFoundException extends Exception{

    /**
     * The name / accession of the segment being queried.
     */
    private String segment;

    public SegmentNotFoundException(String segment, String message){
        super(message);
        this.segment = segment;
    }

    public SegmentNotFoundException(String segment, String message, Throwable cause){
        super(message, cause);
        this.segment = segment;
    }


    public String getSegment() {
        return segment;
    }
}

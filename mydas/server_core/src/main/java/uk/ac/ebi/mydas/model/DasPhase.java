package uk.ac.ebi.mydas.model;

/**
 * Created using IntelliJ IDEA.
 * Date: 26-May-2007
 * Time: 15:40:09
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public enum DasPhase {
    PHASE_READING_FRAME_0("0"),
    PHASE_READING_FRAME_1 ("1"),
    PHASE_READING_FRAME_2 ("2"),
    PHASE_NOT_APPLICABLE ("-");

    private String representation;

    private DasPhase (String representation){
        this.representation = representation;
    }

    public String toString (){
        return representation;
    }
}

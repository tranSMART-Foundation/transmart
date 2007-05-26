package uk.ac.ebi.mydas.model;

/**
 * Created using IntelliJ IDEA.
 * Date: 26-May-2007
 * Time: 15:35:39
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public enum DasFeatureOrientation {
    ORIENTATION_NOT_APPLICABLE("0"),
    ORIENTATION_SENSE_STRAND("+"),
    ORIENTATION_ANTISENSE_STRAND("-");

    private String representation;

    private DasFeatureOrientation(String representation){
        this.representation = representation;
    }

    public String toString(){
        return representation;
    }
}

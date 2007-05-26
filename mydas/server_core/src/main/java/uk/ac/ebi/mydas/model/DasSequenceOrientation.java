package uk.ac.ebi.mydas.model;

/**
 * Created using IntelliJ IDEA.
 * Date: 26-May-2007
 * Time: 15:32:43
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * The DasSequenceOrientation enum is used to represent the
 * orientation of the entry point.
 */
    public enum DasSequenceOrientation {
        /**
         * Object used to define a positive orientation of a sequenceString.
         */
        POSITIVE_ORIENTATION ("+"),
        /**
         * Object used to define a negative orientation of a sequenceString.
         */
        NEGATIVE_ORIENTATION ("_"),
        /**
         * Object used to define that the sequenceString has no intrinsic orientation.
         * This is the default value of the DasEntryPoint.
         */
        NO_INTRINSIC_ORIENTATION ("+");

        private final String displayString;

        DasSequenceOrientation(String displayString){
            this.displayString = displayString;
        }

        public String toString (){
            return displayString;
        }
    }


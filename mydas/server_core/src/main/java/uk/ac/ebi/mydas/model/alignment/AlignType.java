package uk.ac.ebi.mydas.model.alignment;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 * 
 * The AlignType enum is used to represent the
 * type of the alignObject as reported in the
 * /dasalignment/alignment/alignObject/@type attribute.
 */
public enum AlignType{

    /**
     * Object used to define a DNA type of alignment.
     */
	TYPE_DNA("DNA"),
    /**
     * Object used to define a protein type of alignment.
     */
	TYPE_PROTEIN ("PROTEIN"),
    /**
     * Object used to define a structure type of alignment.
     */
	TYPE_STRUCTURE ("STRUCTURE");

	private String typesString;

	/**
	 * Constructor that sets the type string to match.
	 * @param typesString being the String signifying the type.
	 */
	AlignType (String typesString){
		this.typesString = typesString;
	}
    public String toString (){
        return typesString;
    }

}

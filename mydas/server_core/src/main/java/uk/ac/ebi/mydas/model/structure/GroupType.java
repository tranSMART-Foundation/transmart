package uk.ac.ebi.mydas.model.structure;

/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 * 
 * The GroupType enum is used to represent the
 * type of the group as reported in the
 * /dasstructure/chain/group/@type attribute.
 */
public enum GroupType{

    /**
     * Object used to define a amino type of sequence.
     */
	TYPE_AMINO("amino"),
    /**
     * Object used to define a nucleotide type of sequence.
     */
	TYPE_NUCLEOTIDE ("nucleotide"),
    /**
     * Object used to define a hetatom type of sequence.
     */
	TYPE_HETATOM ("hetatom");

	private String typesString;

	/**
	 * Constructor that sets the type string to match.
	 * @param typesString being the String signifying the type.
	 */
	GroupType (String typesString){
		this.typesString = typesString;
	}
    public String toString (){
        return typesString;
    }

}

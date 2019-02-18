package org.transmartproject.core.ontology

import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.Patient

/**
 * An i2b2 ontology metadata entry.
 */
interface OntologyTerm {

    /**
     * The hierarchical level of the term. The term at the highest level of a`
     * hierarchy has a value of 0, the next level has a value of 1 and so on.
     *
     * @return non-negative integer representing the depth of the term
     */
    Integer getLevel()

    /**
     * Returns the term key; this is a string starting with \\,
     * followed by the the table code and the concept full name
     * @return \\<table code><full name>
     */
    String getKey()

    /**
     * The hierarchical path that leads to the term,
     * like <code>\i2b2\Diagnoses\Musculoskeletal and connective tissue (710)
     * \Arthropaties (710-19)\(714) Rheumatoid arthritis and other
     * arthropaties\(714-0) Rheumatoid arthritis</code>.
     *
     * @return the full path of the term; path with \ as a separator
     */
    String getFullName()

    /**
     * A tranSMART-ism. Return a wrapper around either this ontology term or
     * a parent of this ontology term that is identified as a 'study' or
     * 'experiment' in tranSMART. Maybe return null if no such term exist.
     *
     * @return the study corresponding to this ontology term
     */
    Study getStudy()

    /**
     * The user friendly name of the term.
     *
     * @return a term name to be displayed to the user
     */
    String getName()

    /**
     * A short (50 chars max) string identifying this object.
     * @return a short identifier for this object
     */
    String getCode()

    /**
     * A tooltip to appear in the user interface for this term.
     *
     * @return the term's tooltip
     */
    String getTooltip()

    /**
     * A set of up to three visual attributes that describe how the field
     * should look in the user interface.
     *
     * @return set of {@link VisualAttributes} enum instances
     */
    EnumSet<VisualAttributes> getVisualAttributes()

    /**
     * The term metadata, or null if none. The format of the metadata is not
     * specified.
     *
     * @return metadata associated with the term
     */
    Object getMetadata()

    /**
     * Returns the terms one level below that have this term as a parent.
     * Synonyms and hidden terms are not shown.
     *
     * @return (direct) children of this term, ordered by name
     */
    List<OntologyTerm> getChildren()

    /**
     * Returns the terms one level below that have this term as a parent.
     *
     * @param showHidden whether to return items with the hidden visual
     * attribute
     * @para showSynonyms whether to show synonyms
     * @return (direct) children of this term, ordered by name
     */
    List<OntologyTerm> getChildren(boolean showHidden, boolean showSynonyms)

    /**
     * Returns all the terms below that have this term as a parent.
     *
     * @param showHidden whether to return items with the hidden visual
     * attribute
     * @para showSynonyms whether to show synonyms
     * @return (all) children of this term, ordered by name
     */
    List<OntologyTerm> getAllDescendants(boolean showHidden, boolean showSynonyms)

    /**
     * Returns all the terms below that have this term as a parent.
     *
     * @return (all) children of this term, ordered by name
     */
    List<OntologyTerm> getAllDescendants()

    /**
     * Fetches all the patients that have at least one observation for this OntologyTerm.
     *
     * @return list of patients
     */
    List<Patient> getPatients()

    @CompileStatic
    enum VisualAttributes {

        /**
         * Non-terminal term that can be used as a query item.
         */
        FOLDER(0, 'F' as char),

        /**
         * Non-terminal term that cannot be used as a query item.
         */
            CONTAINER(0, 'C' as char),

        /**
         * The term represents several terms, which are collapsed on it. An
         * example is under Gender in the Demographics folder &#2013; the
         * term 'Unknown' having this type indicates that there are at least
         * two terms that are considered to be 'Unknown Gender' and both are
         * mapped to that one.
         */
            MULTIPLE(0, 'M' as char),

        /**
         * A terminal term.
         */
            LEAF(0, 'L' as char),
            MODIFIER_CONTAINER(0, 'O' as char),
            MODIFIER_FOLDER(0, 'D' as char),
            MODIFIER_LEAF(0, 'R' as char),


        /**
         * A term to be displayed normally.
         */
            ACTIVE              (1, 'A' as char),

        /**
         * A term that cannot be used.
         */
            INACTIVE            (1, 'I' as char),

        /**
         * The term is hidden from the user.
         */
            HIDDEN              (1, 'H' as char),


        /**
         * If present, the term can have children added to it and it can also
         * be deleted.
         */
            EDITABLE            (2, 'E' as char),

        /**
         * !! tranSMART extension !!
         * Indicates high-dimensional data.
         */
            HIGH_DIMENSIONAL    (2, 'H' as char),

        /**
         * To indicate the term as study
         */
            STUDY               (2, 'S' as char),

        /**
         * To indicate the term as program
         */
            PROGRAM             (2, 'P' as char)

        final int position
        final char keyChar

        private VisualAttributes(int position, char keyChar) {
            this.position = position
            this.keyChar = keyChar
        }

        static EnumSet<VisualAttributes> forSequence(String sequence) {
	    EnumSet<VisualAttributes> set = EnumSet.noneOf(VisualAttributes)
	    VisualAttributes[] allValues = values()
	    sequence.eachWithIndex { String c, int i ->
		VisualAttributes v = allValues.find { VisualAttributes it -> it.position == i && it.keyChar == c }
		if (v) {
                    set << v
		}
	    }
	    set
        }
    }
}

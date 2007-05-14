package uk.ac.ebi.mydas.model;

import java.util.Collection;
import java.util.Map;
import java.net.URL;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:20:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasFeature {

    private String featureId;

    private String featureLabel;

    private String typeId;

    private String typeCategory;

    private String typeLabel;

    private boolean typeIsReference;

    private String methodId;

    private String methodLabel;

    private int startCoodinate;

    private int endCoordinate;

    private Double score;

    private String orientation;

    private String phase;

    private Collection<String> notes;

    private Map<URL, String> links;

    private Collection<DasTarget> targets;

    private Collection<DasGroup> groups;
    
}

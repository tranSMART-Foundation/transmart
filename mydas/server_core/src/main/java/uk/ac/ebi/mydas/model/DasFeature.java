package uk.ac.ebi.mydas.model;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:20:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasFeature {

    private final String featureId;

    private final String featureLabel;

    private final String typeId;

    private final String typeCategory;

    private final String typeLabel;

    private final boolean typeIsReference;

    private final String methodId;

    private final String methodLabel;

    /*
     * <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     * segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     * and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     */
    private final int startCoodinate;

    private final int endCoordinate;

    private final Double score;

    private final String orientation;

    private final String phase;

    private final Collection<String> notes;

    private final Map<URL, String> links;

    private final Collection<DasTarget> targets;

    private final Collection<DasGroup> groups;


    public DasFeature(String featureId,
                      String featureLabel,
                      String typeId,
                      String typeCategory,
                      String typeLabel,
                      boolean typeIsReference,
                      String methodId,
                      String methodLabel,
                      int startCoodinate,
                      int endCoordinate,
                      Double score,
                      String orientation,
                      String phase,
                      Collection<String> notes,
                      Map<URL, String> links,
                      Collection<DasTarget> targets,
                      Collection<DasGroup> groups) {

        this.featureId = featureId;
        this.featureLabel = featureLabel;
        this.typeId = typeId;
        this.typeCategory = typeCategory;
        this.typeLabel = typeLabel;
        this.typeIsReference = typeIsReference;
        this.methodId = methodId;
        this.methodLabel = methodLabel;
        this.startCoodinate = startCoodinate;
        this.endCoordinate = endCoordinate;
        this.score = score;
        this.orientation = orientation;
        this.phase = phase;
        this.notes = notes;
        this.links = links;
        this.targets = targets;
        this.groups = groups;
    }


    public String getFeatureId() {
        return featureId;
    }

    public String getFeatureLabel() {
        return featureLabel;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeCategory() {
        return typeCategory;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public boolean isTypeIsReference() {
        return typeIsReference;
    }

    public String getMethodId() {
        return methodId;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public int getStartCoordinate() {
        return startCoodinate;
    }

    public int getStopCoordinate() {
        return endCoordinate;
    }

    public Double getScore() {
        return score;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getPhase() {
        return phase;
    }

    public Collection<String> getNotes() {
        return notes;
    }

    public Map<URL, String> getLinks() {
        return links;
    }

    public Collection<DasTarget> getTargets() {
        return targets;
    }

    public Collection<DasGroup> getGroups() {
        return groups;
    }
}

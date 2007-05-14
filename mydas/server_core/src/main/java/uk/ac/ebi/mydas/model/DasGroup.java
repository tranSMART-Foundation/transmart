package uk.ac.ebi.mydas.model;

import java.util.Collection;
import java.util.Map;
import java.net.URL;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 17:03:37
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasGroup {

    private String groupId;

    private String groupLabel;

    private String groupType;

    private Collection<String> notes;

    private Map<URL, String> links;

    private Collection<DasTarget> targets;


    public DasGroup(String groupId, String groupLabel, String groupType, Collection<String> notes, Map<URL, String> links, Collection<DasTarget> targets) {
        this.groupId = groupId;
        this.groupLabel = groupLabel;
        this.groupType = groupType;
        this.notes = notes;
        this.links = links;
        this.targets = targets;
    }


    public String getGroupId() {
        return groupId;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public String getGroupType() {
        return groupType;
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
}

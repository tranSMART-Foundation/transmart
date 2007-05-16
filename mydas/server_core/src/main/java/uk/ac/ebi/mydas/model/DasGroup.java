/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

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

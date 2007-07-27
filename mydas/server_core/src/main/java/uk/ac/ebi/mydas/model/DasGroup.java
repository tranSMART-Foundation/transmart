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

import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.io.Serializable;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 17:03:37
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This class encapsulates the data held in a
 * /DASGFF/GFF/SEGMENT/FEATURE/GROUP element
 * as the response to the features command.
 *
 * Please see the
 * <a href="http://biodas.org/documents/spec.html#features">
 * DAS 1.53 Specification: Retrieve the Annotations Across a Segment
 * </a>
 * for details of the use of the <GROUP/> element.
 */
public class DasGroup implements Serializable {

    /**
     * <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@id
     */
    private String groupId;

    /**
     * <b>Optional</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@label
     */
    private String groupLabel;

    /**
     * <b>Optional</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@type
     */
    private String groupType;

    /**
     * <b>Optional</b> Collection (can be null), corresponds to
     * the following element in the response to the
     * feature command:
     *
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/NOTE
     */
    private Collection<String> notes;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK
     * elements, where the Map key is the
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK/@href attribute
     * and the Map value is the
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK element value.
     */
    private Map<URL, String> links;

    /**
     * <b>Optional</b> - May be <code>null</code>.
     *
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/TARGET
     * elements.
     *
     * See the documentation of the {@link DasTarget} class
     * for details of how this class maps to the DAS XML.
     */
    private Collection<DasTarget> targets;


    /**
     * Constructor builds a valid DasGroup instance that represents a single
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP element as the response to the features command.
     * @param groupId <b>Mandatory</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@id
     * @param groupLabel <b>Optional</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@label
     * @param groupType <b>Optional</b> field, corresponds to
     * the following attribute in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/@type
     * @param notes <b>Optional</b> Collection (can be null), corresponds to
     * the following element in the response to the
     * feature command:
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/NOTE
     * @param links <b>Optional</b> - May be <code>null</code>.
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK
     * elements, where the Map key is the
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK/@href attribute
     * and the Map value is the
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/LINK element value.
     * @param targets <b>Optional</b> - May be <code>null</code>.
     * For the features command, provides the values for
     * any number of
     * /DASGFF/GFF/SEGMENT/FEATURE/GROUP/TARGET
     * elements.
     * See the documentation of the {@link DasTarget} class
     * for details of how this class maps to the DAS XML.
     */
    public DasGroup(String groupId, String groupLabel, String groupType, Collection<String> notes, Map<URL, String> links, Collection<DasTarget> targets) throws DataSourceException {
        if (groupId == null || groupId.length() == 0){
            throw new DataSourceException("A DasGroup object has been instantiated without setting the mandatory groupId field.");
        }
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

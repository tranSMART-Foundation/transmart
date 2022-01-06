package org.transmartproject.core.dataquery

import org.transmartproject.core.dataquery.assay.Assay

/**
 * A patient mapping is an individual patient mapped to identifiers
 */
interface PatientMapping {

    /**
     * A unique identifier for the patient. Cannot be null.
     *
     * @return object's numeric identifier
     */
    Long getId()

    /**
     * String patient identifier
     *
     * @return patient identifier
     */
    String getPatientIde()

    /**
     * String patient identifier source
     *
     * @return patient identifier source
     */
    String getPatientIdeSource()

    /**
     * String patient identifier status
     *
     * @return patient identifier status
     */
    String getPatientIdeStatus()

    /**
     * The download date of the patient data, or null if not available.
     *
     * @return patient data download date or null
     */
    Date getDownloadDate()

    /**
     * The update date of the patient data, or null if not available.
     *
     * @return patient data update date or null
     */
    Date getUpdateDate()

    /**
     * The upload date of the patient data, or null if not available.
     *
     * @return patient data upload date or null
     */
    Date getUploadDate()

    /**
     * The import date of the patient data, or null if not available.
     *
     * @return patient data import date or null
     */
    Date getImportDate()

    /**
     * The upload id of the patient data, or null if not available.
     *
     * @return upload id of the patient or null
     */
    Long getUploadId()

    /**
     * String describing the source of the patient data, or null if not available.
     *
     * @return source of the patient data or null
     */
    String getSourcesystemCd()
}

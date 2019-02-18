package org.transmartproject.core.dataquery.highdim.vcf

import org.transmartproject.core.dataquery.assay.Assay

/**
 * Represents metadata about one row from the VCF file.
 */
interface VcfValues {

    String getChromosome()
    Long getPosition()
    String getRsId()
    
    String getReferenceAllele()
    List<String> getAlternativeAlleles()
    
    Double getQualityOfDepth()
    String getFilter()
    
    Map<String, String> getInfoFields()
    List<String> getFormatFields()
    
    /**
     * Aggregated information about the selected cohort 
     */
    VcfCohortInfo getCohortInfo()
    
    /**
     * Original subject variant data for a given assay
     */
    String getOriginalSubjectData(Assay assay)
}

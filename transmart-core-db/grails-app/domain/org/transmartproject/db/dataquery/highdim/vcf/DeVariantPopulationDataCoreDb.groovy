package org.transmartproject.db.dataquery.highdim.vcf

class DeVariantPopulationDataCoreDb {

    String chromosome
    DeVariantDatasetCoreDb dataset
    Double floatValue
    Integer infoIndex
    String infoName
    Long integerValue
    Long position
    String textValue

    static constraints = {
        floatValue nullable: true
        infoIndex nullable: true
        infoName nullable: true
        integerValue nullable: true
        textValue nullable: true
    }

    static mapping = {
        table 'deapp.de_variant_population_data'
        id column:'variant_population_data_id', generator: 'sequence'
        version false

        chromosome   column: 'chr'
//        dataset      column: 'dataset_id'
//        floatValue   column: 'float_value'
//        infoIndex    column: 'info_index'
//        infoName     column: 'info_name'
//        integerValue column: 'integer_value'
        position     column: 'pos'
//        textValue    column: 'text_value'
    }
}

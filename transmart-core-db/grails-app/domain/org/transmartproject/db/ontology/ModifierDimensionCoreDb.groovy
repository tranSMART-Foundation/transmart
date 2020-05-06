package org.transmartproject.db.ontology

class ModifierDimensionCoreDb {

    String code
    String name
    String path
    String studyId

    // unused
//    String modifierBlob
//    Date updateDate
//    Date downloadDate
//    Date importDate
//    Long uploadId

    static mapping = {
        table 'i2b2demodata.modifier_dimension'
        id      name: 'path', generator: 'assigned'
        version false

        code     column: 'modifier_cd'
	name     column: 'name_char'
        path     column: 'modifier_path'
        studyId  column: 'sourcesystem_cd'
    }

    static constraints = {
        code           nullable: true, maxSize: 50
        name           nullable: true, maxSize: 2000
        path           maxSize:  700
        studyId        nullable: true, maxSize: 50

        // unused:
//        modifierBlob   nullable: true
//        updateDate     nullable: true
//        downloadDate   nullable: true
//        importDate     nullable: true
//        uploadId       nullable: true
    }
}

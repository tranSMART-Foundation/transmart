package org.transmartproject.db.ontology

class ModifierMetadataCoreDb implements Serializable {

    /* why the tables were designed in a way that this one has no reference
     * modifier_dimension's primary key is beyond me.
     * Add to that oddity that this table that it stores a boolean as a string
     * and doesn't even have a primary key (though in this mapping I chose
     * modifierCode as the primary key (the queries make it clear modifierCode
     * must be unique) */
    String code //id

    String unit
    String valueType
    Character visitInd = (char) 'N'

    static transients = ['visit']

    Boolean visit

    Boolean isVisit() {
        visitInd == 'Y'
    }

    void setVisit(Boolean visit) {
        visitInd = visit == null ? null : visit ? 'Y' : 'N'
    }

    static mapping = {
        table 'i2b2demodata.modifier_metadata'
        id generator: 'assigned', name: 'code'
        version false

        code      column: 'modifier_cd'
        unit      column: 'std_units'
        valueType column: 'valtype_cd'
    }

    static constraints = {
        code      maxSize: 50
        unit      nullable: true,  maxSize: 50
        valueType nullable: true,  maxSize: 10
//        visitInd  nullable: false
    }
}

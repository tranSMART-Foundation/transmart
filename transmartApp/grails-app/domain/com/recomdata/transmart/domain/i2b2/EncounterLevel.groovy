package com.recomdata.transmart.domain.i2b2

class EncounterLevel {

    String conceptCode
    String linkType
    String studyId

    static mapping = {
        table 'DEAPP.DE_ENCOUNTER_LEVEL'
        version false

        conceptCode column: 'CONCEPT_ID'
        linkType column: 'LINK_TYPE'
	studyId column: 'STUDY_ID'
    }
}

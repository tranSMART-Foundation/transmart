package com.recomdata.transmart.domain.i2b2

import java.sql.Clob

class ModifierDimension {

    Date downloadDate
    String id
    Date importData
    Clob modifierBlob
    String modifierPath
    String nameChar
    String sourcesystemCd
    Date updateDate
    Long uploadId

    static mapping = {
        table 'I2B2DEMODATA.MODIFIER_DIMENSION'
	id column: 'MODIFIER_CD'
        version false
    }
}

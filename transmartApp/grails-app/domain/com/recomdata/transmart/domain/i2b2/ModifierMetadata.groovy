package com.recomdata.transmart.domain.i2b2

class ModifierMetadata {

    String id
    String stdUnits
    String valtypeCd
    String visitInd

    static mapping = {
        table 'I2B2DEMODATA.MODIFIER_METADATA'
        id column: 'MODIFIER_CD'
	version false
    }
}

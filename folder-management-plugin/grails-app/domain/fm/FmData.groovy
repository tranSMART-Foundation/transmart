package fm

class FmData {
    String fmDataType
    String uniqueId

    static mapping = {
	table 'FMAPP.FM_DATA_UID'
	id generator: 'assigned', column: 'FM_DATA_ID'
        version false
    }
}

package annotation

class AmData {
    String amDataType
    String uniqueId

    static mapping = {
	table 'AMAPP.AM_DATA_UID'
        id column: 'AM_DATA_ID', generator: 'assigned'
	version false
    }
}

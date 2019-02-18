class BioBankSample {
    String accession_number
    String client_sample_tube_id
    String container_id
    String id
    Date import_date
    String source_type

    static mapping = {
        table 'BIOMART.BIOBANK_SAMPLE'
        id column: 'SAMPLE_TUBE_ID'
	version false
    }
}

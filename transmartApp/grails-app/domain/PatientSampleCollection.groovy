class PatientSampleCollection {

    String id
    String patientId
    String resultInstanceId

    static mapping = {
	table 'I2B2DEMODATA.QTM_PATIENT_SAMPLE_COLLECTION'
        id column: 'SAMPLE_ID'
	version false
    }
}

package galaxy.export.plugin

class StatusOfExport {

    int id
    String jobName
    String jobStatus
    String lastExportName
    Date lastExportTime

    static mapping = {
        table 'GALAXY.STATUS_OF_EXPORT_JOB'
        id column: 'ID',
            generator: 'sequence',
            params: [sequence: 'GALAXY.STATUS_OF_EXPORT_JOB_SEQ']
        version false

        jobName column: 'JOB_NAME_ID'
        jobStatus column: 'JOB_STATUS'
        lastExportName column: 'LAST_EXPORT_NAME'
        lastExportTime column : 'LAST_EXPORT_TIME'
    }

    static constraints = {
        id(nullable: false)
        jobStatus(nullable: false)
        lastExportName(nullable: false)
        lastExportTime(nullable: false)
    }

}

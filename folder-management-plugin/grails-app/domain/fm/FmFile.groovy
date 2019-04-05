package fm

import groovy.transform.ToString

@ToString(includes = ['id', 'displayName', 'originalName', 'fileSize', 'filestoreLocation', 'filestoreName', 'createDate'])
class FmFile {
    Boolean activeInd = true
    Date createDate = new Date()
    String displayName
    Long fileSize
    String filestoreLocation
    String filestoreName
    String fileType
    Long fileVersion = 1
    String linkUrl
    String originalName
    String uniqueId
    Date updateDate = new Date()

    static transients = ['folder', 'uniqueId']

    //Should probably only have one, but Grails doesn't allow join table on one-many
    static hasMany = [folders: FmFolder]

    static belongsTo = FmFolder

    static mapping = {
	table 'FMAPP.fm_file'
	id column: 'file_id', generator: 'sequence', params: [sequence: 'FMAPP.seq_fm_id']
        version false
        cache true
        sort 'displayName'

	folders joinTable: [name: 'FMAPP.fm_folder_file_association', key: 'file_id', column: 'folder_id']
    }

    static constraints = {
	displayName maxSize: 1000
	fileSize nullable: true
	filestoreLocation nullable: true, maxSize: 1000
	filestoreName nullable: true, maxSize: 1000
	fileType nullable: true, maxSize: 100
	linkUrl nullable: true, maxSize: 1000
	originalName maxSize: 1000
    }

    /**
     * Find file by its uniqueId
     * @return file with matching uniqueId or null, if match not found.
     */
    static FmFile findByUniqueId(String uniqueId) {
	// TODO BB
	get FmData.findByUniqueId(uniqueId)?.id
    }

    FmFolder getFolder() {
	folders?.iterator()?.next()
    }

    void setFolder(FmFolder folder) {
	addToFolders folder
    }

    /**
     * Use transient property to support unique ID for folder.
     * @return folder's uniqueId
     */
    String getUniqueId() {
	if (uniqueId) {
            return uniqueId
	}

	FmData data = FmData.get(id)
	if (data) {
	    uniqueId = data.uniqueId
	    return uniqueId
        }
    }
}

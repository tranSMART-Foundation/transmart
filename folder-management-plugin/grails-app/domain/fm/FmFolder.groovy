package fm

import com.recomdata.util.FolderType
import groovy.util.logging.Slf4j

@Slf4j('logger')
class FmFolder implements Buildable {
    Boolean activeInd = true
    String description
    String folderFullName
    Long folderLevel
    String folderName
    String folderTag
    String folderType
    String pluralFolderTypeName
    String uniqueId

    static transients = ['pluralFolderTypeName', 'uniqueId']

    static belongsTo = [parent: FmFolder]

    static hasMany = [children: FmFolder, fmFiles: FmFile]

    static mapping = {
	table 'FMAPP.fm_folder'
	id generator: 'sequence', params: [sequence: 'FMAPP.seq_fm_id'], column: 'folder_id'
        version false
        sort 'folderName'

	fmFiles joinTable: [name: 'FMAPP.fm_folder_file_association', key: 'folder_id', column: 'file_id'],
	    lazy: false, cascade: 'all-delete-orphan'
    }

    static constraints = {
	description blank: false, maxSize: 4000
	folderFullName nullable: true, maxSize: 1000
	folderName blank: false, maxSize: 1000
	folderTag nullable: true, maxSize: 50
	folderType blank: false, maxSize: 100
	parent nullable: true
    }

    /**
     * Find folder by its uniqueId
     * @return folder with matching uniqueId or null if match not found.
     */
    static FmFolder findByUniqueId(String uniqueId) {
	// TODO BB ..
	get FmData.findByUniqueId(uniqueId)?.id
    }

    /**
     * Use transient property to support unique ID for folder.
     * @return folder's uniqueId
     */
    String getUniqueId() {
	if (uniqueId) {
	    return uniqueId
	}

	// TODO BB ..
        FmData data = FmData.get(id)
	if (data) {
            uniqueId = data.uniqueId
            return uniqueId
	}
    }

    String getPluralFolderTypeName() {
	switch (folderType) {
	    case FolderType.ANALYSIS.name():
		return 'ANALYSES'
	    case FolderType.PROGRAM.name():
	    case FolderType.ASSAY.name():
	    case FolderType.FOLDER.name():
		return folderType + 'S'
	    case FolderType.STUDY.name():
		return 'STUDIES'
        }
    }

    FmFolder findParentStudyFolder() {
	FmFolder currentFolder = this
        while (currentFolder) {
            if (currentFolder.folderType == FolderType.STUDY.name()) {
                return currentFolder
            }
            currentFolder = currentFolder.parent
        }
    }

    /**
     * Return true if this folder has any folders that name it as a parent
     * @return true if this folder has children, false otherwise
     */
    boolean hasChildren() {
        def children = FmFolder.createCriteria().list {
            eq('parent', this)
            eq('activeInd', true)
        }
        if (children) {
            return true
        }
        return false
    }

    void build(GroovyObject builder) {
	List<FmFolder> subFolders = executeQuery('''
		from FmFolder as fd
		where fd.folderFullName like :fn escape '*'
		  and fd.folderLevel = :fl''',
		[fn: folderFullName + '%', fl: (folderLevel + 1)])

        def fmFolder = {
	    folderDefinition(id: id) {
                folderName(this.folderName)
                folderFullName(this.folderFullName)
                folderLevel(this.folderLevel)
                folderType(this.folderType)

                unescaped << '<fmFolders>'
		for (FmFolder f in subFolders) {
		    out << f
                }
                unescaped << '</fmFolders>'
            }
        }

        fmFolder.delegate = builder
        fmFolder()
    }

    String toString() {
	'ID: ' + id + ', Folder Name: ' + folderName + ', Folder Full Name: ' + folderFullName +
	    ', Folder Level: ' + folderLevel + ', Folder Type: ' + folderType +
	    ', uniqueId: ' + uniqueId + ', Description: ' + description
    }
}

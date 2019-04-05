package xnat.plugin

class SubjectService {

    static transactional = false

    String getXnatID(String subjectId) {
	Subject.findByTransmartSubjectId(subjectId).xnatSubjectId
    }

    String getXnatProject(String subjectId) {
	Subject.findByTransmartSubjectId(subjectId).xnatProject
    }

    boolean subjectExists(String subjectId) {
	Subject.countByTransmartSubjectIdIsNotNullAndTransmartSubjectId(subjectId)
    }
}

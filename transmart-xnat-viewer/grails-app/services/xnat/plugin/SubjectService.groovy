package xnat.plugin

import grails.transaction.Transactional

@Transactional
class SubjectService {

    def serviceMethod() {

    }



    def getXnatID(def subjectID)
    {
        def subject = Subject.findByTranSMART_subjectID(subjectID.toString());

        return subject.xnat_subjectID;
    }

    def getXnatProject(def subjectID)
    {
        def subject = Subject.findByTranSMART_subjectID(subjectID.toString());


        return subject.xnat_project;
    }

    def SubjectExists(def subjectID)
    {
        if (Subject.findByTranSMART_subjectIDIsNotNullAndTranSMART_subjectID(subjectID.toString()))
            return true;
        else
            return false;
    }
}
getSubjects <- function(study) {
    .checkTransmartConnection()
    studyName <- study[["name"]]
    listOfSubjects <- transmartClientEnv$serverGetRequest(
            paste("/studies/",studyName,"/subjects",sep="")
    )
    subjectIDs <- sapply(listOfSubjects, FUN = function(x) { x[["id"]] })
    names(listOfSubjects) <- paste("id",subjectIDs,sep="")
    listOfSubjects
}

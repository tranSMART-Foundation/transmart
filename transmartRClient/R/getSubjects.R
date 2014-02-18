getSubjects <- function(study) {
    .checkRClientEnvironment()
    studyName <- study[["name"]]
    listOfSubjects <- RClientEnv$serverGetRequest(
            paste("/studies/",studyName,"/subjects",sep="")
    )
    subjectIDs <- sapply(listOfSubjects, FUN = function(x) { x[["id"]] })
    names(listOfSubjects) <- paste("id",subjectIDs,sep="")
    listOfSubjects
}
getSubjects <- function(studyName, as.data.frame = FALSE) {
    .checkTransmartConnection()

    listOfSubjects <- transmartClientEnv$serverGetRequest(
            paste("/studies/",studyName,"/subjects",sep="")
    )
    subjectIDs <- sapply(listOfSubjects, FUN = function(x) { x[["id"]] })
    names(listOfSubjects) <- paste("id",subjectIDs,sep="")

    if (as.data.frame) return(.listToDataFrame(listOfSubjects))
    listOfSubjects
}

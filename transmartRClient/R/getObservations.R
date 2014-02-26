getObservations <- function(studyName, subjectID = NULL, as.data.frame = FALSE) {
    .checkTransmartConnection()
    
    if (is.null(subjectID)) {
        listOfObservations <- transmartClientEnv$serverGetRequest(
            paste("/studies/", studyName, "/observations", sep="")
        )
    } else {
        listOfObservations <- transmartClientEnv$serverGetRequest(
            paste("/studies/", studyName, "/subjects/", subjectID, "/observations", sep="")
        )
    }

    if (as.data.frame) return(.listToDataFrame(listOfObservations))
    listOfObservations
}

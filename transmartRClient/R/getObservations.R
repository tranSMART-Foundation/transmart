# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License, version 3

getObservations <- function(studyName, subjectID = NULL, as.data.frame = FALSE) {
    .checkTransmartConnection()

    if (is.null(subjectID)) {
        listOfObservations <- .transmartServerGetRequest(
            paste("/studies/", studyName, "/observations", sep="")
        )
    } else {
        listOfObservations <- .transmartServerGetRequest(
            paste("/studies/", studyName, "/subjects/", subjectID, "/observations", sep="")
        )
    }

    if (as.data.frame) return(.listToDataFrame(listOfObservations))
    listOfObservations
}

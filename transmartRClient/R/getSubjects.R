# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getSubjects <- function(study.name, as.data.frame = TRUE) {
    .checkTransmartConnection()

    serverResult <- .transmartServerGetRequest( paste("/studies/", study.name,"/subjects", sep=""), use.HAL = TRUE )
    listOfSubjects <- serverResult$subjects
    
    subjectIDs <- sapply(listOfSubjects, FUN = function(x) { x[["id"]] })
    names(listOfSubjects) <- paste("id",subjectIDs,sep="")

    if (as.data.frame) return(.listToDataFrame(listOfSubjects))
    listOfSubjects
}

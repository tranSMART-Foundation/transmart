# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License, version 3

getStudies <- function(nameMatch = "", as.data.frame = TRUE) {
    .checkTransmartConnection()

    serverResult <- .transmartServerGetRequest("/studies", use.HAL = TRUE)
    listOfStudies <- serverResult$studies
    
    studyNames <- sapply(listOfStudies, FUN = function(x) { x[["name"]] })
    names(listOfStudies) <- studyNames
    listOfStudies <- listOfStudies[ grep(nameMatch, studyNames) ]

    if (as.data.frame) return(.listToDataFrame(listOfStudies))
    listOfStudies
}

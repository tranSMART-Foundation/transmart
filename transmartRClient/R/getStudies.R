getStudies <- function(nameMatch = "", as.data.frame = FALSE) {
    .checkTransmartConnection()
    listOfStudies <- transmartClientEnv$serverGetRequest("/studies")

    studyNames <- sapply(listOfStudies, FUN = function(x) { x[["name"]] })
    names(listOfStudies) <- studyNames
    listOfStudies <- listOfStudies[ grep(nameMatch, studyNames) ]

    if (as.data.frame) return(.listToDataFrame(listOfStudies))
    listOfStudies
}

# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getStudies <- function(name.match = "", as.data.frame = TRUE, cull.columns = TRUE) {
    .checkTransmartConnection()

    serverResult <- .transmartServerGetRequest("/studies", accept.type = "hal")
    listOfStudies <- serverResult$studies
    
    studyNames <- sapply(listOfStudies, FUN = function(x) { x[["name"]] })
    names(listOfStudies) <- studyNames
    listOfStudies <- listOfStudies[ grep(name.match, studyNames) ]

    if (as.data.frame) {
        dataFrameStudies <- .listToDataFrame(listOfStudies)
        if (cull.columns) {
            columnsToKeep <- match(c("name", "api.link.self.href", "ontologyTerm.fullName"), names(dataFrameStudies))
            if (any(is.na(columnsToKeep))) {
                warning("There was a problem culling columns. You can try again with cull.columns = FALSE.")
                message("Sorry. You've encountered a bug.\n",
                        "You can help fix it by contacting us. Type ?transmartRClient for contact details.\n", 
                        "Optional: type options(verbose = TRUE) and replicate the bug to find out more details.")
            }
            return(dataFrameStudies[ , columnsToKeep])
        }
        return(dataFrameStudies)
    }
    
    listOfStudies
}

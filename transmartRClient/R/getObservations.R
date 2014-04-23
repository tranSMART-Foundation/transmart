# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getObservations <- function(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE, cull.columns = TRUE) {
    .checkTransmartConnection()

    if (!is.null(concept.match) && is.null(concept.links)) {
        concept.links <- c()
        studyConcepts <- getConcepts(study.name)
        for (toMatch in concept.match) {
            conceptMatch <- grep(toMatch, studyConcepts$name)[1] # TODO: this might result in the same concept being included multiple times
            if (is.na(conceptMatch)) {
                warning(paste("No match found for:", toMatch))
            } else {
                concept.links <- c(concept.links, studyConcepts$api.link.self.href[conceptMatch])
            }
        }
    } else {
        concept.links <- paste("/studies/", study.name, sep = "")
    }

    if (length(concept.links) == 0L) {
        warning("No concepts selected or found to match your arguments.")
        return(NULL)
    }

    listOfObservations <- list()

    for (oneLink in concept.links) {
        serverResult <- .transmartServerGetRequest(
                paste(oneLink, "/observations", sep=""),
                accept.type = "hal") 
        listOfObservations <- c(listOfObservations, serverResult$observations)
    }
    
    

    if (as.data.frame) {
        dataFrameObservations <- .listToDataFrame(listOfObservations)
        if (cull.columns) {
            columnsToCull <- match(c("concept.conceptCode", "concept.conceptPath", "subject.api.link.self.href"), names(dataFrameObservations))
            if (any(is.na(columnsToCull))) {
                warning("There was a problem culling columns. You can try again with cull.columns = FALSE.")
                cat("Sorry. You've encountered a bug.\n",
                    "You can help fix it by contacting us. Type ?transmartRClient for contact details.\n", 
                    "Optional: type options(verbose = TRUE) and replicate the bug to find out more details.\n")
            }
            return(dataFrameObservations[, -columnsToCull])
        }
        return(dataFrameObservations)
    }
    listOfObservations
}

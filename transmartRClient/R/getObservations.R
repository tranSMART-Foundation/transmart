# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getObservations <- function(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE) {
    .checkTransmartConnection()

    if (is.null(concept.links)) {
        if (!is.null(concept.match)) {
            concept.links <- c()
            studyConcepts <- getConcepts(study.name)
            for (toMatch in concept.match) {
                conceptMatch <- grep(toMatch, studyConcepts$name)[1] # TODO: this might result in the same concept being included multiple times
                if (is.na(conceptMatch)) {
                    warning(paste("No match found for:", toMatch))
                } else {
                    concept.links <- c(concept.links, paste(studyConcepts$api.link.self.href[conceptMatch], "/observations", sep = ""))
                }
            }
        } else {
            concept.links <- paste("/studies/", study.name, "/observations", sep = "")
        }
    }

    if (length(concept.links) == 0L) {
        warning("No concepts selected or found to match your arguments.")
        return(NULL)
    }

    listOfObservations <- list()

    for (oneLink in concept.links) {
        serverResult <- .transmartServerGetRequest(
                oneLink,
                accept.type = "hal") 
        listOfObservations <- c(listOfObservations, serverResult$observations)
    }

    if (as.data.frame) {
        dataFrameObservations <- .listToDataFrame(listOfObservations)
        
        conceptNames <- as.factor(dataFrameObservations$label)
        labelComponents <- matrix(nrow = 0, ncol = 0)
        for (level in strsplit(levels(conceptNames), split = "\\\\")) {
            labelComponents <- rbind.fill.matrix(labelComponents, t(level))
        }
        if (length(levels(conceptNames)) > 1) labelComponents <- labelComponents[ , -which(apply(labelComponents, 2, function(x) length(unique(x)))==1)]
        levels(conceptNames) <- apply(labelComponents, 1, function(x) paste(x[!is.na(x)&x!=""], collapse = "_"))
        conceptColumns <- grep("concept\\.|label", colnames(dataFrameObservations))
        
        conceptInfo <- unique(cbind(conceptNames, dataFrameObservations[ , conceptColumns, drop = FALSE]))
        dataFrameObservations <- dataFrameObservations[ , -conceptColumns, drop = FALSE]
        dataFrameObservations <- cbind(dataFrameObservations, conceptNames)
        
        subjectIdColumn <- grep("subject.id", colnames(dataFrameObservations))
        subjectColumns <- setdiff(grep("subject\\.", colnames(dataFrameObservations)), subjectIdColumn)
        
        subjectInfo <- unique(cbind(dataFrameObservations[ , subjectIdColumn, drop = FALSE], dataFrameObservations[ , subjectColumns, drop = FALSE]))
        dataFrameObservations <- dataFrameObservations[ , -subjectColumns, drop = FALSE]                                      
        castedObservations <- cast(dataFrameObservations, subject.id ~ conceptNames)
        factorizedColumns <- which(unlist(lapply(castedObservations, is.factor)))
        for (factorizedColumn in factorizedColumns) {
            castedObservations[ , factorizedColumn] <- levels(castedObservations[ , factorizedColumn])[castedObservations[ , factorizedColumn]]
        }

        return(list(observations = castedObservations, subjectInfo = subjectInfo, conceptInfo = conceptInfo))
    }
    listOfObservations
}

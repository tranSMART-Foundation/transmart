# Copyright 2014, 2015 The Hyve B.V.
# Copyright 2014 Janssen Research & Development, LLC.
#
# This file is part of tranSMART R Client: R package allowing access to
# tranSMART's data via its RESTful API.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version, along with the following terms:
#
#   1. You may convey a work based on this program in accordance with
#      section 5, provided that you retain the above notices.
#   2. You may convey verbatim copies of this program code as you receive
#      it, in any medium, provided that you retain the above notices.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/>.

getObservations <- function(study.name, concept.match = NULL, concept.links = NULL, as.data.frame = TRUE) {
    .ensureTransmartConnection()

    if (is.null(concept.links)) {
        if (!is.null(concept.match)) {
            concept.links <- c()
            studyConcepts <- getConcepts(study.name)
            for (toMatch in concept.match) {
                # TODO: this might result in the same concept being included multiple times
                conceptMatch <- grep(toMatch, studyConcepts$name)[1]
                if (is.na(conceptMatch)) {
                    warning(paste("No match found for:", toMatch))
                } else {
                    concept.links <- c(concept.links, studyConcepts$api.link.self.href[conceptMatch])
                }
            }
        } else {
            concept.links <- paste("/studies/", study.name, sep = "")
        }
    }

    if (length(concept.links) == 0L) {
        warning("No concepts selected or found to match your arguments.")
        return(NULL)
    }

    listOfObservations <- list()

    for (oneLink in concept.links) {
        serverResult <- .transmartGetJSON(paste(oneLink, "/observations", sep = "")) 
        listOfObservations <- c(listOfObservations, serverResult$observations)
    }

    if (as.data.frame) {
        dataFrameObservations <- .listToDataFrame(listOfObservations)
        subjectConceptPairs <- table(dataFrameObservations[ , match(c("label", "subject.id"),
                colnames(dataFrameObservations))])
        if(any(subjectConceptPairs>1)) {
            warning("Your results contain multiple values per subject-concept pair. One of the input concepts is probably a child concept of another. Only the first occurence will be included.")
        }

        conceptNames <- as.factor(dataFrameObservations$label)
        labelComponents <- matrix(nrow = 0, ncol = 0)
        for (level in strsplit(levels(conceptNames), split = "\\\\")) {
            labelComponents <- rbind.fill.matrix(labelComponents, t(level))
        }
        if (length(levels(conceptNames)) > 1) {
            labelComponents <-
                    labelComponents[ , -which(apply(labelComponents, 2, function(x) length(unique(x)))==1), drop = FALSE]
        }
        levels(conceptNames) <- apply(labelComponents, 1, function(x) paste(x[!is.na(x)&x!=""], collapse = "_"))
        conceptColumns <- grep("concept\\.|label", colnames(dataFrameObservations))

        conceptInfo <- unique(cbind(conceptNames, dataFrameObservations[ , conceptColumns, drop = FALSE]))
        conceptInfo$conceptNames <- as.character(conceptInfo$conceptNames)
        dataFrameObservations <- dataFrameObservations[ , -conceptColumns, drop = FALSE]
        dataFrameObservations <- cbind(dataFrameObservations, conceptNames)

        subjectIdColumn <- grep("subject.id", colnames(dataFrameObservations))
        subjectColumns <- setdiff(grep("subject\\.", colnames(dataFrameObservations)), subjectIdColumn)

        subjectInfo <- unique(cbind(dataFrameObservations[ , subjectIdColumn, drop = FALSE],
                dataFrameObservations[ , subjectColumns, drop = FALSE]))
        dataFrameObservations <- dataFrameObservations[ , -subjectColumns, drop = FALSE]
        castedObservations <- cast(dataFrameObservations, subject.id ~ conceptNames, fun.aggregate = function(x) {x[1]})
        castedObservations <- castedObservations[ , c("subject.id", as.character(unique(conceptNames))), drop = FALSE]

        factorizedColumns <- which(unlist(lapply(castedObservations, is.factor)))
        for (factorizedColumn in factorizedColumns) {
            castedObservations[ , factorizedColumn] <- levels(castedObservations[ , factorizedColumn])[castedObservations[ , factorizedColumn]]
        }

        return(list(observations = castedObservations, subjectInfo = subjectInfo, conceptInfo = conceptInfo))
    }
    listOfObservations
}

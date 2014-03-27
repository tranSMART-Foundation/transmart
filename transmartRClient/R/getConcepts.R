# Copyright (c) 2014 The Hyve B.V.
# This code is licensed under the GNU General Public License,
# version 3, or (at your option) any later version.

getConcepts <- function(study.name, as.data.frame = TRUE, cull.columns = TRUE) {
    .checkTransmartConnection()

    serverResult <- .transmartServerGetRequest( paste("/studies/", study.name, "/concepts", sep=""), use.HAL = TRUE)
    listOfConcepts <- serverResult$ontology_terms

    if (as.data.frame) {
      dataFrameConcepts <- .listToDataFrame(listOfConcepts)
      if (cull.columns) {
        columnsToKeep <- match(c("name", "fullName", "api.link.self.href"), names(dataFrameConcepts))
        return(dataFrameConcepts[ , columnsToKeep])
      }
      return(dataFrameConcepts)
    }

    listOfConcepts
}

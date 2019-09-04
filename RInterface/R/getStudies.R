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
# with this program. If not, see <http://www.gnu.org/licenses/>..

getStudies <- function(name.match = "", as.data.frame = TRUE, cull.columns = TRUE) {
    .ensureTransmartConnection()

    serverResult <- .transmartGetJSON("/studies")
    listOfStudies <- serverResult$studies
    
    n <- length(listOfStudies)
    if (n == 0) {
        message("Empty list of studies received.")
    }
    
    studyNames <- sapply(listOfStudies, FUN = function(x) { x[["id"]] })
    names(listOfStudies) <- studyNames
    listOfStudies <- listOfStudies[ grep(name.match, studyNames) ]
    if (as.data.frame) {
        dataFrameStudies <- .listToDataFrame(listOfStudies)
        if (cull.columns) {
            columnsToKeep <- match(c("id", "api.link.self.href", "ontologyTerm.fullName"), names(dataFrameStudies))
            if (n > 0 && any(is.na(columnsToKeep))) {
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

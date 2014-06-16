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

test.protobufParsing.acgh <- function() {
    protobufFileLocation <- 
            system.file("unittests/resources/sample-acgh-acgh_values.protobuf", package="transmartRClient")
    
    protobufFile <- file(protobufFileLocation, open = "rb")
    binaryContent <- readBin(protobufFile, raw(), n = file.info(protobufFileLocation)$size)
    
    result <- transmartRClient:::.parseHighdimData(binaryContent)
    
    checkTrue(exists("data", where = result))
    checkTrue(exists("labelToBioMarkerMap", where = result))
    checkEquals(dim(result$data), c(2, 21))
    checkEquals(result$data$assayId, c("-3002", "-3001"))
    checkEquals(mean(result$data$cytoband2.probabilityOfGain), 0.18)
    
    close(protobufFile)
}

test.protobufParsing.mrna.default_real_projection <- function() {
    protobufFileLocation <- 
        system.file("unittests/resources/sample-mrna-default_real_projection.protobuf", package="transmartRClient")
    
    protobufFile <- file(protobufFileLocation, open = "rb")
    binaryContent <- readBin(protobufFile, raw(), n = file.info(protobufFileLocation)$size + 1)
    
    result <- transmartRClient:::.parseHighdimData(binaryContent)
    
    checkTrue(exists("data", where = result))
    checkTrue(exists("labelToBioMarkerMap", where = result))
    checkEquals(dim(result$data), c(2, 10))
    checkEquals(result$data$assayId, c("-402", "-401"))
    checkEquals(mean(result$data$X1553513_at), 0.55)
    
    close(protobufFile)
}

test.protobufParsing.mrna.all_data <- function() {
    protobufFileLocation <- 
        system.file("unittests/resources/sample-mrna-all_data.protobuf", package="transmartRClient")
    
    protobufFile <- file(protobufFileLocation, open = "rb")
    binaryContent <- readBin(protobufFile, raw(), n = file.info(protobufFileLocation)$size + 1)
    
    result <- transmartRClient:::.parseHighdimData(binaryContent)
    
    checkTrue(exists("data", where = result))
    checkTrue(exists("labelToBioMarkerMap", where = result))
    checkEquals(dim(result$data), c(2, 19))
    checkEquals(result$data$assayId, c("-402", "-401"))
    checkEquals(mean(result$data$X1553513_at.logIntensity), -0.8685)
    
    close(protobufFile)
}

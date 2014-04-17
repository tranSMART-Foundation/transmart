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

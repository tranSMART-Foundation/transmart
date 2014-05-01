.onAttach <- function(...) {
    protoFileLocation <- system.file("extdata", "highdim.proto", package = "transmartRClient")
    readProtoFiles(protoFileLocation)
} 

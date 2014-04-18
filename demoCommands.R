# This file contains some basic demo commands

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
require("transmartRClient")
connectToTransmart("http://test-api.thehyve.net/transmart")

studies <- getStudies()
print(studies)

allObservations <- getObservations(studies$name[1], as.data.frame = T)

concepts <- getConcepts(studies$name[1])
# retrieve observations for study 1 for the first concept containing "e"
observations <- getObservations(studies$name[1], concept.match = "e")
# retrieve observations belonging to the first two concept by using the api.links contained in the getConcepts-result
observations <- getObservations(studies$name[1], concept.links = concepts$api.link.self.href[c(1,2)])

# if a concept contains high dimensional data, use the following command to obtain this data
getHighdimData(study.name = "GSE8581", concept.match = "Lung")
# you will be told that one of the listed projections needs to be selected. The following will return the actual data.
data <- getHighdimData(study.name = "GSE8581", concept.match = "Lung", projection = "zscore")
names(data)
data[["data"]][1:10,1:10]
data[[2]]["214503_x_at"]
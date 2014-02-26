# This file is added only as a development sandbox. It uses R's development
# environment to load the package in isolation and contains some example calls.

# set dev environment and load package
require(devtools)
dev_mode(F)
dev_mode(T)
setwd("~/Projects/transmart-rclient")
load_all("transmartRClient")

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
AuthenticateWithTransmart("http://test-build.thehyve.net")
ConnectToTransmart("http://test-build.thehyve.net")
studies <- getStudies(as.data.frame = T)
subjects <- getSubjects(studies$name[1], as.data.frame = T)
observations <- getObservations(studies$name[1], subjects$id[1], as.data.frame = T)
observations <- getObservations(studies$name[1], as.data.frame = T)

# create skeleton package to generate the Rd files per function
setwd("~/Projects/transmart-rclient/transmartRClient/R/")
sapply(list.files(), source)
functionsToDocument <- c("AuthenticateWithTransmart", "ConnectToTransmart", "getStudies", "getSubjects", "getObservations")
package.skeleton(list = functionsToDocument, name = "transmartRClient", path = "~/Projects/transmart-rclient/skeleton/")

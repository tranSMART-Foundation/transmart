# set dev environment and load package
require(devtools)
dev_mode(F)
dev_mode(T)

setwd("~/Projects/transmart-rclient")
load_all("transmartRClient")

#AuthenticateWithTransmart()
ConnectToTransmart()
studies <- getStudies(as.data.frame = T)
subjects <- getSubjects(studies$name[1], as.data.frame = T)
observations <- getObservations(studies$name[1], subjects$id[1], as.data.frame = T)

# create skeleton package
setwd("~/Projects/transmart-rclient/transmartRClient/R/")
sapply(list.files(),source)
functionsToDocument <- c("AuthenticateWithTransmart", "ConnectToTransmart", "getStudies", "getSubjects", "getObservations")
package.skeleton(list = functionsToDocument, name = "transmartRClient", path = "~/Projects/transmart-rclient/skeleton/")

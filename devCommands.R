# This file contains some potentially useful development commands for 
# re-installing the package, running some demo calls, and R-document creation

# Unload+uninstall previous version, and install+load transmartRClient package from source
detach("package:transmartRClient")
remove.packages("transmartRClient")
# Despite unloading and uninstalling the R client, you should restart your R session
# to ensure removal of the individually loaded functions at this point
pathOfPackageSource <- "~/Projects/transmart-rclient/transmartRClient"
install.packages(pathOfPackageSource, repos = NULL, type = "source")
require("transmartRClient")

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
AuthenticateWithTransmart("test-api.thehyve.net")
ConnectToTransmart("test-api.thehyve.net")
studies <- getStudies()
subjects <- getSubjects(studies$name[2])
observations <- getObservations(studies$name[2], subjects$id[1], as.data.frame = T)
observations <- getObservations(studies$name[2], as.data.frame = T)

# create skeleton package to generate the Rd files per function
setwd("~/Projects/transmart-rclient/transmartRClient/R/")
sapply(list.files(), source)
functionsToInclude <- c("AuthenticateWithTransmart", "ConnectToTransmart",
                         "getStudies", "getSubjects", "getObservations")
package.skeleton(list = functionsToDocument, name = "transmartRClient", 
                 path = "~/Projects/transmart-rclient/skeleton/")


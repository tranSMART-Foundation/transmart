# set dev environment and load package
require(devtools)
dev_mode(F)
dev_mode(T)

setwd("~/Projects/transmart-rclient")
load_all("transmartRClient")
#test("transmartRClient")

#AuthenticateWithTransmart()
ConnectToTransmart()
studies <- getStudies()
subjects <- getSubjects(studies$GSE8581SERIES)



# to be run in seperate R session
code_path <- "~/Projects/transmart-rclient/transmartRClient/R"
test_path <- "~/Projects/transmart-rclient/transmartRClient/inst/tests"
library(testthat)
auto_test(code_path, test_path)

# create skeleton package
setwd("~/Projects/transmart-rclient/transmartRClient/R/")
sapply(list.files(),source)
functionsToDocument <- c("AuthenticateClientSession", "getStudies", "getSubjects")
package.skeleton(list = functionsToDocument, name = "transmartRClient", path = "~/Projects/transmart-rclient/skeleton/")

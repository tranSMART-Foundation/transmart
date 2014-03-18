# This file contains some pointers for running a small demo, package refreshing, and skeleton package creation

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
require("transmartRClient")
options(verbose = FALSE)
connectToTransmart("test-api.thehyve.net", use.authentication = TRUE)
studies <- getStudies()
subjects <- getSubjects(studies$name[3])
observations <- getObservations(studies$name[3], subjects$id[1], as.data.frame = T)
observations <- getObservations(studies$name[1], as.data.frame = T)


# Clean install of transmartRClient: unload and uninstall, clean environment, and then re-install package from source
detach("package:transmartRClient")
remove.packages("transmartRClient")
# Remove manually declared functions
rm(list = lsf.str())
# Also, I have noticed the puzzling result that R sometimes sources old file versions (cache problem?)
# Recommended to restart your R console at this point. Please let me know if you know how to handle this more elegantly.
pathOfPackageSource <- "~/Projects/transmart-rclient/transmartRClient"
install.packages(pathOfPackageSource, clean = TRUE, repos = NULL, type = "source")


# create skeleton package: automises documentation and package base structure
sourcePath <- ("~/Projects/transmart-rclient/transmartRClient/R")
skeletonPath <- ("~/Projects/transmart-rclient/skeleton")
sourceFiles <- list.files(sourcePath, pattern = "[.][Rr]$", full.names = TRUE)
# optional, tidy up source files
require("formatR")
for (inputFile in sourceFiles) {
    outputFile = sub("[.][Rr]$", "_tidy.R", inputFile)
    tidy.source(source = inputFile, file = outputFile, width.cutoff = 120)
}
# create sekeleton
package.skeleton(code_files = sourceFiles, name = "transmartRClient", path = skeletonPath)

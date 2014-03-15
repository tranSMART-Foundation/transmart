# This file contains some pointers for running a small demo, package refreshing, and skeleton package creation

# Example steps to authenticate with, connect to, and retrieve data from tranSMART
require("transmartRClient")
ConnectToTransmart("test-build.thehyve.net")
studies <- getStudies()
subjects <- getSubjects(studies$name[2])
observations <- getObservations(studies$name[1], subjects$id[1], as.data.frame = T)
observations <- getObservations(studies$name[2], as.data.frame = T)

# Clean install of transmartRClient: unload and uninstall, clean environment, and then re-install package from source
detach("package:transmartRClient")
remove.packages("transmartRClient")
# Remove manually declared functions
rm(list = lsf.str())
# Also, I have noticed the puzzling result that R sometimes sources old file versions (cache problem?)
# Recommended to restart your R console at this point
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

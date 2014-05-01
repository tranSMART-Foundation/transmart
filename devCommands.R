# This file contains some potentially useful commands for people who wish to develop with this package

# Clean install of transmartRClient: unload and uninstall, clean environment, and then re-install package from source
detach("package:transmartRClient")
remove.packages("transmartRClient")
# Remove manually declared functions
rm(list = lsf.str())
#rm(list = ls(all.names = TRUE)) # optional: remove all other objects in current environment

# Also, I have noticed the puzzling result that R sometimes sources old file versions when you reinstall directly (cache problem?)
# Recommended to restart your R console at this point. Please let edit this file appropriatly if you know how to handle this issue more elegantly.
# Then, reinstall package
pathOfPackageSource <- "~/Projects/transmart-rclient/transmartRClient"
install.packages(pathOfPackageSource, repos = NULL, type = "source")




# create skeleton package: automises documentation and package base directory structure
sourcePath <- paste(pathOfPackageSource, "/R", sep='')
skeletonPath <- paste(pathOfPackageSource, "/skeleton", sep='')
sourceFiles <- list.files(sourcePath, pattern = "[.][Rr]$", full.names = TRUE)
# optional, tidy up source files
require("formatR")
for (inputFile in sourceFiles) {
    outputFile = sub("[.][Rr]$", "_tidy.R", inputFile)
    tidy.source(source = inputFile, file = outputFile, width.cutoff = 120)
}
# create sekeleton
package.skeleton(code_files = sourceFiles, name = "transmartRClient", path = skeletonPath)

# Copyright 2014, 2015 The Hyve B.V.
# Copyright 2014 Janssen Research & Development, LLC.
#
# This file is part of tranSMART R Client: R package allowing access to
# tranSMART's data via its RESTful API.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or (at your
# option) any later version, along with the following terms:
#
#   1. You may convey a work based on this program in accordance with
#      section 5, provided that you retain the above notices.
#   2. You may convey verbatim copies of this program code as you receive
#      it, in any medium, provided that you retain the above notices.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
# Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program. If not, see <http://www.gnu.org/licenses/>.

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

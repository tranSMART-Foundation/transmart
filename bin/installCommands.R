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


# Notes for first time installers:

# The package transmartRClient depends on five packages: RCurl, rjson, RProtoBuf, plyr, hash, and reshape.
# You can install them as follows:
install.packages(pkgs=c("RCurl", "rjson", "RProtoBuf", "plyr", "hash", "reshape"))

# RProtoBuf depends on the system protobuf headers. For Ubuntu you will need to
# install the libprotoc-dev and libprotobuf-dev packages.

# One nasty issue encountered in some MacOSX versions (at least in 10.9) is that
# the R version installed via homebrew runs into a libl library error. Please
# use the default R installed via the CRAN installer, which does not have this
# issue

# Now you can install the transmartRClient. First, point the following path to the location of your "transmartRClient"
# directory, which is the parent directory of the directory where this installCommands.R file is located
pathOfPackageSource <- "somePath/repository-location/RInterface"
# The following command will install the package from its source files into your standard library
install.packages(pathOfPackageSource, repos = NULL, type = "source")

# If all was succesfull, the following should load the transmartRClient
library("transmartRClient")

# Notes for first time installers: 

# The package transmartRClient depends on five packages: RCurl, RJSONIO, RProtoBuf, plyr, hash, and reshape.
# You can install them as follows:
install.packages(pkgs=c("RCurl", "RJSONIO", "RProtoBuf", "plyr", "hash", "reshape"))

# One nasty issue encountered in MacOSX is that the R version installed via homebrew runs into a libl library error:
#   please use the default R installed via the CRAN installer, which does not have this issue

# Now you can install the transmartRClient. First, point the following path to the location of your "transmartRClient"
# directory, which is a sub-directory of where this installCommands.R file is located
pathOfPackageSource <- "somePath/repository-location/transmartRClient"
# The following command will install the package from its source files into your standard library
install.packages(pathOfPackageSource, repos = NULL, type = "source")

# If all was succesfull, the following should load the transmartRClient
library("transmartRClient")

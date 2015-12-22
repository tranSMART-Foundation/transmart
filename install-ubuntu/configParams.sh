# parameters for build

# BUILD_BASE is the folder into which the build will be constructed
# and from which it will be run. The default is $HOME/transmart
# and this value will work
 
setenv BUILD_BASE=$HOME/transmart


# TRANSMART_VERSION is the version to be build; 
# there is no default for this value and it is required;
# acceptable values are release-1.2.4, release-1.2.5-Beta, and release-1.2.4
# the values must correspond to branches or tags in both the archives
# https://github.com/tranSMART-Foundation/transmart-data and
# https://github.com/tranSMART-Foundation/tranSMART-ETL

# setenv TRANSMART_VERSION=release-1.2.4
# setenv TRANSMART_VERSION=release-1.2.5-Beta
# setenv TRANSMART_VERSION=release-1.2.5
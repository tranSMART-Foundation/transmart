#!/bin/bash

# ---------------------------
# Check the version of those command line element that need specific versions
# ---------------------------

. ./versionCompare.sh

# check java version, 1.7 or higher
desiredjavaVersion="1.7"
javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
reportCheckOrHigher "java" $desiredjavaVersion $javaVersion

# check php version, 5.4 or higher
desiredPhpVersion="5.4"
phpVersion=$(php --version | awk -F '^PHP ' '{print $2}' | awk -F 'ubuntu' '{print $1}')
reportCheckOrHigher "php" $desiredPhpVersion $phpVersion

# check R version, exactly 3.1.2
desiredRVersion="3.1.2"
RVersion=$(R --version | awk -F '^R version ' '{print $2}')
reportCheckExact "R" $desiredRVersion $RVersion

# check psql version, 9.2 or higher
desiredPsqlVersion="9.2"
version=$(psql --version)
psqlVersion=$( echo "$version" | awk -F '^psql .PostgreSQL. ' '{print $2}')
reportCheckOrHigher "psql" $desiredPsqlVersion $psqlVersion

# check groovy version, 2.1 or higher
desiredGroovyVersion="2.1"
groovyVersion=$(groovy --version | awk -F '^Groovy Version: ' '{print $2}')
reportCheckOrHigher "groovy" $desiredGroovyVersion $groovyVersion


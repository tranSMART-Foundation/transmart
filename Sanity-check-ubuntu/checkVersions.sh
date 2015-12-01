#!/bin/bash

# ---------------------------
# Check the version of those command line element that need specific versions
# ---------------------------

. ./versionCompare.sh

# check java version, 1.7 or higher

javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
javaVersionProbe=$(echo $javaVersion | tr '_' '.')
$(vercomp "1.7" $javaVersionProbe)
versionTest=$?
if [ "$versionTest" -gt 0 ]; then
    echo "Java version, $javaVersion, is good!"
else 
    echo "Expected java verison 1.7 or higher; currently $javaVersion; needs to be upgraded"
fi

# check php version, 5.4 or higher
phpVersion=$(php --version | awk -F '^PHP ' '{print $2}' | awk -F 'ubuntu' '{print $1}')
phpVersionProbe=$(echo $phpVersion | tr "-" ".")
$(vercomp "5.4" $phpVersionProbe)
versionTest=$?
if [ "$versionTest" -gt 0 ]; then
    echo "Php version, $phpVersion, is good!"
else
    echo "Expected php verison 5.4 or higher; currently $phpVersion; needs to be upgraded"
fi

# check R version, 3.1.2

# check psql version, 9.2 or higher

# check groovy version, 2.1 or higher


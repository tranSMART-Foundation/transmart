#!/bin/bash

# ---------------------------
# Check the version of those command line element that need specific versions
# ---------------------------

. ./versionCompare.sh

javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
javaVersion=$(echo $javaVersion | tr '_' '.')
$(vercomp "1.7" $javaVersion)
versionTest=$?
if [ "$versionTest" -gt 0 ]; then
    echo "Java version, $javaVersion, is good!"
else 
    echo "Expected java verison 1.7 or better; currently $javaVersion; needs to be upgraded"
fi



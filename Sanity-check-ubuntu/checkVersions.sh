#!/bin/bash

# ---------------------------
# Check the version of those command line element that need specific versions
# ---------------------------

. ./versionCompare.sh

javaVersion=$(java -version 2>&1 | awk -F '"' '{print $2}')
echo "$javaVersion"
javaVersion=$(echo $javaVersion | tr '_' '.')
echo "$javaVersion"
echo $(vercomp "1.7" $javaVersion)
if [ $(vercomp "1.7" $javaVersion) -gt 0 ]; then
    echo "Java version, $javaVersion, is good!"
else 
    echo "Expected java verison 1.7 or better; currently $javaVersion; needs to be upgraded"
fi


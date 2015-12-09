#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the context of R
# ******************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh
. ./versionCompare.sh

pathForRBin=$HOME/transmart/transmart-data/R/root/bin

echo "-------------------------------------"
echo "|  Checking for basics and packages required by R;"
echo "|    if anything is reproted as missing, then recheck"
echo "|    the detailed instructions to installing the missing items"
echo "-------------------------------------"

echo "Checking for R bin on path"
if [ -p $pathForRBin ] ; then
	echo "WARNING - the bin file for the correct version of R is not on your path"
	echo "After reboot and login, check for this element on your path:"
	echo "$pathForRBin"
	echo "If not there, the make sure that it is added in your bash profile"
	export PATH=$pathForRBin:$PATH
fi

echo "checking for R itself"
if ! checkForCommandLineTool "R"; then
    echo "R itself is missing; nothing further can be checked"
    exit 1
fi

# check R version, exactly 3.1.2
desiredRVersion="3.1.2"
RVersion=$(R --version | awk -F '^R version ' '{print $2}')
reportCheckExact "R" $desiredRVersion $RVersion
returnFlag=$?
if [ returnFlag = 1 ] then;
	echo "R version problems; aborting check of R and Rpackages
	exit 1
fi

R --vanilla --slave < probeRserve.R > /dev/null
results=$?

if (( ! $results )); then
    echo "OK; Rserve and the other packages required appear to be available"
else
    echo "One or more required package is missing from R;"
    echo "  see probeRserve.R for a method of checking the details."
fi 

exit $results

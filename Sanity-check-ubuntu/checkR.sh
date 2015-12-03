#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the context of R
# ******************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh

echo "-------------------------------------"
echo "|  Checking for basics and packages required by R;"
echo "|    if anything is reproted as missing, then recheck"
echo "|    the detailed instructions to installing the missing items"
echo "-------------------------------------"

echo "checking for R itself"
if ! checkForCommandLineTool "R"; then
    echo "R itself is missing; nothing further can be checked"
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

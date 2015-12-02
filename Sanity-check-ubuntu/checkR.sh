#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the context of R
# ******************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh

echo "-------------------------------------"
echo "Checking for basics and packages required by R;"
echo "  if anything is reproted as missing, then recheck:"
echo "  the instructions to installing the missing items"
echo "-------------------------------------"

echo "checking for R itself"
if ! checkForCommandLineTool "R"; then
    echo "R itself is missing; nothing further can be checked"
    exit 1
fi

R --vanilla --slave < probeRserve.R > probeRserve.log
results=$(cat probeRserve.log)

echo $results

if [[ $results == *"ok"* ]]; then
    echo "Rserve and the other packages required appear to be available"
    exit 0
fi

echo "One or more required package is missing from R; see probeRserve.log"
exit 1

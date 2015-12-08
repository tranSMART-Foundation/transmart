#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing items in the list of basic
# linux command lines that are needed for the tranSAMRT install and data loading
# ********************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh

echo "-------------------------------------"
echo "|  Checking for basic command-line tools; if any of the following does not exist,"
echo "|  then recheck the instructions for installing the missing items"
echo "-------------------------------------"
probe=0
for command in git make java ant mvn tar rsync php g++ gfortran psql groovy grails
do
    if ! checkForCommandLineTool "$command"; then
        probe=1
    fi
done

exit $probe


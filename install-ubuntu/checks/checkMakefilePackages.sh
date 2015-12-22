#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing items in the list of basic
# linux command lines that are needed for the tranSAMRT install and data loading
# ********************************************************************************

# # ------------------ helper function -------------------
. ./basicsHelper.sh

echo "-------------------------------------"
echo "|  Checking for basic command-line tools; that are installed by make"
echo "|  if any of the following does not exist,"
echo "|  then recheck the instructions for installing the missing items; "
echo "|  also ref: installTransmart.sh,'Install of basic tools' "
echo "-------------------------------------"

probe=0
for command in git make java tar rsync php g++ gfortran
do
    if ! checkForCommandLineTool "$command"; then
        probe=1
    fi
done

exit $probe


#!/bin/bash

# ******************************************************************************
# This script checks for and reports missing items in the list of basic
# linux command lines that are needed for the tranSMART install and data loading
# ******************************************************************************

# # ------------------ helper function -------------------
. $TMSCRIPTS_BASE/checks/basicsHelper.sh

echo "-------------------------------------------------------------------"
echo "|  Checking for basic command-line tools that are installed by make"
echo "|  If any of the following does not exist,"
echo "|  then recheck the instructions for installing the missing items;"
echo "|  also ref: InstallTransmart.sh, 'Install of basic tools'"
echo "-------------------------------------------------------------------"

probe=0
for command in git make java tar rsync php g++ gfortran
do
    if ! checkForCommandLineTool "$command"; then
        probe=1
    fi
done

exit $probe


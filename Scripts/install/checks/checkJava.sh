#!/bin/bash

# ********************************************************
# This script checks for and reports missing java commands
# needed for the tranSMART install and data loading
# ********************************************************

# # ------------------ helper function -------------------
. $TMSCRIPTS_BASE/checks/basicsHelper.sh

echo "--------------------------------------------------------------------------------"
echo "|  Checking for java command-line tools; if any of the following does not exist,"
echo "|  then recheck the instructions for installing the missing items"
echo "--------------------------------------------------------------------------------"

probe=0
for command in java javac
do
    if ! checkForCommandLineTool "$command"; then
        probe=1
    fi
done

exit $probe


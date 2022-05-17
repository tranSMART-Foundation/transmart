#!/bin/bash

# **********************************************************
# This script checks for and reports missing groovy compiler
# needed for the tranSMART install and data loading
# **********************************************************

# # ------------------ helper function -------------------
. $TMSCRIPTS_BASE/checks/basicsHelper.sh

echo "---------------------------------------------------------------"
echo "|  Checking for groovy; if any of the following does not exist,"
echo "|  then recheck the instructions for installing"
echo "---------------------------------------------------------------"

# Depends on the path set up by sourcing transmart-data/vars

probe=0
for command in groovy
do
    if ! checkForCommandLineTool "$command"; then
        probe=1
    fi
done

exit $probe


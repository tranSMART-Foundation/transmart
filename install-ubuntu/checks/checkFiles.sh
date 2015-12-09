#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required end point files and folders"
echo "-------------------------------------"

base="$HOME/transmart/transmart-data"
baseEnv="$base/env"
baseR="$base/R"
baseConfig="/usr/share/tomcat7/.grails/transmartConfig"
baseWebapps="/var/lib/tomcat7/webapps"
baseLogs="/var/lib/tomcat7/logs"

returnValue=0

filepath="$baseR/root/bin"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; repeat that step"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required end point files and folders are present"
fi

exit $returnValue
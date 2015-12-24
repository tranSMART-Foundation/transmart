#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required files and folders from basic set up"
echo "-------------------------------------"

base="$INSTALL_BASE/transmart-data"
baseEnv="$base/env"
baseR="$base/R"
baseConfig="/usr/share/tomcat7/.grails/transmartConfig"
baseWebapps="/var/lib/tomcat7/webapps"
baseLogs="/var/lib/tomcat7/logs"

returnValue=0

filepath="$base/vars"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  1s required and does not exist; this should have been created"
	echo "  in the 'basics' step of the install; repeat that step"
	returnValue=1
fi

for envDir in data-integration tranSMART-ETL
do
	filepath="$baseEnv/$envDir"
	if [ ! -e "$filepath" ]; then
		echo "The directory/folder at $filepath"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildEnv' step of the install; repeat that step"
		returnValue=1
	fi
done

if [ 0 = $returnValue ] ; then
	echo "|  ALL Required files and folders from basic set up are present"
	echo "-------------------------------------"
fi

exit $returnValue
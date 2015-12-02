#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "Checking for required files and folders"
echo "-------------------------------------"

base="$HOME/transmart/transmart-data"

returnValue=0

filepath="$base/vars"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  1s required and does not exist; this should have been created"
	echo "  in the 'basics' step of the install; check the log and repeat that step"
	returnValue=1
fi

baseEnv="$base/env"
for envDir in data-integration tranSMART-ETL
do
	filepath="$baseEnv/$envDir"
	if [ ! -e "$filepath" ]; then
		echo "The directory/folder at $filepath"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildEnv' step of the install; check the log and repeat that step"
		returnValue=1
	fi
done

baseR="$base/R"
filepath="$baseR/root/bin"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; check the log and repeat that step"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required files and folders are present"
fi

exit $returnValue
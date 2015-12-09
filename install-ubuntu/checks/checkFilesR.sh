#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required files and folders for the R install"
echo "-------------------------------------"

base="$HOME/transmart/transmart-data"
baseR="$base/R"

returnValue=0

filepath="$baseR"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; repeat that step"
	returnValue=1
fi

filepath="$baseR/root"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; repeat that step"
	returnValue=1
fi

filepath="$baseR/root/bin"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; repeat that step"
	returnValue=1
fi
 

if [ 0 = $returnValue ] ; then
	echo "All required files and folders for the R install are present"
fi

exit $returnValue
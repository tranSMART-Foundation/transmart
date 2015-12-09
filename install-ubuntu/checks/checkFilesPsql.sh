#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************
. ./postgresCheckHelper.sh

echo "-------------------------------------"
echo "|  Checking for postgres support folders"
echo "-------------------------------------"

returnValue=0

for folderName in biomart deapp indx search_app transmart
do
	if ! checkForPostgresTablespaceFolder $folderName; then
		echo "The required postgres tablespace folder $folderName"
		echo "  is missing; this should have been created as part of the basics"
		echo "  stage in the install process; recheck that step"
		returnValue=1
	fi
done

if [ 0 = $returnValue ] ; then
	echo "All required postgres support folders are present"
fi

exit $returnValue
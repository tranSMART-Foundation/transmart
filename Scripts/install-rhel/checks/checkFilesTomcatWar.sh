#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required tomcat war files"
echo "-------------------------------------"

baseWebapps="/var/lib/tomcat7/webapps"

returnValue=0

filepath="$baseWebapps/transmart.war"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildWarfiles' step of the install; repeat that step"
	returnValue=1
fi

filepath="$baseWebapps/gwava.war"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildWarfiles' step of the install; repeat that step"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required required tomcat war files are present"
fi

exit $returnValue
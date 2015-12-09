#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required transmart/grails configuration files"
echo "-------------------------------------"

baseConfig="/usr/share/tomcat7/.grails/transmartConfig"

returnValue=0
 
filepath="$baseConfig/Config.groovy"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildConfig' step of the install; repeat that step"
	returnValue=1
fi

filepath="$baseConfig/DataSource.groovy"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildConfig' step of the install; repeat that step"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required required transmart/grails configuration files are present"
fi

exit $returnValue
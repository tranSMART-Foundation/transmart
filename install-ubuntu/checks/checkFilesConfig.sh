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

filepath="$baseR/root/bin"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; repeat that step"
	returnValue=1
fi
 
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

filepath="$baseLogs/catalina.out"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  does not exist; it is likely the case that tomcat was never started;"
	echo "  start it with the command: 'sudo service tomcat7 restart'"
	returnValue=1
fi

filepath="$baseLogs/transmart.log"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  does not exist; one posibility is that tomcat was never started;"
	echo "  start it with the command: 'sudo service tomcat7 restart'"
	echo "  another possibility is that transmart failed to start"
	echo "  check the log file at $baseLogs/catalina.out for possible problems"
	returnValue=1
fi

filepath="$baseWebapps/transmart"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; one posibility is that tomcat is not running;"
	echo "  start it with the command: 'sudo service tomcat7 restart'"
	echo "  another possibility is that transmart failed to start"
	echo "  check the log file at $baseLogs/catalina.out for possible problems"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required end point files and folders are present"
fi

exit $returnValue
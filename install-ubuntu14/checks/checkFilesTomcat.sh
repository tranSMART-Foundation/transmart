#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for required tomcat files and folders"
echo "-------------------------------------"

baseWebapps="/var/lib/tomcat7/webapps"
baseLogs="/var/lib/tomcat7/logs"

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
	echo "All required tomcat files and folders are present"
fi

exit $returnValue
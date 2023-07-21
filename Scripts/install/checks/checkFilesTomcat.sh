#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# **********************************************************************************

echo "-------------------------------------------------"
echo "|  Checking for required tomcat files and folders"
echo "-------------------------------------------------"

TMINSTALL_OS="$1"
TMINSTALL_OSVERSION="$2"

case $TMINSTALL_OS in
    ubuntu)
	echo "testing ubuntu version"
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		baseWebapps="/var/lib/tomcat8/webapps"
		baseLogs="/var/lib/tomcat8/logs"
		tomcatservice="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		baseWebapps="/var/lib/tomcat9/webapps"
		baseLogs="/var/lib/tomcat9/logs"
		tomcatservice="tomcat9"
		;;
	esac
esac

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
	echo "  start it with the command: 'sudo service $tomcatservice restart'"
	returnValue=1
fi

filepath="$baseLogs/transmart.log"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  does not exist; one posibility is that tomcat was never started;"
	echo "  start it with the command: 'sudo service $tomcatservice restart'"
	echo "  another possibility is that transmart failed to start"
	echo "  check the log file at $baseLogs/catalina.out for possible problems"
	returnValue=1
fi

filepath="$baseWebapps/transmart"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; one posibility is that tomcat is not running;"
	echo "  start it with the command: 'sudo service $tomcatservice restart'"
	echo "  another possibility is that transmart failed to start"
	echo "  check the log file at $baseLogs/catalina.out for possible problems"
	returnValue=1
fi

filepath="$baseWebapps/gwava"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; one posibility is that tomcat is not running;"
	echo "  start it with the command: 'sudo service $tomcatservice restart'"
	echo "  another possibility is that transmart failed to start"
	echo "  check the log file at $baseLogs/catalina.out for possible problems"
	returnValue=1
fi

if [ 0 = $returnValue ] ; then
	echo "All required tomcat files and folders are present"
fi

exit $returnValue

#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# **********************************************************************************

echo "-----------------------------------------"
echo "|  Checking for required tomcat war files"
echo "-----------------------------------------"

TMINSTALL_OS="$1"
TMINSTALL_OSVERSION="$2"

echo "Testing OS '$TMINSTALL_OS' VERSION '$TMINSTALL_OSVERSION'"

case $TMINSTALL_OS in
    ubuntu)
	echo "testing ubuntu version"
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		baseWebapps="/var/lib/tomcat8/webapps"
		echo "set ubuntu 18 variables"
		;;
	    20.04 | 20 | 22.04 | 22)
		baseWebapps="/var/lib/tomcat9/webapps"
		echo "set ubuntu 20/22 variables"
		;;
	esac
esac

echo "baseWebapps '$baseWebapps'"

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

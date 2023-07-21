#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# ********************************************************************************

echo "------------------------------------------------------------"
echo "|  Checking for required files and folders from basic set up"
echo "------------------------------------------------------------"

base="$TMINSTALL_BASE/transmart-data"
baseEnv="$base/env"
baseR="$base/R"

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		baseWebapps="/var/lib/tomcat8/webapps"
		baseConfig="/usr/share/tomcat8/.grails/transmartConfig"
		baseLogs="/var/lib/tomcat8/logs"
		;;
	    20.04 | 20 | 22.04 | 22)
		baseWebapps="/var/lib/tomcat9/webapps"
		baseConfig="/usr/share/tomcat9/.grails/transmartConfig"
		baseLogs="/var/lib/tomcat9/logs"
		;;
	esac
esac

returnValue=0

filepath="$base/vars"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  1s required and does not exist; this should have been created"
	echo "  in the 'basics' step of the install; repeat that step"
	returnValue=1
fi

for envDir in data-integration transmart-etl
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
	echo "|  All required files and folders from basic set up are present"
	echo "---------------------------------------------------------------"
fi

exit $returnValue

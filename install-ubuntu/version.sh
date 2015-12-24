#!/bin/bash

#--------------------------
#  This script discovers and prints version information
#--------------------------

if [ -e ../VERSION.txt ] ; then
	transmartVersion=$(grep "VERSION: " ../VERSION.txt)
	scriptsVersion=$(grep "Scripts Version" ../VERSION.txt)
	echo "From VERSIONS.txt"
	if ! [ -z "$transmartVersion" ] ; then
	    echo "  tranSMART version: $transmartVersion"	
	fi
	if ! [ -z "$scriptsVersion" ] ; then
	    echo "  scripts version: $scriptsVersion"
	fi
fi

if [ -e InstallTransmart.sh ] ; then
    inFileVersion=$(grep "Version:" InstallTransmart.sh)
    if ! [ -z "$inFileVersion" ] ; then
        echo "From InstallTransmart.sh file: $inFileVersion"
    fi
fi

gitCommandExists=1
if type "git" >/dev/null 2>&1; then
    gitCommandExists=0
fi 

if ! [ -z gitCommandExists ] ; then
	gitVersion=$( git log -1 --pretty=format:"%h - %cD" )
	if ! [ -z "$gitVersion" ]; then
	    echo "From git command; version (hash code): $gitVersion"
	fi
fi

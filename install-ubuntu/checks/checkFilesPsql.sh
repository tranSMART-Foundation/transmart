#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

function checkForPostgresTablespaceFolder {
    name=$1
    checkPath=$TABLESPACES$name
    if ! [ -x "$checkPath" ] ; then
    	echo "Can not find postgres tablespace folder: $checkPath"
    	return 1
    fi
    
    x=$(la -la $TABLESPACES | grep "$name" | grep "postgres")
    if [ -z $x ] ; then
    	echo "the folder at $checkPath"
    	echo "  is not owned by 'postgres' as required"
    	return 1
    fi

	return 0
}

echo "-------------------------------------"
echo "|  Checking for postgres support folders"
echo "-------------------------------------"

varsFile=$HOME/transmart/transmart-data/vars

if [ -x "$varsFile" ] ; then
    source $HOME/transmart-data/transmart/vars
fi

echo "checking for \$TABLESPACES env variable"
if [ -z "$TABLESPACES" ] ; then
	echo "  The \$TABLESPACES end variable is not set"
	echo "  Cannot continue"
	exit 1
else
	echo "  it is set to: $TABLESPACES "
fi

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
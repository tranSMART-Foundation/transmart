#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

function checkForPostgresTablespaceFolder {
    name=$1
    checkPath=$TABLESPACES$name
    
    x=$(ls -la $TABLESPACES | grep "$name")
    if [ -z "$x" ] ; then
    	echo "the folder at $checkPath"
    	echo "  was not found; it needs to be (sudo mkdir) created"
    	return 1
    fi
    
    x=$(ls -la $TABLESPACES | grep "$name" | grep "postgres")
    if [ -z "$x" ] ; then
    	echo "the folder at $checkPath"
    	echo "  is not owned by 'postgres' as required"
    	echo "  correct with sudo chown"
    	return 1
    fi

    x=$(ls -la $TABLESPACES | grep "$name" | grep "drwx------")
    if [ -z "$x" ] ; then
    	echo "the folder at $checkPath"
    	echo "  is not set to permit flags 'drwx------' as required"
    	echo "  correct with sudo chmod"
    	return 1
    fi
    
    echo "OK - the folder at $checkPath"
    
	return 0
}

echo "-------------------------------------"
echo "|  Checking for postgres support folders"
echo "-------------------------------------"

varsFile=$HOME/transmart/transmart-data/vars

if [ -x "$varsFile" ] ; then
    source $varsFile
fi

echo "checking for \$TABLESPACES env variable"
if [ -z "$TABLESPACES" ] ; then
	echo "  The \$TABLESPACES end variable is not set"
	echo "  Cannot continue"
	exit 1
else
	echo "  it is set to: $TABLESPACES "
fi

echo "checking for the individual tablespace folders"
returnValue=0
for folderName in biomart deapp indx search_app transmart
do
	if ! checkForPostgresTablespaceFolder $folderName; then
		echo "  Something is wrong with the postgres tablespace folder $folderName"
		returnValue=1
	fi
done

if [ 0 = $returnValue ] ; then
	echo "All required postgres support folders are present"
fi

exit $returnValue
#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# this is a preliminary check; see checkFilesPsql.sh for full check
# ********************************************************************************

function checkForPostgresTablespaceFolder {

    TABLESPACES="/var/lib/postgresql/tablespaces"

    name=$1
    checkPath="$TABLESPACES/$name"
    
    x=$(ls -la $TABLESPACES | grep "$name")
    if [ -z "$x" ] ; then
    	echo "the folder at $checkPath"
    	echo "  was not found; it needs to be (sudo mkdir) created"
    	return 1
    fi
    
    echo "OK - the folder at $checkPath"
    
    return 0
}


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


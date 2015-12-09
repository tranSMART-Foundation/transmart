#!/bin/bash

# helper function to check for postgres tablespace database support folders

function checkForPostgresTablespaceFolder {
    name=$1
    checkPath=$TABLESPACES/$name
    if ! [ -x "$checkPath" ] ; then
    	echo "Can not find postgres tablespace folder: $checkPath"
    	return 1
    fi
    
    x=$(la -la $TABLESPACES | grep "$name" | grep "postgres")
    if [ -z $x ]
    	echo "the folder at $checkPath"
    	echo "  is not owned by 'postgres' as required"
    	return 1
    fi

	return 0
}
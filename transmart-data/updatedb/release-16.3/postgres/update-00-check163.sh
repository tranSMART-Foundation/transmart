#!/bin/sh -f

# Check ./vars has been run

if [ -z "$PGDATABASE" ]; then
    echo "Please source ./vars first"
    exit -1
fi


# If this script was found, then we are in the update directory so relative paths will work for includes

if [ ! -e "./update-00-check163.sh" ]; then
    echo "Please cd to the updatedb/release-16.3/postgres directory"
    exit -1
fi






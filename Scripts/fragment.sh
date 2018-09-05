#!/bin/bash

# Helper function: use gpg to verify download
# assumes <name> is downloaded file and <name>.asc is signature file
# on current directory
function verifyWithGpg {
	filename=$1
	echo "************"
	echo "verifyWithGpg $filename"
	echo "verifyWithGpg $filename.asc"
	echo "************"
	gpg --verify $filename.asc
	return $?
}

TRANSMART_DATA_NAME="transmart-data-release-16.2"
TRANSMART_DATA_TAR="$TRANSMART_DATA_NAME.tar.gz"

if [ $(verifyWithGpg "$TRANSMART_DATA_TAR") ] ; then 
	echo "++++++++++++++++++++++++++++"
	echo "+  VERIFY(gpg) failed transmart-data folder"
	echo "++++++++++++++++++++++++++++"
	exit -1
fi

echo "Done"

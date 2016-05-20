#!/bin/bash

# Helper function: use gpg to verify downaload
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

TRANSMART_ETL_NAME="tranSMART-ETL-release-16.1"
TRANSMART_ETL_TAR="$TRANSMART_ETL_NAME.tar.gz"

if [ $(verifyWithGpg "$TRANSMART_ETL_TAR") ] ; then
	echo "++++++++++++++++++++++++++++"
	echo "+  VERIFY(gpg) failed tranSMART-ETL folder"
	echo "++++++++++++++++++++++++++++"
	exit -1 
fi
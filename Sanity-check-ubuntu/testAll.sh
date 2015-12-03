#!/bin/bash

# ********************************************************************************
# This script runs all of the scripts for checking code sanity; reports return values
# ********************************************************************************

./basics.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "Some problem with the bascis tools check"
	echo "************"
fi

./checkVersions.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "Some problem with the versions check"
	echo "************"
fi

./checkFiles.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "Some problem with the required files and folders check"
	echo "************"
fi

./checkR.sh
returnValue=$?
if (( $returnValue )); then
	echo "************"
	echo "Some problem with the checks for R"
	echo "************"
fi


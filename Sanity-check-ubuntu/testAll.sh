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


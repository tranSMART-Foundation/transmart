#!/bin/bash

# ********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSAMRT install and data loading
# ********************************************************************************

base="$HOME/transmart/transmart-data"

filepath="$base/vars"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  1s required and does not exist; this should have been created"
	echo "  in the 'basics' step of the install; check the log and repeat that step"
fi

baseEnv="$base/env"
for envDir in data-integration tranSMART-ETL
do
	filepath="$baseEnv/$envDir"
	if [ ! -e "$filepath" ]; then
		echo "The directory/folder at $filepath"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildEnv' step of the install; check the log and repeat that step"
	fi
done

baseR="$base/R"
filepath="$baseR/root/bin"
if [ ! -e "$filepath" ]; then
	echo "The file at $filepath"
	echo "  is required and does not exist; this should have been created"
	echo "  in the 'buildR' step of the install; check the log and repeat that step"
fi


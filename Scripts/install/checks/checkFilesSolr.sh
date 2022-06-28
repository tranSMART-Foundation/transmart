#!/bin/bash

# **********************************************************************************
# This script checks for and reports missing files and directories that are required
# for the tranSMART install and data loading
# **********************************************************************************

echo "---------------------------------------------------------------"
echo "|  Checking for required folders and files for the solr install"
echo "---------------------------------------------------------------"

base="$TMINSTALL_BASE/transmart-data"
baseSolr="$base/solr"

returnValue=0
missingPackages=0
for filepath in "$baseSolr" "$baseSolr/solr/browse" "$baseSolr/solr/rwg" "$baseSolr/solr/sample"
do
	if [ ! -d "$filepath" ]; then
		echo "The file at $filepath"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildSolr' step of the install; repeat that step"
		returnValue=1
	fi
done
if [ $returnValue != 0 ]; then
    exit $returnvalue
fi

for filename in  "$baseSolr/start.jar"
do
	if [ ! -e "$filename" ]; then
		echo "The file at $filename"
		echo "  is required and does not exist; this should have been created"
		echo "  in the 'buildSolr' step of the install; repeat that step"
		returnValue=1
	fi
done



exit $returnValue

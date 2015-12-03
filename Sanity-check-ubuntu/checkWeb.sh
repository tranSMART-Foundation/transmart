#!/bin/bash

# ********************************************************************************
# This script checks for and reports incompatible version numbers in the 
# linux command lines that are needed for the tranSAMRT install and data loading
# ********************************************************************************

# # ------------------ source helper function -------------------
. ./checkUrl.sh

# ---------------------------
# Check to see that the expected web sites are running
# ---------------------------

echo "-------------------------------------"
echo "Check to see that the expected web sites are running"
echo "-------------------------------------"

solrUrl="http://localhost:8983/solr/#/"
gwavaUrl="http://localhost:8080/gwava/"
transmartUrl="http://localhost:8080/transmasrt"

checkURL $solrUrl
solrResults=$?

checkURL $gwavaUrl
gwavaResults=$?

checkURL $transmartUrl
transmartResults=$?

exitResults=0
if [ $solrResults -ne 0 ]; then
	echo "SOLR (expected at $solrUrl) is not running; see detailed instructions to start it"
	exitResults=1
else
	echo "SOLR ($solrUrl) is running"
fi

if [ $gwavaResults -ne 0 ]; then
	echo "The GWAVA web site (expected at $gwavaUrl) is not running; see detailed instructions to start it"
	exitResults=1
else
	echo "The GWAVA web site ($gwavaUrl) is running"
fi

if [ $transmartResults -ne 0 ]; then
	echo "The tranSMART web site (expected at $transmartUrl) is not running; see detailed instructions to start it"
	exitResults=1
else
	echo "The tranSMART web site ($transmartUrl) is running"
fi

exit $exitResults

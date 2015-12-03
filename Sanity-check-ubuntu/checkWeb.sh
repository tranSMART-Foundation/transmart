#!/bin/bash

# ********************************************************************************
# This script checks for and reports incompatible version numbers in the 
# linux command lines that are needed for the tranSAMRT install and data loading
# ********************************************************************************

# # ------------------ source helper function -------------------
. ./checkUrl.sh

solrUrl="http://localhost:8983/solr/#/"
gwavaUrl="http://localhost:8080/gwava/"
transmartUrl="http://localhost:8080/transmasrt"

checkURL $solrUrl
solrResults=$?

checkURL $gwavaUrl
gwavaResults=$?

checkURL $transmartUrl
transmartResults=$?

echo "solr $solrResults"
echo "gwava $gwavaResults"
echo "transmart $transmartResults"
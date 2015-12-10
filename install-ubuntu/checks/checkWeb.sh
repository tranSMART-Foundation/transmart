#!/bin/bash

# ********************************************************************************
# This script checks for and reports incompatible version numbers in the 
# linux command lines that are needed for the tranSAMRT install and data loading
# ********************************************************************************

function checkURL {
    curl -s -o "/dev/null" $1
    results=$?
    if [ $results -ne 0 ] ; then
        echo "Error occurred getting URL $1:"
        if [ $results -eq 6 ]; then
            echo "  Unable to resolve host"
        fi
        if [ $results -eq 7 ]; then
            echo "  Unable to connect to host"
        fi
        return 1
    fi
    return 0	
}

# ---------------------------
# Check to see that the expected web sites are running
# ---------------------------

echo "-------------------------------------"
echo "|  Check to see that the expected web sites are running"
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

if ! $(curl $transmartUrl | grep "title" | grep "tranSMART") ; then
	echo "The tranSMART web site (at $transmartUrl) is not delivering the login home page;"
	echo "  see tomcat log file, transmart.log for possible errors "
	exitResults=1
else
	echo "The transmart home page (login) appears to be loading"
fi

exit $exitResults

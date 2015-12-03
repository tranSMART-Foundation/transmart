#!/bin/bash

# ********************************************************************************
# This script to see that the required tool processes are running:
# SOLR, tomcat, Rserve
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking that the required tool processes are running:"
echo "|  SOLR, tomcat, Rserve"
echo "-------------------------------------"

solrRunning=$(ps aux | grep "start.jar" | grep "java")
tomcatRunning=$(ps aux | grep "tomcat" | grep "catalina")
rserveRunning=$(ps aux | grep "Rserve" | grep "R/root")

exitReturn=0
if [ -z "$solrRunning" ]; then 
	echo "The SOLR process does not appear to be running; start it"
	echo "  with the commands:"
	echo "  cd ~/transmart/transmart-data"
    echo "  . ./vars"
    echo "  make -C solr start > ~/transmart/transmart-data/solr.log 2>&1 &"
	exitReturn=1
else 
	echo "The SOLR process is running"
fi

if [ -z "$tomcatRunning" ]; then 
	echo "The tomcat7 process does not appear to be running; start it"
	echo "  with the command: sudo service tomcat7 restart"
	exitReturn=1
else 
	echo "The tomcat7 process is running"
fi

if [ -z "$rserveRunning" ]; then 
	echo "The Rserve process does not appear to be running; start it"
	echo "  with the commands:"
	echo "    cd ~/transmart/transmart-data"
	echo "    sudo -u tomcat7 bash -c 'source vars; make -C R start_Rserve' "
	exitReturn=1
else 
	echo "The Rserve process is running"
fi

exit $exitReturn
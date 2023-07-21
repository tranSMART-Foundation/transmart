#!/bin/bash

# ****************************************************************
# This script to see that the required tool processes are running:
# SOLR, tomcat, Rserve
# ****************************************************************

case $TMINSTALL_OS in
    ubuntu)
	case $TMINSTALL_OSVERSION in
	    18.04 | 18)
		tomcatservice="tomcat8"
		;;
	    20.04 | 20 | 22.04 | 22)
		tomcatservice="tomcat9"
		;;
	esac
esac

echo "---------------------------------------------------------"
echo "|  Checking that the required tool processes are running:"
echo "|  SOLR, tomcat, Rserve"
echo "---------------------------------------------------------"

solrRunning=$(ps aux | grep "start.jar" | grep "java")
tomcatRunning=$(ps aux | grep "tomcat" | grep "catalina")
rserveRunning=$(ps aux | grep "Rserve" | grep "R/root")

exitReturn=0
if [ -z "$solrRunning" ]; then 
	echo "The SOLR process does not appear to be running; start it"
	echo "  with the command:"
	echo "  sudo systemctl start solr"
	exitReturn=1
else 
	echo "The SOLR process is running"
fi

if [ -z "$tomcatRunning" ]; then 
	echo "The $tomcatservice process does not appear to be running; start it"
	echo "  with the command: sudo systemctl restart $tomcatservice"
	exitReturn=1
else 
	echo "The $tomcatservice process is running"
fi

if [ -z "$rserveRunning" ]; then 
	echo "The Rserve process does not appear to be running; start it"
	echo "  with the command:"
	echo "    sudo systemctl start rserve"
	exitReturn=1
else 
	echo "The Rserve process is running"
fi

exit $exitReturn

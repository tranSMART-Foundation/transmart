#!/bin/bash

# **************************************************************
# This script checks for tomcat and mods to tomcat configuration
# **************************************************************

echo "------------------------------------------------------------------------"
echo "|  Checking for tomcat install and modifications to tomcat configuration"
echo "------------------------------------------------------------------------"

if ! [ -e /etc/default/tomcat8 ] ; then
    echo "It appears that tomcat is not installed"
    echo "Please check install set and repeat test"
    exit 1
fi

sudo service tomcat8 status | grep -q "servlet engine"
results=$?
if ! [ $results ] ; then
    echo "It appears that tomcat is not installed"
    echo "Please check install set and repeat test"
    exit 1
fi

grep "Xmx2g" /etc/default/tomcat8 | grep -q "JAVA_OPTS"
results=$?
if ! [ $results ] ; then
    echo "It appears that the tomcat configuration for heap space"
    echo "was not modified; tomcat will not fun correctly"
    echo "the heap space in /etc/default/tomcat8 should be set to -Xmx2g"
    exit 1
fi

echo "-----------------------------------------"
echo "|  tomcat install and configuration is ok"
echo "-----------------------------------------"

exit 0

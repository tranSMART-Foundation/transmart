#!/bin/bash

# **************************************************************
# This script checks for tomcat and mods to tomcat configuration
# **************************************************************

# tomcat locations
# ubuntu18 /etc/default/tomcat8 (TOMCAT8_USER, JAVA_HOME, JAVA_OPTS)
#          /etc/tomcat8/ context.xml server.xml tomcat-users.xml
#          
# ubuntu20 tomcat9

tomcatVersion=0

echo "------------------------------------------------------------------------"
echo "|  Checking for tomcat install and modifications to tomcat configuration"
echo "------------------------------------------------------------------------"

if [ -e /etc/default/tomcat9 ] ; then
    tomcatVersion=9
elif [ -e /etc/default/tomcat8 ] ; then
    tomcatVersion=8
elif [ -e /etc/default/tomcat7 ] ; then
    tomcatVersion=7
else
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

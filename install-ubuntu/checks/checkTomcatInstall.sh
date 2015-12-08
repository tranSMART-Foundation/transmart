#!/bin/bash

# ********************************************************************************
# This script checks for tomcat and modes to tomcat configuration
# ********************************************************************************

echo "-------------------------------------"
echo "|  Checking for tomcat install and modes to tomcat configuration"
echo "-------------------------------------"

if ! -e /etc/default/tomcat7 ; then
    echo "It appears that tomcat is not installed"
    echo "Please check install set and repeat test"
    exit 1
fi

sudo service tomcat7 status | grep "servlet engine"
results=$?
if (! results) ; then
    echo "It appears that tomcat is not installed"
    echo "Please check install set and repeat test"
    exit 1
fi

grep "Xmx2g" /etc/default/tomcat7
results=$?
if (! results) ; then
    echo "It appears that the tomcat configuration for heap space"
    echo "was not modified; tomcat will not fun correctly"
    echo "the heap space in /etc/default/tomcat7 should be set to -Xmx2g"
    exit 1
fi

exit 0